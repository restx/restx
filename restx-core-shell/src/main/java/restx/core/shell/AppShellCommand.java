package restx.core.shell;

import com.github.mustachejava.Mustache;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import restx.build.RestxBuild;
import restx.common.UUIDGenerator;
import restx.common.Version;
import restx.factory.Component;
import restx.shell.RestxShell;
import restx.shell.ShellCommandRunner;
import restx.shell.StdShellCommand;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * User: xavierhanin
 * Date: 4/10/13
 * Time: 8:53 PM
 */
@Component
public class AppShellCommand extends StdShellCommand {
    public AppShellCommand() {
        super(ImmutableList.of("app"), "app related commands: creates a new app, run your app, ...");
    }

    @Override
    protected Optional<? extends ShellCommandRunner> doMatch(String line) {
        List<String> args = splitArgs(line);

        if (args.size() < 2) {
            return Optional.absent();
        }

        switch (args.get(1)) {
            case "new":
                return Optional.of(new NewAppCommandRunner());
        }

        return Optional.absent();
    }

    @Override
    public Iterable<Completer> getCompleters() {
        return ImmutableList.<Completer>of(new ArgumentCompleter(
                new StringsCompleter("app"), new StringsCompleter("new")));
    }

    private class NewAppCommandRunner implements ShellCommandRunner {

        private ImmutableMap<Mustache, String> mainTemplates = buildTemplates(
                AppShellCommand.class, ImmutableMap.<String, String>builder()
                .put("md.restx.json.mustache", "md.restx.json")
                .put("AppModule.java.mustache", "src/main/java/{{packagePath}}/AppModule.java")
                .put("AppServer.java.mustache", "src/main/java/{{packagePath}}/AppServer.java")
                .put("web.xml.mustache", "src/main/webapp/WEB-INF/web.xml")
                .put("logback.xml.mustache", "src/main/resources/logback.xml")
                .build()
        );
        private ImmutableMap<Mustache, String> helloResourceTemplates = buildTemplates(
                AppShellCommand.class, ImmutableMap.<String, String>builder()
                .put("Message.java.mustache", "src/main/java/{{packagePath}}/domain/Message.java")
                .put("HelloResource.java.mustache", "src/main/java/{{packagePath}}/rest/HelloResource.java")
                .put("HelloResourceSpecTest.java.mustache", "src/test/java/{{packagePath}}/rest/HelloResourceSpecTest.java")
                .put("should_say_hello.spec.yaml.mustache", "src/test/resources/specs/hello/should_say_hello.spec.yaml")
                .build()
        );

        @Override
        public void run(RestxShell shell) throws Exception {
            shell.printIn("Welcome to RESTX APP bootstrap!", RestxShell.AnsiCodes.ANSI_GREEN);
            shell.println("");
            shell.println("This command will ask you a few questions to generate your brand new RESTX app.");
            shell.println("For any question you can get help by answering '??' (without the quotes).");
            shell.println("");

            String appName = "";
            while (Strings.isNullOrEmpty(appName)) {
                appName = shell.ask("App name? ", "",
                        "This is the name of the application you are creating.\n" +
                                "It can contain spaces, it's used mainly for documentation and to provide default for other values.\n" +
                                "Examples: Todo, Foo Bar, ...");
            }
            String groupId = shell.ask("group id [%s]? ",
                    appName.replaceAll("\\s+", "-").toLowerCase(Locale.ENGLISH),
                    "This is the identifier of the group or organization producing the application.\n" +
                            "In the Maven world this is called a groupId, in Ivy it's called organization.\n" +
                            "It MUST NOT contain spaces nor columns (':'), and is usually a reversed domain name.\n" +
                            "Examples: io.restx, com.example, ...");
            String artifactId = shell.ask("artifact id [%s]? ",
                    appName.replaceAll("\\s+", "-").toLowerCase(Locale.ENGLISH),
                    "This is the identifier of the app module.\n" +
                            "In the Maven world this is called an artifactId, in Ivy it's called module.\n" +
                            "It MUST NOT contain spaces nor columns (':'), and is usually a dash separated lower case word.\n" +
                            "Examples: myapp, todo, foo-app, ...")
                    .replaceAll("\\s+", "-");
            String mainPackage = shell.ask("main package [%s]? ",
                    artifactId.replaceAll("\\-", ".").toLowerCase(Locale.ENGLISH),
                    "This is the main package in which you will develop your application.\n" +
                            "In Java convention it should start with a reversed domain name followed by the app name\n" +
                            "but for applications (as opposed to APIs) we prefer to use a short name, like that app name.\n" +
                            "It MUST follow Java package names restrictions, so MUST NOT contain spaces\n" +
                            "Examples: myapp, com.example.todoapp, ...");
            String version = shell.ask("version [%s]? ", "0.1-SNAPSHOT",
                    "This is the name of the first version of the app you are targetting.\n" +
                            "It's recommended to use Maven convention to suffix it with -SNAPSHOT if you plan to use Maven for your app\n" +
                            "Examples: 0.1-SNAPSHOT, 1.0, ...");

            String buildFile = shell.ask("generate module descriptor (ivy/pom/none/all) [%s]? ", "all",
                    "This allows to generate a module descriptor for your app.\n" +
                            "Options:\n" +
                            "\t- 'ivy': get an Easyant compatible Ivy file generated for you.\n" +
                            "\t- 'pom': get a Maven POM generated for you.\n" +
                            "\t- 'all': get both a POM and an Ivy file.\n" +
                            "\t- 'none': get no module descriptor generated. WARNING: this will make it harder to build your app.\n" +
                            "If you don't know these tools, use default answer.\n"
            );
            boolean generateIvy = "ivy".equalsIgnoreCase(buildFile) || "all".equalsIgnoreCase(buildFile);
            boolean generatePom = "pom".equalsIgnoreCase(buildFile) || "all".equalsIgnoreCase(buildFile);

            String restxVersion = shell.ask("restx version [%s]? ", Version.getVersion("io.restx", "restx-core"));

            List<String> list = Lists.newArrayList(UUIDGenerator.generate(),
                    String.valueOf(new Random().nextLong()), appName, artifactId);
            Collections.shuffle(list);
            String signatureKey = shell.ask("signature key (to sign cookies) [%s]? ",
                    Joiner.on(" ").join(list),
                    "This is used as salt for signing stuff exchanged with the client.\n" +
                            "Use something fancy or keep what is proposed by default, but make sure to not share that publicly.");

            String adminPassword = shell.ask("admin password (to authenticate on restx console) [%s]? ",
                    String.valueOf(new Random().nextInt(10000)),
                    "This is used as password for the admin user to authenticate on restx console.\n" +
                            "This is only a default way to authenticate out of the box, restx security is very flexible.");

            String defaultPort = shell.ask("default port [%s]? ", "8080",
                    "This is the default port used when using embedded version.\n" +
                            "Usually Java web containers use 8080, it may be a good idea to use a different port to avoid \n" +
                            "conflicts with another servlet container.\n" +
                            "You can also use port 80 if you want to serve your API directly with the embedded server\n" +
                            "and no reverse proxy in front of it. But beware that you may need admin privileges for that.\n" +
                            "Examples: 8080, 8086, 8000, 80");
            String basePath = shell.ask("base path [%s]? ", "/api",
                    "This is the base API path on which RESTX will handle requests.\n" +
                            "Being focused on REST API only, RESTX is usually shared with either static or dynamic \n" +
                            "resources serving (HTML, CSS, JS, images, ...) and therefore is used to handle requests on\n" +
                            "only a sub path of the web app.\n" +
                            "If you plan to use it to serve requests from an API only domain (eg api.example.com)\n" +
                            "you can use '' (empty string) for this path.\n" +
                            "Examples: /api, /api/v2, /restx, ...");

            ImmutableMap scope = ImmutableMap.builder()
                    .put("appName", appName)
                    .put("groupId", groupId)
                    .put("artifactId", artifactId)
                    .put("mainPackage", mainPackage)
                    .put("packagePath", mainPackage.replace('.', '/'))
                    .put("version", version)
                    .put("signatureKey", signatureKey)
                    .put("adminPassword", adminPassword)
                    .put("defaultPort", defaultPort)
                    .put("basePath", basePath)
                    .put("restxVersion", restxVersion)
                    .build();

            boolean generateHelloResource = shell.askBoolean("generate hello resource example [Y/n]? ", "y",
                    "This will generate an example resource with an associated spec test so that your boostrapped\n" +
                            "application can be used as soon as it has been generated.\n" +
                            "If this is the first app you generate with RESTX, it's probably a good idea to generate\n" +
                            "this example resource.\n" +
                            "If you already know RESTX by heart you shouldn't be reading this message anyway :)");

            Path appPath = shell.currentLocation().resolve(artifactId);

            shell.println("scaffolding app to `" + appPath.toAbsolutePath() + "` ...");
            generate(mainTemplates, appPath, scope);

            if (generateIvy) {
                shell.println("generating module.ivy ...");
                RestxBuild.convert(appPath.toAbsolutePath() + "/md.restx.json", appPath.toAbsolutePath() + "/module.ivy");
            }
            if (generatePom) {
                shell.println("generating pom.xml ...");
                RestxBuild.convert(appPath.toAbsolutePath() + "/md.restx.json", appPath.toAbsolutePath() + "/pom.xml");
            }

            if (generateHelloResource) {
                shell.println("generating hello resource ...");
                generate(helloResourceTemplates, appPath, scope);
            }
            shell.printIn("Congratulations! - Your app is now ready in " + appPath.toAbsolutePath(), RestxShell.AnsiCodes.ANSI_GREEN);
            shell.println("");
        }
    }
}
