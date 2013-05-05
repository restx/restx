package restx.shell;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteStreams;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import restx.common.Version;
import restx.factory.Factory;
import restx.shell.commands.HelpCommand;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;
import static jline.console.ConsoleReader.RESET_LINE;

/**
 * User: xavierhanin
 * Date: 4/9/13
 * Time: 9:42 PM
 */
public class RestxShell implements Appendable {
    private final ConsoleReader consoleReader;
    private final Factory factory;
    private final ImmutableSet<ShellCommand> commands;

    private WatchDir watcher;
    private final List<WatchListener> listeners = new CopyOnWriteArrayList<>();
    private final ExecutorService watcherExecutorService = Executors.newSingleThreadExecutor();

    public RestxShell(ConsoleReader consoleReader) {
        this(consoleReader, Factory.builder().addFromServiceLoader().build());
    }

    public RestxShell(ConsoleReader consoleReader, Factory factory) {
        this.consoleReader = consoleReader;
        this.factory = factory;

        this.commands = ImmutableSet.copyOf(findCommands());

        initConsole(consoleReader);
    }

    public ConsoleReader getConsoleReader() {
        return consoleReader;
    }

    public Factory getFactory() {
        return factory;
    }

    public ImmutableSet<ShellCommand> getCommands() {
        return commands;
    }

    public void start() throws IOException {
        banner();

        installCompleters();

        boolean exit = false;
        while (!exit) {
            String line = consoleReader.readLine();
            exit = exec(line);
        }

        consoleReader.println("Bye.");
        synchronized (this) {
            if (watcher != null) {
                watcherExecutorService.shutdownNow();
                watcher = null;
            }
        }
        consoleReader.shutdown();
    }

    public static void printIn(Appendable appendable, String msg, String ansiCode) throws IOException {
        appendable.append(ansiCode + msg + AnsiCodes.ANSI_RESET);
    }

    public static List<String> splitArgs(String line) {
        return ImmutableList.copyOf(Splitter.on(" ").omitEmptyStrings().split(line));
    }

    public void printIn(String msg, String ansiCode) throws IOException {
        consoleReader.print(ansiCode + msg + AnsiCodes.ANSI_RESET);
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        consoleReader.print(csq);
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        if (csq != null) {
            append(csq.subSequence(start, end));
        }
        return this;
    }

    @Override
    public Appendable append(char c) throws IOException {
        consoleReader.print(String.valueOf(c));
        return this;
    }

    public String ask(String msg, String defaultValue) throws IOException {
        String value = consoleReader.readLine(String.format(msg, defaultValue));
        if (value.trim().isEmpty()) {
            return defaultValue;
        } else {
            return value.trim();
        }
    }

    public boolean askBoolean(String message, String defaultValue) throws IOException {
        return asList("y", "yes", "true", "on").contains(ask(message, defaultValue).toLowerCase(Locale.ENGLISH));
    }


    public void print(String s) throws IOException {
        append(s);
    }

    public void println(String msg) throws IOException {
        consoleReader.println(msg);
        consoleReader.flush();
    }

    public void printError(String msg, Exception ex) {
        System.err.println(msg);
        ex.printStackTrace();
    }

    public Path currentLocation() {
        return Paths.get(".");
    }

    public Path installLocation() {
        return Paths.get(System.getProperty("restx.shell.home", "."));
    }

    public void restart() {
        try {
            new File(installLocation().toFile(), ".restart").createNewFile();
            printIn("RESTARTING SHELL...", AnsiCodes.ANSI_RED);
            println("");
            throw new ExitShell();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void watchFile(WatchListener listener) {
        synchronized (this) {
            if (watcher == null) {
                final Path dir = currentLocation();
                watcherExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            watcher = new WatchDir(dir, true) {
                                @Override
                                protected void onEvent(WatchEvent.Kind<?> kind, Path path) {
                                    for (WatchListener watchListener : listeners) {
                                        try {
                                            watchListener.onEvent(RestxShell.this, kind, path);
                                        } catch (Exception ex) {
                                            printError("FS event propagation to " + watchListener +
                                                    " raised an exception: " + ex, ex);
                                        }
                                    }
                                }
                            };
                            watcher.processEvents();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }
        listeners.add(listener);
    }

    protected void initConsole(ConsoleReader consoleReader) {
        consoleReader.setPrompt("restx> ");
        consoleReader.setHistoryEnabled(true);
    }

    protected void banner() throws IOException {
        consoleReader.println("===============================================================================");
        consoleReader.println("== WELCOME TO RESTX SHELL - " + version() + " - type `help` for help on available commands");
        consoleReader.println("===============================================================================");
    }

    protected boolean exec(String line) throws IOException {
        boolean found = false;
        for (ShellCommand command : commands) {
            Optional<? extends ShellCommandRunner> match = command.match(line);
            if (match.isPresent()) {
                String storedPrompt = consoleReader.getPrompt();
                // store current completers and clean them, so that executing command can perform in a clean env
                Collection<Completer> storedCompleters = ImmutableList.copyOf(consoleReader.getCompleters());
                for (Completer completer : storedCompleters) {
                    consoleReader.removeCompleter(completer);
                }

                try {
                    match.get().run(this);
                    found = true;
                } catch (ExitShell e) {
                    return true;
                } catch (Exception e) {
                    consoleReader.println("command " + line + " raised an exception: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    for (Completer completer : ImmutableList.copyOf(consoleReader.getCompleters())) {
                        consoleReader.removeCompleter(completer);
                    }
                    for (Completer completer : storedCompleters) {
                        consoleReader.addCompleter(completer);
                    }
                    consoleReader.setPrompt(storedPrompt);
                }
                break;
            }
        }
        if (!found) {
            consoleReader.println("command not found. use `help` to get the list of commands.");
        }
        return false;
    }




    protected void installCompleters() {
        for (ShellCommand command : commands) {
            for (Completer completer : command.getCompleters()) {
                consoleReader.addCompleter(completer);
            }
        }
    }

    protected Set<ShellCommand> findCommands() {
        Set<ShellCommand> commands = factory.queryByClass(ShellCommand.class).findAsComponents();
        HelpCommand helpCommand = new HelpCommand(commands);
        commands = Sets.newLinkedHashSet(commands);
        commands.add(helpCommand);
        return commands;
    }

    public static void main(String[] args) throws Exception {
        ConsoleReader consoleReader = new ConsoleReader();
        new RestxShell(consoleReader).start();
    }

    public String version() {
        return Version.getVersion("io.restx", "restx-shell");
    }

    public void download(final URL source, File destination) throws IOException {
        final String name = source.toString();
        println("downloading " + (name.length() <= 70 ? name : "[...]" + name.substring(name.length() - 65)));
        URLConnection connection = source.openConnection();
        final int total = connection.getContentLength();
        final int[] progress = new int[]{0};
        startProgress(name, total);
        try (InputStream stream = connection.getInputStream();
             final OutputStream out = new FileOutputStream(destination)) {
            ByteStreams.readBytes(stream,
                    new ByteProcessor<Void>() {
                        public boolean processBytes(byte[] buffer, int offset, int length)
                                throws IOException {
                            out.write(buffer, offset, length);
                            progress[0] += length;
                            updateProgress(name, progress[0], total);
                            return true;
                        }

                        public Void getResult() {
                            return null;
                        }
                    });
            endProgress(name);
        }
    }

    private void endProgress(String name) throws IOException {
        consoleReader.println();
    }

    private void startProgress(String name, int total) throws IOException {
        updateProgress(name, 0, total);
    }

    private void updateProgress(String name, int progress, int total) throws IOException {
        int barWidth = 70;
        StringBuilder line = new StringBuilder();

        if (progress >= total) {
            line.append("[").append(Strings.repeat("=", barWidth)).append("]");
        } else {
            int p = Math.min(progress * barWidth / total, barWidth - 1);
            line.append("[").append(Strings.repeat("=", p)).append(">").append(Strings.repeat(" ", barWidth - p - 1)).append("]");
        }

        line.append(String.format(" %3d", progress * 100 / total)).append("%");

        consoleReader.print("" + RESET_LINE + line);
    }


    public static final class ExitShell extends RuntimeException { }

    public static class AnsiCodes {
        public static final String ANSI_RESET = "\u001B[0m";
        public static final String ANSI_BLACK = "\u001B[30m";
        public static final String ANSI_RED = "\u001B[31m";
        public static final String ANSI_GREEN = "\u001B[32m";
        public static final String ANSI_YELLOW = "\u001B[33m";
        public static final String ANSI_BLUE = "\u001B[34m";
        public static final String ANSI_PURPLE = "\u001B[35m";
        public static final String ANSI_CYAN = "\u001B[36m";
        public static final String ANSI_WHITE = "\u001B[37m";
    }

    public static interface WatchListener {
        public void onEvent(RestxShell shell, WatchEvent.Kind<?> kind, Path path);
    }
}
