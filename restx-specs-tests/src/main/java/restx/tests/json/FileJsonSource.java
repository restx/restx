package restx.tests.json;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Date: 3/2/14
 * Time: 22:08
 */
public class FileJsonSource implements JsonSource {
    private final String name;
    private final File file;
    private final Charset cs;

    public FileJsonSource(File file, Charset cs) {
        this.name = file.getName();
        this.file = file;
        this.cs = cs;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String content() {
        try {
            return Files.toString(file, cs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
