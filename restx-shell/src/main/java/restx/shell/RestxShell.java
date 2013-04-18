package restx.shell;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import restx.factory.Factory;
import restx.shell.commands.HelpCommand;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * User: xavierhanin
 * Date: 4/9/13
 * Time: 9:42 PM
 */
public class RestxShell {
    public static final class ExitShell extends RuntimeException { }

    private final ConsoleReader consoleReader;
    private final Factory factory;
    private final ImmutableSet<ShellCommand> commands;

    public RestxShell(ConsoleReader consoleReader) {
        this(consoleReader, Factory.builder().addFromServiceLoader().build());
    }

    public RestxShell(ConsoleReader consoleReader, Factory factory) {
        this.consoleReader = consoleReader;
        this.factory = factory;

        this.commands = ImmutableSet.copyOf(findCommands());

        initConsole(consoleReader);
    }

    public ConsoleReader getConsoleReader() {
        return consoleReader;
    }

    public Factory getFactory() {
        return factory;
    }

    public ImmutableSet<ShellCommand> getCommands() {
        return commands;
    }

    public void start() throws IOException {
        banner();

        installCompleters();

        boolean exit = false;
        while (!exit) {
            String line = consoleReader.readLine();
            exit = exec(line);
        }

        consoleReader.println("Bye.");
        consoleReader.shutdown();
    }

    protected void initConsole(ConsoleReader consoleReader) {
        consoleReader.setPrompt("restx> ");
        consoleReader.setHistoryEnabled(true);
    }

    protected void banner() throws IOException {
        consoleReader.println("===============================================================================");
        consoleReader.println("== WELCOME TO RESTX SHELL - type `help` for help on available commands");
        consoleReader.println("===============================================================================");
    }

    protected boolean exec(String line) throws IOException {
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
                    match.get().run(consoleReader);
                    found = true;
                } catch (ExitShell e) {
                    return true;
                } catch (Exception e) {
                    consoleReader.println("command " + line + " raised an exception: " + e.getMessage());
                    e.printStackTrace();
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
        return false;
    }

    protected void installCompleters() {
        for (ShellCommand command : commands) {
            for (Completer completer : command.getCompleters()) {
                consoleReader.addCompleter(completer);
            }
        }
    }

    protected Set<ShellCommand> findCommands() {
        Set<ShellCommand> commands = factory.queryByClass(ShellCommand.class).findAsComponents();
        HelpCommand helpCommand = new HelpCommand(commands);
        commands = Sets.newLinkedHashSet(commands);
        commands.add(helpCommand);
        return commands;
    }


    public static void main(String[] args) throws Exception {
        ConsoleReader consoleReader = new ConsoleReader();
        new RestxShell(consoleReader).start();
    }

}
