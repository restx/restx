package restx.shell;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * User: xavierhanin
 * Date: 4/9/13
 * Time: 10:24 PM
 */
public abstract class StdShellCommand implements ShellCommand {
    private final ImmutableList<String> aliases;
    private final String shortDescription;

    protected StdShellCommand(ImmutableList<String> aliases, String shortDescription) {
        this.aliases = Preconditions.checkNotNull(aliases);
        if (aliases.isEmpty()) {
            throw new IllegalArgumentException("aliases must not be empty");
        }

        this.shortDescription = shortDescription;
    }

    @Override
    public Optional<? extends ShellCommandRunner> match(String line) {
        for (String alias : aliases) {
            if (matchCommand(line, alias)) {
                return doMatch(line);
            }
        }

        return Optional.absent();
    }

    private boolean matchCommand(String line, String commandName) {
        return line.equals(commandName) || line.startsWith(commandName + " ");
    }

    protected abstract Optional<? extends ShellCommandRunner> doMatch(String line);

    @Override
    public void help(ConsoleReader reader) throws IOException {
        printIn(reader, String.format("%10s", aliases.get(0)), AnsiCodes.ANSI_GREEN);
        reader.println(" - " + getShortDescription());
    }

    @Override
    public void man(ConsoleReader reader) throws IOException {
        printIn(reader, aliases.get(0), AnsiCodes.ANSI_GREEN);
        reader.println(" - " + getShortDescription());
        if (aliases.size() > 1) {
            reader.println("  aliases: " + Joiner.on(", ").join(aliases.subList(1, aliases.size())));
        }
    }

    @Override
    public Iterable<Completer> getCompleters() {
        return Collections.<Completer>singleton(commandCompleter());
    }

    protected StringsCompleter commandCompleter() {
        return new StringsCompleter(aliases);
    }

    public ImmutableList<String> getAliases() {
        return aliases;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    protected void printIn(ConsoleReader reader, String msg, String ansiCode) throws IOException {
        reader.print(ansiCode + msg + AnsiCodes.ANSI_RESET);
    }

    protected List<String> splitArgs(String line) {
        return ImmutableList.copyOf(Splitter.on(" ").omitEmptyStrings().split(line));
    }

    protected <T> T openSubSession(ConsoleReader consoleReader,
                                   String prompt, ImmutableList<StringsCompleter> completers, Callable<T> callable) throws Exception {
        String storedPrompt = consoleReader.getPrompt();
        consoleReader.setPrompt(prompt);
        // store completers and set ours
        Collection<Completer> storedCompleters = ImmutableList.copyOf(consoleReader.getCompleters());
        for (Completer completer : storedCompleters) {
            consoleReader.removeCompleter(completer);
        }
        for (StringsCompleter completer : completers) {
            consoleReader.addCompleter(completer);
        }

        try {
            return callable.call();
        } finally {
            for (Completer completer : ImmutableList.copyOf(consoleReader.getCompleters())) {
                consoleReader.removeCompleter(completer);
            }
            for (Completer completer : storedCompleters) {
                consoleReader.addCompleter(completer);
            }
            consoleReader.setPrompt(storedPrompt);
        }
    }

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
