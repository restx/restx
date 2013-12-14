package restx.common;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.google.common.base.Charsets;
import com.google.common.io.InputSupplier;

import java.io.*;
import java.nio.file.Path;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.newReaderSupplier;

/**
 * User: xavierhanin
 * Date: 3/31/13
 * Time: 6:36 PM
 */
public class Mustaches {
    public static Mustache compile(Class relativeTo, String name) {
        return compile(name, newReaderSupplier(getResource(relativeTo, name), Charsets.UTF_8));
    }

    public static Mustache compile(String name) {
        return compile(name, newReaderSupplier(getResource(name), Charsets.UTF_8));
    }

    private static Mustache compile(String name, InputSupplier<InputStreamReader> supplier) {
        try (InputStreamReader reader = supplier.getInput()) {
            return new DefaultMustacheFactory().compile(reader, name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String execute(Mustache mustache, Object scope) {
        StringWriter w = new StringWriter();
        mustache.execute(w, scope);
        w.flush();
        return w.toString();
    }

    public static void execute(Mustache mustache, Object scope, Path path) throws IOException {
        File file = path.toFile();
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new IOException(String.format("can't generate file to `%s`: can't create directory `%s`",
                        file.getAbsolutePath(), file.getParentFile().getAbsolutePath()));
            }
        }
        try (FileWriter w = new FileWriter(file)) {
            mustache.execute(w, scope);
        }
    }
}
