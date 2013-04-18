package restx.core.shell;

import com.github.mustachejava.Mustache;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import restx.common.UUIDGenerator;
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
                AppShellCommand.class, ImmutableMap.<String,String>builder()
                .put("md.restx.json.mustache", "md.restx.json")
                .put("AppModule.java.mustache", "src/main/java/{{packagePath}}/AppModule.java")
                .put("AppServer.java.mustache", "src/main/java/{{packagePath}}/AppServer.java")
                .put("web.xml.mustache", "src/main/webapp/WEB-INF/web.xml")
                .put("logback.xml.mustache", "src/main/resources/logback.xml")
                .build()
        );
        private ImmutableMap<Mustache, String> helloResourceTemplates = buildTemplates(
                AppShellCommand.class, ImmutableMap.<String,String>builder()
                .put("Message.java.mustache", "src/main/java/{{packagePath}}/domain/Message.java")
                .put("HelloResource.java.mustache", "src/main/java/{{packagePath}}/rest/HelloResource.java")
                .put("HelloResourceSpecTest.java.mustache", "src/test/java/{{packagePath}}/rest/HelloResourceSpecTest.java")
                .put("should_say_hello.spec.yaml.mustache", "src/test/resources/specs/hello/should_say_hello.spec.yaml")
                .build()
        );

        @Override
        public void run(RestxShell shell) throws Exception {
            String appName = shell.getConsoleReader().readLine("App name? ");
            String groupId = shell.ask("group id [%s]? ",
                    appName.replaceAll("\\s+", "-").toLowerCase(Locale.ENGLISH));
            String artifactId = shell.ask("artifact id [%s]? ",
                                        appName.replaceAll("\\s+", "-").toLowerCase(Locale.ENGLISH))
                    .replaceAll("\\s+", "-");
            String mainPackage = shell.ask("main package [%s]? ",
                    artifactId.replaceAll("\\-", ".").toLowerCase(Locale.ENGLISH));
            String version = shell.ask("version [%s]? ", "0.1-SNAPSHOT");

            List<String> list = Lists.newArrayList(UUIDGenerator.generate(),
                    String.valueOf(new Random().nextLong()), appName, artifactId);
            Collections.shuffle(list);
            String signatureKey = shell.ask("signature key (to sign cookies) [%s]? ",
                    Joiner.on(" ").join(list));

            String defaultPort = shell.ask("default port [%s]? ", "8080");
            String basePath = shell.ask("base path [%s]? ", "/api");

            String restxVersion = "0.2-SNAPSHOT";

            ImmutableMap scope = ImmutableMap.builder()
                    .put("appName", appName)
                    .put("groupId", groupId)
                    .put("artifactId", artifactId)
                    .put("mainPackage", mainPackage)
                    .put("packagePath", mainPackage.replace('.', '/'))
                    .put("version", version)
                    .put("signatureKey", signatureKey)
                    .put("defaultPort", defaultPort)
                    .put("basePath", basePath)
                    .put("restxVersion", restxVersion)
                    .build();

            boolean generateHelloResource = shell.askBoolean("generate hello resource example [Y/n]? ", "y");

            Path appPath = shell.currentLocation().resolve(artifactId);

            shell.println("scaffolding app to `" + appPath.toAbsolutePath() + "` ...");
            generate(mainTemplates, appPath, scope);

            if (generateHelloResource) {
                generate(helloResourceTemplates, appPath, scope);
            }
        }
    }
}
