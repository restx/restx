package restx.shell.commands;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import restx.factory.Component;
import restx.shell.RestxShell;
import restx.shell.ShellCommandRunner;
import restx.shell.StdShellCommand;

import java.io.IOException;

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
    protected Optional<? extends ShellCommandRunner> doMatch(String line) {
        return Optional.of(new ShellCommandRunner() {
            @Override
            public void run(RestxShell shell) throws IOException {
                throw new RestxShell.ExitShell();
            }
        });
    }
}
