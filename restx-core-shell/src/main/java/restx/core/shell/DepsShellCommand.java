package restx.core.shell;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import restx.build.IvySupport;
import restx.build.ModuleDescriptor;
import restx.build.RestxJsonSupport;
import restx.factory.Component;
import restx.shell.RestxShell;
import restx.shell.ShellCommandRunner;
import restx.shell.ShellIvy;
import restx.shell.StdShellCommand;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

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
        }

        return Optional.absent();
    }

    @Override
    public Iterable<Completer> getCompleters() {
        return ImmutableList.<Completer>of(new ArgumentCompleter(
                new StringsCompleter("deps"), new StringsCompleter("install")));
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
