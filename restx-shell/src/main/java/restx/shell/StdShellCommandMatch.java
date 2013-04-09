package restx.shell;

/**
 * User: xavierhanin
 * Date: 4/9/13
 * Time: 10:31 PM
 */
public class StdShellCommandMatch implements ShellCommandMatch {
    private final String line;

    public StdShellCommandMatch(String line) {
        this.line = line;
    }

    public String getLine() {
        return line;
    }
}
