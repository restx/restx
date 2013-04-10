package restx.shell;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import restx.factory.Factory;
import restx.shell.commands.HelpCommand;

import java.util.Collection;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 4/9/13
 * Time: 9:42 PM
 */
public class RestxShell {
    public static void main(String[] args) throws Exception {
        ConsoleReader consoleReader = new ConsoleReader();
        consoleReader.setPrompt("restx> ");
        consoleReader.setHistoryEnabled(true);
        consoleReader.println("===============================================================================");
        consoleReader.println("== WELCOME TO RESTX SHELL - type `help` for help on available commands");
        consoleReader.println("===============================================================================");

        Factory factory = Factory.builder().addFromServiceLoader().build();

        Set<ShellCommand> commands = factory.queryByClass(ShellCommand.class).findAsComponents();
        HelpCommand helpCommand = new HelpCommand(commands);
        commands = Sets.newLinkedHashSet(commands);
        commands.add(helpCommand);

        for (ShellCommand command : commands) {
            for (Completer completer : command.getCompleters()) {
                consoleReader.addCompleter(completer);
            }
        }

        boolean exit = false;
        while (!exit) {
            String line = consoleReader.readLine();
            boolean found = false;
            for (ShellCommand command : commands) {
                Optional<? extends ShellCommandRunner> match = command.match(line);
                if (match.isPresent()) {
                    String storedPrompt = consoleReader.getPrompt();
                    // store current completers and clean them, so that executing command can perform in a clean env
                    Collection<Completer> storedCompleters = ImmutableList.copyOf(consoleReader.getCompleters());
                    for (Completer completer : storedCompleters) {
                        consoleReader.removeCompleter(completer);
                    }

                    try {
                        exit = match.get().run(consoleReader);
                        found = true;
                    } finally {
                        for (Completer completer : ImmutableList.copyOf(consoleReader.getCompleters())) {
                            consoleReader.removeCompleter(completer);
                        }
                        for (Completer completer : storedCompleters) {
                            consoleReader.addCompleter(completer);
                        }
                        consoleReader.setPrompt(storedPrompt);
                    }
                    break;
                }
            }
            if (!found) {
                consoleReader.println("command not found. use `help` to get the list of commands.");
            }
        }

        consoleReader.println("Bye.");
        consoleReader.shutdown();
    }

}
