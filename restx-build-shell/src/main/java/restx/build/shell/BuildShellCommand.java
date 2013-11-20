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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
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
        if (args.size() < 2) {
            return Optional.absent();
        }
        switch (args.get(1)) {
            case "generate":
                if (args.size() < 3) {
                    return Optional.absent();
                }
                switch (args.get(2)) {
                    case "ivy":
                        return Optional.<ShellCommandRunner>of(new GenerateModuleCommandRunner("module.ivy"));
                    case "pom":
                        return Optional.<ShellCommandRunner>of(new GenerateModuleCommandRunner("pom.xml"));
                }
                break;
            case "watch":
                return Optional.<ShellCommandRunner>of(new WatchCommandRunner());
        }

        return Optional.absent();
    }

    @Override
    protected String resourceMan() {
        return "restx/build/shell/build.man";
    }

    @Override
    public Iterable<Completer> getCompleters() {
        return ImmutableList.<Completer>of(
                new ArgumentCompleter(new StringsCompleter("build"), new StringsCompleter("generate"), new StringsCompleter("ivy", "pom")),
                new ArgumentCompleter(new StringsCompleter("build"), new StringsCompleter("watch")));
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

    private class WatchCommandRunner implements ShellCommandRunner, RestxShell.WatchListener {
        @Override
        public void run(RestxShell shell) throws Exception {
            shell.watchFile(this);
            shell.printIn("now watching md.restx.json files\n", RestxShell.AnsiCodes.ANSI_YELLOW);
        }

        @Override
        public void onEvent(RestxShell shell, WatchEvent.Kind<?> kind, Path path) {
            if (path.endsWith("md.restx.json") && kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                mayConvertTo(shell, path, path.getParent().resolve("pom.xml"));
                mayConvertTo(shell, path, path.getParent().resolve("ivy.xml"));
                mayConvertTo(shell, path, path.getParent().resolve("module.ivy"));
            }
        }

        private void mayConvertTo(RestxShell shell, Path source, Path target) {
            if (target.toFile().exists()) {
                try {
                    RestxBuild.convert(source, target);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
