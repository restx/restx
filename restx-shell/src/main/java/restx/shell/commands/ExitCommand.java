package restx.shell.commands;

import com.google.common.collect.ImmutableList;
import jline.console.ConsoleReader;
import restx.factory.Component;
import restx.shell.ShellCommandMatch;
import restx.shell.StdShellCommand;

/**
 * User: xavierhanin
 * Date: 4/9/13
 * Time: 10:11 PM
 */
@Component
public class ExitCommand extends StdShellCommand {
    public ExitCommand() {
        super(ImmutableList.of("exit", "quit"), "exits the shell");
    }

    @Override
    public boolean run(ConsoleReader reader, ShellCommandMatch match) {
        return true;
    }
}
