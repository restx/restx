package restx.shell;

import jline.console.ConsoleReader;

/**
 * User: xavierhanin
 * Date: 4/9/13
 * Time: 10:07 PM
 */
public interface ShellCommandRunner {
    void run(ConsoleReader reader) throws Exception;
}
