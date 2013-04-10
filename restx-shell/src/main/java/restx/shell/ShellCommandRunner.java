package restx.shell;

import jline.console.ConsoleReader;

import java.io.IOException;

/**
 * User: xavierhanin
 * Date: 4/9/13
 * Time: 10:07 PM
 */
public interface ShellCommandRunner {
    boolean run(ConsoleReader reader) throws Exception;
}
