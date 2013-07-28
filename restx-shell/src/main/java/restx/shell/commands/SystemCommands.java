package restx.shell.commands;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import restx.factory.Module;
import restx.factory.Provides;
import restx.shell.RestxShell;
import restx.shell.ShellCommand;
import restx.shell.ShellCommandRunner;
import restx.shell.StdShellCommand;

import java.util.List;

/**
 * User: xavierhanin
 * Date: 4/9/13
 * Time: 10:11 PM
 */
@Module
public class SystemCommands {

    @Provides public ShellCommand cd() {
        return new StdShellCommand(ImmutableList.of("cd"), "change current directory") {
            @Override
            protected Optional<? extends ShellCommandRunner> doMatch(String line) {
                List<String> args = splitArgs(line);

                if (args.size() < 2) {
                    return Optional.absent();
                }

                final String to = args.get(1);

                return Optional.of(new ShellCommandRunner() {
                    @Override
                    public void run(RestxShell shell) throws Exception {
                        shell.cd(shell.currentLocation().resolve(to));
                    }
                });
            }
        };
    }

    @Provides public ShellCommand pwd() {
        return new StdShellCommand(ImmutableList.of("pwd"), "print current directory") {
            @Override
            protected Optional<? extends ShellCommandRunner> doMatch(String line) {
                return Optional.of(new ShellCommandRunner() {
                    @Override
                    public void run(RestxShell shell) throws Exception {
                        shell.println(shell.currentLocation().toAbsolutePath().toString());
                    }
                });
            }
        };
    }
}
