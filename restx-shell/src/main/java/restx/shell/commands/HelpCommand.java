package restx.shell.commands;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import restx.shell.RestxShell;
import restx.shell.ShellCommand;
import restx.shell.ShellCommandRunner;
import restx.shell.StdShellCommand;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 4/9/13
 * Time: 10:11 PM
 */
public class HelpCommand extends StdShellCommand {
    private final Set<ShellCommand> commands;

    public HelpCommand(Set<ShellCommand> commands) {
        super(ImmutableList.of("help", "man"), "provides list of commands or a command manual");
        this.commands = commands;
    }

    @Override
    protected Optional<? extends ShellCommandRunner> doMatch(String line) {
        final List<String> args = splitArgs(line);
        if (args.size() > 1) {
            return Optional.of(new ShellCommandRunner() {
                @Override
                public void run(RestxShell shell) throws IOException {
                    man(shell, args.get(1));
                }
            });
        } else {
            return Optional.of(new ShellCommandRunner() {
                @Override
                public void run(RestxShell shell) throws IOException {
                    for (ShellCommand command : commands) {
                        command.help(shell);
                    }
                    shell.println("");
                    shell.println("use `help <command>` with any of these commands to get a detailed man on the command");
                }
            });
        }
    }

    private void man(RestxShell shell, String command) throws IOException {
        for (ShellCommand shellCommand : commands) {
            if (shellCommand.getAliases().contains(command)) {
                shellCommand.man(shell);
                return;
            }
        }

        if (getAliases().contains(command)) {
            man(shell);
        } else {
            shell.println("command not found: `" + command + "`. use `help` to get the list of available commands.");
        }
    }
}
