package restx.shell;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import restx.factory.Factory;
import restx.shell.commands.HelpCommand;

import java.util.Set;

/**
 * User: xavierhanin
 * Date: 4/9/13
 * Time: 9:42 PM
 */
public class RestxShell {
    public static void main(String[] args) throws Exception {
        ConsoleReader consoleReader = new ConsoleReader();
        consoleReader.setPrompt("rx> ");
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
                Optional<ShellCommandMatch> match = command.match(consoleReader, line);
                if (match.isPresent()) {
                    exit = command.run(consoleReader, match.get());
                    found = true;
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
