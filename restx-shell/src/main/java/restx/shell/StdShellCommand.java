package restx.shell;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import restx.common.MoreResources;
import restx.common.Mustaches;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
    public void help(Appendable appendable) throws IOException {
        RestxShell.printIn(appendable, String.format("%10s", aliases.get(0)), RestxShell.AnsiCodes.ANSI_GREEN);
        appendable.append(" - " + getShortDescription() + "\n");
    }

    @Override
    public void man(Appendable appendable) throws IOException {
        RestxShell.printIn(appendable, aliases.get(0), RestxShell.AnsiCodes.ANSI_GREEN);
        appendable.append(" - " + getShortDescription() + "\n");
        if (aliases.size() > 1) {
            appendable.append("  aliases: " + Joiner.on(", ").join(aliases.subList(1, aliases.size())) + "\n");
        }

        String resourceMan = resourceMan();
        if (resourceMan != null) {
            appendable.append("\n");
            appendResourceMan(appendable, resourceMan);
            appendable.append("\n\n");
        }
    }

    protected String resourceMan() {
        return null;
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


    protected List<String> splitArgs(String line) {
        return RestxShell.splitArgs(line);
    }

    @Override
    public void start(RestxShell shell) throws IOException {
    }

    protected Appendable appendResourceMan(Appendable appendable, String man) throws IOException {
        return appendable.append(Resources.toString(Resources.getResource(man), Charsets.UTF_8));
    }
}
