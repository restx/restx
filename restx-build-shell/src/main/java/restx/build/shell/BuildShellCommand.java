package restx.build.shell;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import restx.build.RestxBuild;
import restx.factory.Component;
import restx.shell.RestxShell;
import restx.shell.ShellCommandRunner;
import restx.shell.StdShellCommand;

import java.nio.file.Path;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 4/16/13
 * Time: 5:08 PM
 */
@Component
public class BuildShellCommand extends StdShellCommand {
    protected BuildShellCommand() {
        super(ImmutableList.of("build"), "build commands: generate pom, ivy, ...");
    }

    @Override
    protected Optional<? extends ShellCommandRunner> doMatch(String line) {
        final List<String> args = splitArgs(line);
        if (args.size() < 3) {
            return Optional.absent();
        }

        switch (args.get(1)) {
            case "generate":
                switch (args.get(2)) {
                    case "ivy":
                        return Optional.<ShellCommandRunner>of(new GenerateModuleCommandRunner("module.ivy"));
                    case "pom":
                        return Optional.<ShellCommandRunner>of(new GenerateModuleCommandRunner("pom.xml"));

                }
        }

        return Optional.absent();
    }

    @Override
    public Iterable<Completer> getCompleters() {
        return ImmutableList.<Completer>of(
                new ArgumentCompleter(new StringsCompleter("build"), new StringsCompleter("generate"), new StringsCompleter("ivy", "pom")));
    }

    private class GenerateModuleCommandRunner implements ShellCommandRunner {
        private final String target;

        public GenerateModuleCommandRunner(String target) {
            this.target = target;
        }

        @Override
        public void run(RestxShell shell) throws Exception {
            Path currentLocationAbsolutePath = shell.currentLocation().toAbsolutePath();
            List<Path> convert = RestxBuild.convert(currentLocationAbsolutePath + "/**/md.restx.json", target);
            if (convert.isEmpty()) {
                shell.println("no mathing file found. module descriptors should be named `md.restx.json`");
            } else {
                shell.println("converted:");
                for (Path path : convert) {
                    shell.println("\t" + currentLocationAbsolutePath.relativize(path));
                }
            }
        }
    }
}
