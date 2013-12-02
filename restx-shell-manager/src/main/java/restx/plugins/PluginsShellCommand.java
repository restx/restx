package restx.plugins;

import com.google.common.base.*;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.PatternFilenameFilter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;
import restx.shell.RestxShell;
import restx.shell.ShellCommandRunner;
import restx.shell.ShellIvy;
import restx.shell.StdShellCommand;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * User: xavierhanin
 * Date: 5/4/13
 * Time: 2:32 PM
 */
@Component
public class PluginsShellCommand extends StdShellCommand {
    private static final Logger logger = LoggerFactory.getLogger(PluginsShellCommand.class);

    /*
     * this is the default exlcusions list used when fetching plugins (to avoid confusion between lib and plugins dir):
     * - we exclude the main modules already part of the shell itself (shell and shell-manager)
     * - we exclude logback-classic, to avoid a SLF4J warning at startup if multiple bindings are present
     */
    private static final List<String> defaultExcludes = ImmutableList.of(
            "io.restx:restx-shell",
            "io.restx:restx-shell-manager",
            "ch.qos.logback:logback-classic");

    private static final ModulesManager.DownloadOptions defaultDownloadOptions = new ModulesManager.DownloadOptions.Builder().exclusions(defaultExcludes).build();

    public PluginsShellCommand() {
        super(ImmutableList.of("shell"), "manages the shell itself: install / update plugins, upgrade restx shell version");
    }

    @Override
    protected String resourceMan() {
        return "restx/plugins/shell.man";
    }

    @Override
    protected Optional<? extends ShellCommandRunner> doMatch(String line) {
        final List<String> args = splitArgs(line);
        if (args.size() < 2) {
            return Optional.absent();
        }
        switch (args.get(1)) {
            case "install":
                return Optional.<ShellCommandRunner>of(new InstallPluginRunner(args));
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

    @Override
    public void start(RestxShell shell) throws IOException {
        File versionFile = new File(shell.installLocation().toFile(), shell.version());
        if (versionFile.exists()) {
            // upgrade check already done
            return;
        }

        try {
            // upgrading to a new version, we check if plugins need to be upgraded
            File[] pluginFiles = pluginFiles(pluginsDir(shell));

            if (pluginFiles.length == 0) {
                // no plugin installed, nothing to upgrade
                return;
            }

            shell.printIn("upgrading to " + shell.version() + " ...", RestxShell.AnsiCodes.ANSI_YELLOW);
            shell.println("");

            ModulesManager modulesManager = new ModulesManager(
                    new URL("http://restx.io/modules"), ShellIvy.loadIvy(shell));

            List<ModuleDescriptor> plugins = modulesManager.searchModules("category=shell");

            Set<String> allJars = new LinkedHashSet<>();
            Set<String> keepJars = new LinkedHashSet<>();
            List<ModuleDescriptor> pluginsToInstall = new ArrayList<>();
            List<String> unmatchedPlugins = new ArrayList<>();
            for (File pluginFile : pluginFiles) {
                try {
                    List<String> desc = Files.readLines(pluginFile, Charsets.UTF_8);
                    String id = desc.get(0);
                    ModuleRevisionId mrid = ModulesManager.toMrid(id);
                    List<String> jars = desc.subList(2, desc.size());


                    allJars.addAll(jars);

                    ModuleDescriptor matchingModule = findMatchingPlugin(plugins, mrid);

                    if (matchingModule == null) {
                        keepJars.addAll(jars);
                        unmatchedPlugins.add(id);
                    } else if (ModulesManager.toMrid(matchingModule.getId()).getRevision()
                                        .equals(mrid.getRevision())) {
                        // up to date
                        keepJars.addAll(jars);
                    } else {
                        pluginsToInstall.add(matchingModule);
                    }
                } catch (Exception e) {
                    shell.printIn("error while parsing plugin file " + pluginFile + ": " + e, RestxShell.AnsiCodes.ANSI_RED);
                    shell.println("");
                }
            }

            if (!unmatchedPlugins.isEmpty()) {
                shell.printIn("found unmanaged installed plugins, they won't be upgraded automatically:\n"
                        + Joiner.on("\n").join(unmatchedPlugins), RestxShell.AnsiCodes.ANSI_YELLOW);
                shell.println("");
            }

            Set<String> jarsToRemove = new LinkedHashSet<>();
            jarsToRemove.addAll(allJars);
            jarsToRemove.removeAll(keepJars);

            for (String jarToRemove : jarsToRemove) {
                logger.debug("removing {}", jarToRemove);
                new File(jarToRemove).delete();
            }

            if (!pluginsToInstall.isEmpty()) {
                int count = 0;
                shell.println("found " + pluginsToInstall.size() + " plugins to upgrade");
                for (ModuleDescriptor md : pluginsToInstall) {
                    if (installPlugin(shell, modulesManager, pluginsDir(shell), md)) {
                        count++;
                    }
                }
                if (count > 0) {
                    shell.println("upgraded " + count + " plugins, restarting shell");
                    shell.restart();
                }
            }
        } finally {
            Files.write(DateTime.now().toString(), versionFile, Charsets.UTF_8);
        }
    }

    private ModuleDescriptor findMatchingPlugin(List<ModuleDescriptor> plugins, ModuleRevisionId mrid) {
        ModuleDescriptor matchingModule = null;
        for (ModuleDescriptor plugin : plugins) {
            ModuleRevisionId pluginId = ModulesManager.toMrid(plugin.getId());
            if (pluginId.getModuleId().equals(mrid.getModuleId())) {
                matchingModule = plugin;
                break;
            }
        }
        return matchingModule;
    }

    private class InstallPluginRunner implements ShellCommandRunner {
        private final Optional<List<String>> pluginIds;

        public InstallPluginRunner(List<String> args) {
            if (args.size() > 2) {
                pluginIds = Optional.<List<String>>of(new ArrayList<>(args.subList(2, args.size())));
            } else {
                pluginIds = Optional.absent();
            }
        }

        @Override
        public void run(RestxShell shell) throws Exception {
            ModulesManager modulesManager = new ModulesManager(
                    new URL("http://restx.io/modules"), ShellIvy.loadIvy(shell));

            shell.println("looking for plugins...");
            List<ModuleDescriptor> plugins = modulesManager.searchModules("category=shell");

            Iterable<String> selected = null;
            if (!pluginIds.isPresent()) {
                shell.printIn("found " + plugins.size() + " available plugins", RestxShell.AnsiCodes.ANSI_CYAN);
                shell.println("");

                for (int i = 0; i < plugins.size(); i++) {
                    ModuleDescriptor plugin = plugins.get(i);
                    shell.printIn(String.format(" [%3d] %s%n", i + 1, plugin.getId()), RestxShell.AnsiCodes.ANSI_PURPLE);
                    shell.println("\t\t" + plugin.getDescription());
                }

                String sel = shell.ask("Which plugin would you like to install (eg '1 3 5')? \nYou can also provide a plugin id in the form <groupId>:<moduleId>:<version>\n plugin to install: ", "");
                selected = Splitter.on(" ").trimResults().omitEmptyStrings().split(sel);
            } else {
                selected = pluginIds.get();
            }

            File pluginsDir = pluginsDir(shell);
            int count = 0;
            for (String s : selected) {
                ModuleDescriptor md;
                if (CharMatcher.DIGIT.matchesAllOf(s)) {
                    int i = Integer.parseInt(s);
                    md = plugins.get(i - 1);
                } else {
                    md = new ModuleDescriptor(s, "shell", "");
                }
                if (installPlugin(shell, modulesManager, pluginsDir, md)) {
                    count++;
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

    private boolean installPlugin(RestxShell shell, ModulesManager modulesManager, File pluginsDir, ModuleDescriptor md) throws IOException {
        shell.printIn("installing " + md.getId() + "...", RestxShell.AnsiCodes.ANSI_CYAN);
        shell.println("");
        try {
            List<File> copied = modulesManager.download(ImmutableList.of(md), pluginsDir, defaultDownloadOptions);
            if (!copied.isEmpty()) {
                shell.printIn("installed " + md.getId(), RestxShell.AnsiCodes.ANSI_GREEN);
                shell.println("");
                Files.write(md.getId() + "\n"
                        + DateTime.now() + "\n"
                        + Joiner.on("\n").join(copied),
                        pluginFile(pluginsDir, md), Charsets.UTF_8);
                return true;
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
        return false;
    }

    private File pluginsDir(RestxShell shell) {
        return new File(shell.installLocation().toFile(), "plugins");
    }

    private File pluginFile(File pluginsDir, ModuleDescriptor md) {
        return new File(pluginsDir, md.getModuleId() + ".plugin");
    }

    private File[] pluginFiles(File pluginsDir) {
        File[] files = pluginsDir.listFiles(new PatternFilenameFilter(".*\\.plugin"));
        return files == null ? new File[0] : files;
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
