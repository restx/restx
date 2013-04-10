package restx.shell;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import restx.common.Mustaches;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;

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

    protected String ask(ConsoleReader reader, String msg, String defaultValue) throws IOException {
        String value = reader.readLine(String.format(msg, defaultValue));
        if (value.trim().isEmpty()) {
            return defaultValue;
        } else {
            return value.trim();
        }
    }

    protected boolean askBoolean(ConsoleReader reader, String message, String defaultValue) throws IOException {
        return asList("y", "yes", "true", "on").contains(ask(reader, message, defaultValue).toLowerCase(Locale.ENGLISH));
    }

    protected <T> ImmutableMap<Mustache, String> buildTemplates(Class<T> clazz, ImmutableMap<String, String> tpls) {
        ImmutableMap.Builder<Mustache, String> builder = ImmutableMap.builder();

        for (Map.Entry<String, String> entry : tpls.entrySet()) {
            builder.put(Mustaches.compile(clazz, entry.getKey()), entry.getValue());
        }

        return builder.build();
    }

    protected void generate(ImmutableMap<Mustache, String> templates, Path path, Object scope) throws IOException {
        for (Map.Entry<Mustache, String> entry : templates.entrySet()) {
            Mustaches.execute(entry.getKey(), scope, resolvePath(path, entry.getValue(), scope));
        }

    }

    private Path resolvePath(Path path, String relative, Object scope) {
        return path.resolve(Mustaches.execute(
                new DefaultMustacheFactory().compile(new StringReader(relative), relative), scope));
    }

    protected Path currentLocation() {
        return Paths.get(".");
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
