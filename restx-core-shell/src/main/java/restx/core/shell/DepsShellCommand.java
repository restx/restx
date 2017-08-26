package restx.core.shell;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import restx.AppSettings;
import restx.build.*;
import restx.build.ModuleDescriptor;
import restx.factory.Component;
import restx.plugins.*;
import restx.shell.*;

import java.io.*;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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

    public static class InstallDepsCommandRunner implements ShellCommandRunner {

        @Override
        public void run(RestxShell shell) throws Exception {
            Optional<ModuleDescriptorType> moduleDescriptorTypeWithExistingFileOpt = ModuleDescriptorType.firstModuleDescriptorTypeWithExistingFile(shell.currentLocation());
            if(!moduleDescriptorTypeWithExistingFileOpt.isPresent()) {
                throw new IllegalStateException(
                        "md.restx.json file not found in " + shell.currentLocation() + "." +
                                " It is required to perform deps management");
            }

            ModuleDescriptorType moduleDescriptorTypeWithExistingFile = moduleDescriptorTypeWithExistingFileOpt.get();
            if(ModuleDescriptorType.RESTX.equals(moduleDescriptorTypeWithExistingFile)) {
                shell.println("installing deps using restx module descriptor...");
                installDepsFromModuleDescriptor(shell, ModuleDescriptorType.RESTX.resolveDescriptorFile(shell.currentLocation()));
            } else if(ModuleDescriptorType.MAVEN.equals(moduleDescriptorTypeWithExistingFile)) {
                shell.println("installing deps using maven descriptor...");
                installDepsFromMavenDescriptor(shell, ModuleDescriptorType.MAVEN.resolveDescriptorFile(shell.currentLocation()));
            } else {
                throw new IllegalArgumentException("Unsupported deps install for module type "+moduleDescriptorTypeWithExistingFile);
            }

            storeModuleDescriptorMD5File(shell, moduleDescriptorTypeWithExistingFile);

            shell.println("DONE");
        }

        private void installDepsFromModuleDescriptor(RestxShell shell, File mdFile) throws Exception {
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
                                        shell.currentLocation().toAbsolutePath() + "/target/dependency/[artifact]-[revision](-[classifier]).[ext]")
                                .setSync(true)
                );
            } finally {
                tempFile.delete();
            }
        }

        private void installDepsFromMavenDescriptor(RestxShell shell, File pomFile) throws Exception {
            AppSettings appSettings = shell.getFactory().getComponent(AppSettings.class);

            Path dependenciesDir = Paths.get(appSettings.targetDependency());

            // Emptying target dependencies directory first
            if(dependenciesDir.toFile().exists()) {
                java.nio.file.Files.walkFileTree(dependenciesDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        java.nio.file.Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        java.nio.file.Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            dependenciesDir.toFile().mkdirs();

            // Then copying dependencies through copy-dependencies plugin
            ProcessBuilder mavenCmd = new ProcessBuilder(
                    "mvn", "org.apache.maven.plugins:maven-dependency-plugin:3.0.1:copy-dependencies",
                    "-DoutputDirectory=" + dependenciesDir.toAbsolutePath(), "-DincludeScope=runtime"
            );

            shell.println("Executing `"+mavenCmd+"` ...");
            mavenCmd.redirectErrorStream(true)
             .redirectOutput(ProcessBuilder.Redirect.INHERIT)
             .directory(shell.currentLocation().toFile().getAbsoluteFile())
             .start()
             .waitFor();
        }

        private static void storeModuleDescriptorMD5File(RestxShell shell, ModuleDescriptorType mdType) throws IOException {
            File mdFile = mdType.resolveDescriptorFile(shell.currentLocation());
            File md5File = ModuleDescriptorType.MAVEN.resolveDescriptorMd5File(shell.currentLocation());

            shell.println(String.format("Storing md5 file %s for module descriptor %s...", md5File.getAbsolutePath(), mdFile.getAbsolutePath()));
            Files.write(Files.hash(mdFile, Hashing.md5()).toString(), md5File, Charsets.UTF_8);
        }
    }

    static class AddDepsCommandRunner implements ShellCommandRunner {
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
            File mdFile = ModuleDescriptorType.RESTX.resolveDescriptorFile(shell.currentLocation());
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
        Optional<ModuleDescriptorType> moduleDescriptorTypeWithExistingFileOpt = ModuleDescriptorType.firstModuleDescriptorTypeWithExistingFile(shell.currentLocation());
        if(!moduleDescriptorTypeWithExistingFileOpt.isPresent()) {
            // no dependency management at all
            return true;
        }

        ModuleDescriptorType moduleDescriptorTypeWithExistingFile = moduleDescriptorTypeWithExistingFileOpt.get();
        File descriptorFile = moduleDescriptorTypeWithExistingFile.resolveDescriptorFile(shell.currentLocation());
        File moduleDescriptorMd5 = moduleDescriptorTypeWithExistingFile.resolveDescriptorMd5File(shell.currentLocation());
        if(!moduleDescriptorMd5.exists()) {
            return false;
        }

        try {
            String md5 = Files.hash(descriptorFile, Hashing.md5()).toString();
            return md5.equals(Files.toString(moduleDescriptorMd5, Charsets.UTF_8));
        } catch (IOException e) {
            return false;
        }
    }
}
