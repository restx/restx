package restx.shell;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 4/9/13
 * Time: 10:06 PM
 */
public interface ShellCommand {
    Optional<ShellCommandMatch> match(ConsoleReader reader, String line);
    boolean run(ConsoleReader reader, ShellCommandMatch match) throws IOException;
    void help(ConsoleReader reader) throws IOException;
    void man(ConsoleReader reader) throws IOException;
    Iterable<Completer> getCompleters();
    ImmutableList<String> getAliases();
}
