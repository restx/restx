package restx.core.shell;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import restx.build.*;
import restx.build.ModuleDescriptor;
import restx.factory.Component;
import restx.plugins.*;
import restx.shell.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * User: xavierhanin
 * Date: 4/10/13
 * Time: 8:53 PM
 */
@Component
public class DepsShellCommand extends StdShellCommand {
    public DepsShellCommand() {
        super(ImmutableList.of("deps"), "deps related commands: install / update / manage app dependencies");
    }

    @Override
    protected String resourceMan() {
        return "restx/core/shell/deps.man";
    }

    @Override
    protected Optional<? extends ShellCommandRunner> doMatch(String line) {
        List<String> args = splitArgs(line);

        if (args.size() < 2) {
            return Optional.absent();
        }

        switch (args.get(1)) {
            case "install":
                return Optional.of(new InstallDepsCommandRunner());
            case "add":
                return Optional.of(new AddDepsCommandRunner(args));
        }

        return Optional.absent();
    }

    @Override
    public Iterable<Completer> getCompleters() {
        return ImmutableList.<Completer>of(new ArgumentCompleter(
                new StringsCompleter("deps"), new StringsCompleter("install", "add")));
    }

    class InstallDepsCommandRunner implements ShellCommandRunner {

        @Override
        public void run(RestxShell shell) throws Exception {
            File mdFile = mdFile(shell);
            if (!mdFile.exists()) {
                throw new IllegalStateException(
                        "md.restx.json file not found in " + shell.currentLocation() + "." +
                                " It is required to perform deps management");
            }
            Ivy ivy = ShellIvy.loadIvy(shell);
            File tempFile = File.createTempFile("restx-md", ".ivy");
            try (FileInputStream is = new FileInputStream(mdFile)) {
                ModuleDescriptor descriptor = new RestxJsonSupport().parse(is);
                try (BufferedWriter w = Files.newWriter(tempFile,Charsets.UTF_8)) {
                    new IvySupport().generate(descriptor, w);
                }

                shell.println("resolving dependencies...");
                ResolveReport resolveReport = ivy.resolve(tempFile);

                shell.println("synchronizing dependencies in " + shell.currentLocation().resolve("target/dependency") + " ...");
                ivy.retrieve(resolveReport.getModuleDescriptor().getModuleRevisionId(),
                        new RetrieveOptions()
                                .setDestArtifactPattern(
                                        shell.currentLocation().toAbsolutePath() + "/target/dependency/[artifact]-[revision].[ext]")
                        .setSync(true)
                );

                File md5File = md5File(shell);
                Files.write(Files.hash(mdFile, Hashing.md5()).toString(), md5File, Charsets.UTF_8);

                shell.println("DONE");
            } finally {
                tempFile.delete();
            }
        }
    }

    class AddDepsCommandRunner implements ShellCommandRunner {
        private final String scope;
        private Optional<List<String>> pluginIds;

        public AddDepsCommandRunner(List<String> args) {
            args = new ArrayList<>(args);
            if (args.size() > 2
                    && args.get(2).startsWith("scope:")) {
                scope = args.get(2).substring("scope:".length());
                args.remove(2);
            } else {
                scope = "compile";
            }

            if (args.size() > 2) {
                pluginIds = Optional.<List<String>>of(new ArrayList<>(args.subList(2, args.size())));
            } else {
                pluginIds = Optional.absent();
            }
        }

        @Override
        public void run(RestxShell shell) throws Exception {
            File mdFile = mdFile(shell);
            if (!mdFile.exists()) {
                throw new IllegalStateException(
                        "md.restx.json file not found in " + shell.currentLocation() + "." +
                                " It is required to perform deps management");
            }

            if (!pluginIds.isPresent()) {
                ModulesManager modulesManager = new ModulesManager(
                        new URL("http://restx.io/modules"), ShellIvy.loadIvy(shell));

                shell.println("looking for plugins...");
                List<restx.plugins.ModuleDescriptor> plugins = modulesManager.searchModules("category=app");

                shell.printIn("found " + plugins.size() + " available plugins", RestxShell.AnsiCodes.ANSI_CYAN);
                shell.println("");

                for (int i = 0; i < plugins.size(); i++) {
                    restx.plugins.ModuleDescriptor plugin = plugins.get(i);
                    shell.printIn(String.format(" [%3d] %s%n", i + 1, plugin.getId()), RestxShell.AnsiCodes.ANSI_PURPLE);
                    shell.println("\t\t" + plugin.getDescription());
                }

                String sel = shell.ask("Which plugin would you like to add (eg '1 3 5')? \n" +
                        "You can also provide a plugin id in the form <groupId>:<moduleId>:<version>\n" +
                        " plugin to install: ", "");
                Iterable<String> selected = Splitter.on(" ").trimResults().omitEmptyStrings().split(sel);
                List<String> ids = new ArrayList<>();
                for (String s : selected) {
                    if (CharMatcher.DIGIT.matchesAllOf(s)) {
                        ids.add(plugins.get(Integer.parseInt(s)).getId());
                    } else {
                        ids.add(s);
                    }
                }

                pluginIds = Optional.of(ids);
            }

            ModuleDescriptor descriptor;
            try (FileInputStream is = new FileInputStream(mdFile)) {
                descriptor = new RestxJsonSupport().parse(is);

                for (String s : pluginIds.get()) {
                    descriptor = descriptor.concatDependency(scope, new ModuleDependency(GAV.parse(s)));
                }
            }
            try (Writer w = Files.newWriter(mdFile, Charsets.UTF_8)) {
                shell.println("updating " + mdFile);
                new RestxJsonSupport().generate(descriptor, w);
            }

            for (Path mod : RestxBuild.resolveForeignModuleDescriptorsIn(shell.currentLocation())) {
                shell.printIn("updating " + mod, RestxShell.AnsiCodes.ANSI_PURPLE);
                shell.println("");
                RestxBuild.convert(mdFile.toPath(), mod);
            }
        }
    }

    public static boolean depsUpToDate(RestxShell shell) {
        File mdFile = mdFile(shell);
        if (!mdFile.exists()) {
            // no dependency management at all
            return true;
        }

        File md5File = md5File(shell);

        if (!md5File.exists()) {
            return false;
        }

        try {
            String md5 = Files.hash(mdFile, Hashing.md5()).toString();
            return md5.equals(Files.toString(md5File, Charsets.UTF_8));
        } catch (IOException e) {
            return false;
        }
    }

    private static File mdFile(RestxShell shell) {
        return shell.currentLocation().resolve("md.restx.json").toFile();
    }

    public static File md5File(RestxShell shell) {
        return shell.currentLocation().resolve("target/dependency/md.restx.json.md5").toFile();
    }
}
