package restx.specs.shell;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import restx.factory.Component;
import restx.server.simple.simple.SimpleWebServer;
import restx.shell.ShellCommandRunner;
import restx.shell.StdShellCommand;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * User: xavierhanin
 * Date: 4/10/13
 * Time: 4:27 PM
 */
@Component
public class SpecsShellCommand extends StdShellCommand {
    protected SpecsShellCommand() {
        super(ImmutableList.of("spec"), "restx spec commands: list, server, ... ");
    }

    @Override
    protected Optional<ShellCommandRunner> doMatch(String line) {
        final List<String> args = splitArgs(line);
        if (args.size() < 2) {
            return Optional.absent();
        }

        switch (args.get(1)) {
            case "server":
                return Optional.<ShellCommandRunner>of(new SpecServerCommandRunner(args));
        }

        return Optional.absent();
    }

    @Override
    public Iterable<Completer> getCompleters() {
        return ImmutableList.<Completer>of(
                new ArgumentCompleter(new StringsCompleter("spec"), new StringsCompleter("server")));
    }



    private class SpecServerCommandRunner implements ShellCommandRunner {
        private final List<String> args;

        public SpecServerCommandRunner(List<String> args) {
            this.args = args;
        }

        @Override
        public boolean run(final ConsoleReader consoleReader) throws Exception {
            final String routerPath;
            if (args.size() > 2) {
                routerPath = args.get(2);
            } else {
                routerPath = "/api";
            }
            int port = 8888;
            if (args.size() > 3) {
                port = Integer.parseInt(args.get(3));
            }

            printIn(consoleReader, "LAUNCHING SPECS SERVER on port " + port + "...\n", AnsiCodes.ANSI_GREEN);
            consoleReader.println("type `stop` to stop the server, `help` to get help on available commands");

            System.setProperty("restx.factory.load", "onrequest");
            final SimpleWebServer webServer = new SimpleWebServer(routerPath, ".", port);
            webServer.startAndAwait();

            openSubSession(consoleReader,
                    "spec-server> ",
                    ImmutableList.of(new StringsCompleter("stop", "open", "help")),
                    new Callable<Object>() {
                        public Object call() throws Exception {
                            boolean exit = false;
                            while (!exit) {
                                String line = consoleReader.readLine().trim();
                                switch (line) {
                                    case "stop":
                                        exit = stop(consoleReader, webServer);
                                        break;
                                    case "open":
                                        openInBrowser(consoleReader, webServer.baseUrl() + routerPath);
                                        break;
                                    case "help":
                                        help(consoleReader);
                                        break;
                                    default:
                                        consoleReader.println(
                                                "command not found. use `help` to get the list of commands.");
                                }
                            }
                            return null;
                        }
                    });

            return false;
        }

        private void openInBrowser(ConsoleReader consoleReader, String uri) throws IOException {
            try {
                Desktop.getDesktop().browse(new URI(uri));
            } catch (UnsupportedOperationException e) {
                printIn(consoleReader, "can't open browser: " + e.getMessage(), AnsiCodes.ANSI_RED);
            } catch (IOException e) {
                printIn(consoleReader, "can't open browser: " + e.getMessage(), AnsiCodes.ANSI_RED);
            } catch (URISyntaxException e) {
                printIn(consoleReader, "can't open browser: " + e.getMessage(), AnsiCodes.ANSI_RED);
            }
        }

        private boolean stop(ConsoleReader consoleReader, SimpleWebServer webServer) {
            boolean exit;
            try {
                consoleReader.println("stopping server...");
                webServer.stop();
                exit = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return exit;
        }

        private void help(ConsoleReader consoleReader) throws IOException {
            printIn(consoleReader, "stop", AnsiCodes.ANSI_GREEN);
            consoleReader.println(" - to stop the server");

            printIn(consoleReader, "open", AnsiCodes.ANSI_GREEN);
            consoleReader.println(" - open a browser on the spec server");

            printIn(consoleReader, "help", AnsiCodes.ANSI_GREEN);
            consoleReader.println(" - this help");

            consoleReader.println("");
            consoleReader.println("to add new routes simply create/edit .spec.yaml file in\n" +
                                  "current directory or subdirectories.");
        }
    }
}
