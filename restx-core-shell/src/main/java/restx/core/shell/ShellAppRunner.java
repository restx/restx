package restx.core.shell;

import restx.Apps;
import restx.shell.RestxShell;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static java.util.Arrays.asList;
import static restx.common.MoreFiles.copyDir;

/**
 * User: xavierhanin
 * Date: 9/8/13
 * Time: 2:48 PM
 */
public class ShellAppRunner {
    private final String appBasePackage;
    private final String appClassName;
    private final boolean compile;

    public ShellAppRunner(String appBasePackage, String appClassName, boolean compile) {
        this.appBasePackage = appBasePackage;
        this.appClassName = appClassName;
        this.compile = compile;
    }

    public void run(RestxShell shell) throws IOException, InterruptedException {
        Path targetClasses = Paths.get("target/classes");
        Path dependenciesDir = Paths.get("target/dependency");
        Path mainSources = Paths.get("src/main/java");
        Path mainResources = Paths.get("src/main/resources");

        if (compile) {
            shell.print("compiling App...");
            shell.currentLocation().resolve(targetClasses).toFile().mkdirs();
            int compiled = new ProcessBuilder(
                    "javac", "-cp", dependenciesDir + "/*", "-sourcepath", mainSources.toString(),
                        "-d", targetClasses.toString(),
                        mainSources.resolve(appClassName.replace('.', '/') + ".java").toString())
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .directory(shell.currentLocation().toFile().getAbsoluteFile())
                    .start()
                    .waitFor();
            if (compiled != 0) {
                shell.printIn(" [ERROR]", RestxShell.AnsiCodes.ANSI_RED);
                shell.println("");
                return;
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
        }

        shell.println("starting " + appClassName + "... - type `stop` to stop it and go back to restx shell");
        Process run = Apps.run(shell.currentLocation().toFile(),
                targetClasses, dependenciesDir, asList("-Drestx.app.package=" + appBasePackage),
                appClassName, Collections.<String>emptyList());

        while (!shell.ask("", "").equals("stop")) {
            shell.printIn("restx> unrecognized command - type `stop` to stop the app",
                    RestxShell.AnsiCodes.ANSI_YELLOW);
            shell.println("");
        }
        run.destroy();
        run.waitFor();
    }
}
