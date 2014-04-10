package restx.core.shell;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import org.mindrot.jbcrypt.BCrypt;
import restx.factory.Component;
import restx.shell.RestxShell;
import restx.shell.ShellCommandRunner;
import restx.shell.StdShellCommand;

import java.util.List;

/**
 * User: xavierhanin
 * Date: 4/10/13
 * Time: 8:53 PM
 */
@Component
public class HashShellCommand extends StdShellCommand {
    public HashShellCommand() {
        super(ImmutableList.of("hash"), "hash commands, to easily generate password hashes");
    }

    @Override
    protected String resourceMan() {
        return "restx/core/shell/hash.man";
    }

    @Override
    protected Optional<? extends ShellCommandRunner> doMatch(String line) {
        List<String> args = splitArgs(line);

        if (args.size() < 3) {
            return Optional.absent();
        }

        return Optional.of(new HashCommandRunner(args.get(1), args.get(2)));
    }

    @Override
    public Iterable<Completer> getCompleters() {
        return ImmutableList.<Completer>of(new ArgumentCompleter(
                new StringsCompleter("hash"),
                new StringsCompleter("md5", "sha1", "bcrypt", "md5+bcrypt", "sha1+bcrypt")));
    }

    private class HashCommandRunner implements ShellCommandRunner {
        private final String hash;
        private final String plaintext;

        public HashCommandRunner(String hash, String plaintext) {
            this.hash = hash;
            this.plaintext = plaintext;
        }

        @Override
        public void run(RestxShell shell) throws Exception {
            switch (hash) {
                case "md5":
                    shell.println(Hashing.md5().hashString(plaintext, Charsets.UTF_8).toString());
                    break;
                case "sha1":
                    shell.println(Hashing.sha1().hashString(plaintext, Charsets.UTF_8).toString());
                    break;
                case "bcrypt":
                    shell.println(BCrypt.hashpw(plaintext, BCrypt.gensalt()));
                    break;
                case "md5+bcrypt":
                    shell.println(BCrypt.hashpw(Hashing.md5().hashString(plaintext, Charsets.UTF_8).toString(), BCrypt.gensalt()));
                    break;
                case "sha1+bcrypt":
                    shell.println(BCrypt.hashpw(Hashing.sha1().hashString(plaintext, Charsets.UTF_8).toString(), BCrypt.gensalt()));
                    break;
                default:
                    shell.printIn("unknown hash function: " + hash, RestxShell.AnsiCodes.ANSI_RED);
                    shell.println("");
            }
        }
    }
}
