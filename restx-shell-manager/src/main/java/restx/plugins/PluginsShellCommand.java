package restx.plugins;

import com.google.common.base.*;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import org.joda.time.DateTime;
import restx.factory.Component;
import restx.shell.RestxShell;
import restx.shell.ShellCommandRunner;
import restx.shell.ShellIvy;
import restx.shell.StdShellCommand;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;

/**
 * User: xavierhanin
 * Date: 5/4/13
 * Time: 2:32 PM
 */
@Component
public class PluginsShellCommand extends StdShellCommand {
    /*
     * this is the default exlcusions list used when fetching plugins (to avoid confusion between lib and plugins dir):
     * - we exclude the main modules already part of the shell itself (shell and shell-manager)
     * - we exclude logback-classic, to avoid a SLF4J warning at startup if multiple bindings are present
     */
    private List<String> defaultExcludes = ImmutableList.of(
            "io.restx:restx-shell",
            "io.restx:restx-shell-manager",
            "ch.qos.logback:logback-classic");

    public PluginsShellCommand() {
        super(ImmutableList.of("shell"), "manages the shell itself: install / update plugins, upgrade restx shell version");
    }

    @Override
    protected Optional<? extends ShellCommandRunner> doMatch(String line) {
        final List<String> args = splitArgs(line);
        if (args.size() < 2) {
            return Optional.absent();
        }
        switch (args.get(1)) {
            case "install":
                return Optional.<ShellCommandRunner>of(new InstallPluginRunner());
            case "upgrade":
                return Optional.<ShellCommandRunner>of(new UpgradeShellRunner());
        }

        return Optional.absent();
    }

    @Override
    public Iterable<Completer> getCompleters() {
        return ImmutableList.<Completer>of(
                new ArgumentCompleter(new StringsCompleter("shell"), new StringsCompleter("install", "upgrade")));
    }

    private class InstallPluginRunner implements ShellCommandRunner {
        @Override
        public void run(RestxShell shell) throws Exception {
            ModulesManager modulesManager = new ModulesManager(
                    new URL("http://restx.io/modules"), ShellIvy.loadIvy(shell));

            shell.println("looking for plugins...");
            List<ModuleDescriptor> plugins = modulesManager.searchModules("category=shell");
            shell.printIn("found " + plugins.size() + " available plugins", RestxShell.AnsiCodes.ANSI_CYAN);
            shell.println("");

            for (int i = 0; i < plugins.size(); i++) {
                ModuleDescriptor plugin = plugins.get(i);
                shell.printIn(String.format(" [%3d] %s%n", i + 1, plugin.getId()), RestxShell.AnsiCodes.ANSI_PURPLE);
                shell.println("\t\t" + plugin.getDescription());
            }

            String sel = shell.ask("Which plugin would you like to install (eg '1 3 5')? \nYou can also provide a plugin id in the form <groupId>:<moduleId>:<version>\n plugin to install: ", "");
            Iterable<String> selected = Splitter.on(" ").trimResults().omitEmptyStrings().split(sel);
            File pluginsDir = new File(shell.installLocation().toFile(), "plugins");
            int count = 0;
            for (String s : selected) {
                ModuleDescriptor md;
                if (CharMatcher.DIGIT.matchesAllOf(s)) {
                    int i = Integer.parseInt(s);
                    md = plugins.get(i - 1);
                } else {
                    md = new ModuleDescriptor(s, "shell", "");
                }
                shell.printIn("installing " + md.getId() + "...", RestxShell.AnsiCodes.ANSI_CYAN);
                shell.println("");
                try {
                    List<File> copied = modulesManager.download(ImmutableList.of(md), pluginsDir, defaultExcludes);
                    if (!copied.isEmpty()) {
                        shell.printIn("installed " + md.getId(), RestxShell.AnsiCodes.ANSI_GREEN);
                        shell.println("");
                        count++;
                        Files.write(md.getId() + "\n"
                                + DateTime.now() + "\n"
                                + Joiner.on("\n").join(copied),
                                pluginFile(pluginsDir, md), Charsets.UTF_8);
                    } else {
                        shell.printIn("problem while installing " + md.getId(), RestxShell.AnsiCodes.ANSI_RED);
                        shell.println("");
                    }
                } catch (IOException e) {
                    shell.printIn("IO problem while installing " + md.getId() + "\n" + e.getMessage(), RestxShell.AnsiCodes.ANSI_RED);
                    shell.println("");
                } catch (IllegalStateException e) {
                    shell.printIn(e.getMessage(), RestxShell.AnsiCodes.ANSI_RED);
                    shell.println("");
                }
            }
            if (count > 0) {
                shell.printIn("installed " + count + " plugins, restarting shell to take them into account", RestxShell.AnsiCodes.ANSI_GREEN);
                shell.println("");

                shell.restart();
            } else {
                shell.println("no plugin installed");
            }
        }
    }

    private File pluginFile(File pluginsDir, ModuleDescriptor md) {
        return new File(pluginsDir, md.getModuleId() + ".plugin");
    }

    private class UpgradeShellRunner implements ShellCommandRunner {
        @Override
        public void run(RestxShell shell) throws Exception {
            shell.println("checking for upgrade of restx shell...");

            try (Reader reader = new InputStreamReader(new URL("http://restx.io/version").openStream(), Charsets.UTF_8)) {
                List<String> parts = CharStreams.readLines(reader);
                if (parts.size() < 2) {
                    shell.printIn(
                            "unexpected content at http://restx.io/version, try again later or contact the group.\n",
                            RestxShell.AnsiCodes.ANSI_RED);
                    shell.println("content: ");
                    shell.println(Joiner.on("\n").join(parts));
                    return;
                }

                String version = parts.get(0);
                String url = parts.get(1);

                if (!version.equals(shell.version())) {
                    shell.printIn("upgrading to " + version, RestxShell.AnsiCodes.ANSI_GREEN);
                    shell.println("");
                    shell.println("please wait while downloading new version, this may take a while...");

                    boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
                    String archiveExt = isWindows ? ".zip" : ".tar.gz";
                    String scriptExt = isWindows ? ".bat" : ".sh";

                    URL source = new URL(url + archiveExt);
                    shell.download(source, shell.installLocation().resolve("upgrade" + archiveExt).toFile());

                    ByteStreams.copy(PluginsShellCommand.class.getResourceAsStream("upgrade" + scriptExt),
                            Files.newOutputStreamSupplier(shell.installLocation().resolve("upgrade" + scriptExt).toFile()));

                    shell.println("downloaded version " + version + ", restarting");
                    shell.restart();
                }
            }
        }
    }
}
