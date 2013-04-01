package restx.common;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.google.common.base.Charsets;
import com.google.common.io.InputSupplier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.newReaderSupplier;

/**
 * User: xavierhanin
 * Date: 3/31/13
 * Time: 6:36 PM
 */
public class Mustaches {
    public static Mustache compile(Class relativeTo, String name) {
        InputSupplier<InputStreamReader> supplier = newReaderSupplier(getResource(relativeTo, name), Charsets.UTF_8);
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
}
