package restx.core.shell;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import restx.AppSettings;
import restx.Apps;
import restx.shell.RestxShell;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static restx.common.MoreFiles.copyDir;

/**
 * User: xavierhanin
 * Date: 9/8/13
 * Time: 2:48 PM
 */
public class ShellAppRunner {
    public static enum CompileMode {
        NO {
            @Override
            boolean compile(RestxShell shell, Path targetClasses, Path dependenciesDir, Path mainSources, Path mainResources, String className) throws IOException, InterruptedException {
                return true;
            }
        }, MAIN_CLASS {
            @Override
            boolean compile(RestxShell shell, Path targetClasses, Path dependenciesDir, Path mainSources, Path mainResources, String className) throws IOException, InterruptedException {
                shell.print("compiling App...");
                shell.currentLocation().resolve(targetClasses).toFile().mkdirs();
                int compiled = new ProcessBuilder(
                        "javac", "-cp", dependenciesDir + "/*", "-sourcepath", mainSources.toString(),
                            "-d", targetClasses.toString(),
                            mainSources.resolve(className.replace('.', '/') + ".java").toString())
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .directory(shell.currentLocation().toFile().getAbsoluteFile())
                        .start()
                        .waitFor();
                if (compiled != 0) {
                    shell.printIn(" [ERROR]", RestxShell.AnsiCodes.ANSI_RED);
                    shell.println("");
                    return false;
                }
                shell.printIn(" [DONE]", RestxShell.AnsiCodes.ANSI_GREEN);
                shell.println("");

                shell.print("copying resources...");
                copyDir(
                        shell.currentLocation().resolve(mainResources),
                        shell.currentLocation().resolve(targetClasses)
                );
                shell.printIn(" [DONE]", RestxShell.AnsiCodes.ANSI_GREEN);
                shell.println("");
                return true;
            }
        }, ALL {
            @Override
            boolean compile(RestxShell shell,
                            Path targetClasses, Path dependenciesDir,
                            final Path mainSources, Path mainResources,
                            String className) throws IOException, InterruptedException {
                shell.print("compiling App...");
                shell.currentLocation().resolve(targetClasses).toFile().mkdirs();
                File currentDir = shell.currentLocation().toFile().getAbsoluteFile();

                final List<String> sources = new ArrayList<>();
                java.nio.file.Files.walkFileTree(mainSources, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.toString().endsWith(".java")) {
                            sources.add(file.toAbsolutePath().toString());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                shell.printIn(" [" + sources.size() + " source files]", RestxShell.AnsiCodes.ANSI_CYAN);
                File classesFile = new File(currentDir, ".restx.classes");
                Files.write(Joiner.on("\n").join(sources), classesFile, Charsets.UTF_8);
                int compiled = new ProcessBuilder(
                        "javac", "-cp", dependenciesDir + "/*", "-sourcepath", mainSources.toString(),
                            "-d", targetClasses.toString(),
                            "@" + classesFile.getName())
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .directory(currentDir)
                        .start()
                        .waitFor();
                classesFile.delete();

                if (compiled != 0) {
                    shell.printIn(" [ERROR]", RestxShell.AnsiCodes.ANSI_RED);
                    shell.println("");
                    return false;
                }
                shell.printIn(" [DONE]", RestxShell.AnsiCodes.ANSI_GREEN);
                shell.println("");

                shell.print("copying resources...");
                copyDir(
                        shell.currentLocation().resolve(mainResources),
                        shell.currentLocation().resolve(targetClasses)
                );
                shell.printIn(" [DONE]", RestxShell.AnsiCodes.ANSI_GREEN);
                shell.println("");
                return true;
            }
        };

        abstract boolean compile(RestxShell shell, Path targetClasses, Path dependenciesDir, Path mainSources, Path mainResources, String className) throws IOException, InterruptedException;

    }
    private final AppSettings appSettings;

    private final String appClassName;
    private final CompileMode compile;
    private final boolean quiet;
    private final boolean daemon;
    private final List<String> vmOptions;

    public ShellAppRunner(AppSettings appSettings, String appClassName, CompileMode compile,
                          boolean quiet, boolean daemon, List<String> vmOptions) {
        this.appSettings = appSettings;
        this.appClassName = appClassName;
        this.compile = compile;
        this.quiet = quiet;
        this.daemon = daemon;
        this.vmOptions = new ArrayList<>(vmOptions);
    }

    public void run(RestxShell shell) throws IOException, InterruptedException {
        Path targetClasses = Paths.get(appSettings.targetClasses());
        Path dependenciesDir = Paths.get(appSettings.targetDependency());
        Path mainSources = Paths.get(appSettings.mainSources());
        Path mainResources = Paths.get(appSettings.mainResources());

        if (!compile.compile(shell, targetClasses, dependenciesDir, mainSources, mainResources, appClassName)) return;

        shell.println("starting " + appClassName + "..." +
                (daemon ? " - type `stop` to stop it and go back to restx shell" : ""));
        vmOptions.add("-Drestx.app.package=" + appSettings.appPackage());
        Process run = Apps.with(appSettings)
                                .run(shell.currentLocation().toFile(),
                                        targetClasses, dependenciesDir, vmOptions,
                                        appClassName, Collections.<String>emptyList(), quiet);

        if (daemon) {
            while (!shell.ask("", "").equals("stop")) {
                shell.printIn("restx> unrecognized command - type `stop` to stop the app",
                        RestxShell.AnsiCodes.ANSI_YELLOW);
                shell.println("");
            }
            run.destroy();
        }
        run.waitFor();
    }

}
