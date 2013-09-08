package restx.specs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
* User: xavierhanin
* Date: 3/12/13
* Time: 9:51 PM
*/
public class RestxSpec {
    private final String path;
    private final String title;
    private final ImmutableList<Given> given;
    private final ImmutableList<When> whens;

    public RestxSpec(String title, ImmutableList<Given> given, ImmutableList<When> whens) {
        this(buildPath(Optional.<String>absent(), title), title, given, whens);
    }

    public RestxSpec(String path, String title, ImmutableList<Given> given, ImmutableList<When> whens) {
        this.path = checkNotNull(path);
        this.title = checkNotNull(title);
        this.given = given;
        this.whens = whens;
    }

    /**
     * Stores this spec as a .spec.yaml file.
     *
     * @return the file where the spec has been stored
     *
     * @throws IOException in case of IO error while saving file.
     */
    public File store() throws IOException {
        File destFile = getStoreFile();
        store(destFile);
        return destFile;
    }

    public void store(File destFile) throws IOException {
        destFile.getParentFile().mkdirs();

        Files.write(this.toString(),
                destFile, Charsets.UTF_8);
    }

    @JsonIgnore
    public File getStoreFile() {
        String basePath = System.getProperty("restx.recorder.basePath", "src/main/resources");
        return new File(basePath + "/" + path);
    }

    public static String buildPath(Optional<String> dir, String title) {
        return dir.or(System.getProperty("restx.recorder.baseSpecPath", "specs")) + "/" + title.replace(' ', '_').replace('/', '_') + ".spec.yaml";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("title: ").append(title).append("\n");
        if (!given.isEmpty()) {
            sb.append("given:\n");
            for (Given g : given) {
                g.toString(sb);
            }
        }
        sb.append("wts:\n");
        for (When when : whens) {
            when.toString(sb);
        }
        return sb.toString();
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public RestxSpec withTitle(String title) {
        return new RestxSpec(path, title, given, whens);
    }

    public RestxSpec withTitle(Optional<String> title) {
        if (title.isPresent()) {
            return withTitle(title.get());
        } else {
            return this;
        }
    }

    public RestxSpec withPath(String path) {
        return new RestxSpec(path, title, given, whens);
    }

    public RestxSpec withPath(Optional<String> path) {
        if (path.isPresent()) {
            return withPath(path.get());
        } else {
            return this;
        }
    }


    public ImmutableList<Given> getGiven() {
        return given;
    }

    public ImmutableList<When> getWhens() {
        return whens;
    }

}
