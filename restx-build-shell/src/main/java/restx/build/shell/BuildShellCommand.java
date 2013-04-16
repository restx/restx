package restx.build.shell;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import restx.build.*;
import restx.factory.Component;
import restx.shell.ShellCommandRunner;
import restx.shell.StdShellCommand;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
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
                        return Optional.<ShellCommandRunner>of(new GenerateModuleCommandRunner(new IvySupport()));
                    case "pom":
                        return Optional.<ShellCommandRunner>of(new GenerateModuleCommandRunner(new MavenSupport()));

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
        private final RestxBuild.Generator generator;

        public GenerateModuleCommandRunner(RestxBuild.Generator generator) {
            this.generator = generator;
        }

        @Override
        public boolean run(ConsoleReader reader) throws Exception {
            Path target = currentLocation().resolve(generator.getDefaultFileName());
            try (InputStream input = new FileInputStream(currentLocation().resolve("md.restx.json").toFile());
                    Writer writer = new FileWriter(target.toFile())) {
                ModuleDescriptor md = new RestxJsonSupport().parse(input);
                generator.generate(md, writer);
                reader.println("generated " + target);
            }
            return false;
        }
    }
}
