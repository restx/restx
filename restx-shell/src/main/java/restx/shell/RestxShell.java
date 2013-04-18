package restx.shell;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import restx.factory.Factory;
import restx.shell.commands.HelpCommand;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * User: xavierhanin
 * Date: 4/9/13
 * Time: 9:42 PM
 */
public class RestxShell implements Appendable {
    private final ConsoleReader consoleReader;
    private final Factory factory;
    private final ImmutableSet<ShellCommand> commands;

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

    public void println(String msg) throws IOException {
        consoleReader.println(msg);
    }

    public Path currentLocation() {
        return Paths.get(".");
    }





    protected void initConsole(ConsoleReader consoleReader) {
        consoleReader.setPrompt("restx> ");
        consoleReader.setHistoryEnabled(true);
    }

    protected void banner() throws IOException {
        consoleReader.println("===============================================================================");
        consoleReader.println("== WELCOME TO RESTX SHELL - type `help` for help on available commands");
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
}
