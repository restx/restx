package restx.specs;

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
    private final String title;
    private final ImmutableList<Given> given;
    private final ImmutableList<When> whens;

    public RestxSpec(String title, ImmutableList<Given> given, ImmutableList<When> whens) {
        checkNotNull(title);
        this.title = title;
        this.given = given;
        this.whens = whens;
    }

    /**
     * Stores this recorded spec as a .spec.yaml file.
     *
     * @param path the path where this spec should be stored, relative to restx.recorder.basePath system property
     * @param title the spec title, use the recorded one if absent
     * @return the file where the spec has been stored
     *
     * @throws IOException in case of IO error while saving file.
     */
    public File store(Optional<String> path, Optional<String> title) throws IOException {
        File destFile = getStoreFile(path, title);
        store(destFile, title);
        return destFile;
    }

    public void store(File destFile, Optional<String> title) throws IOException {
        destFile.getParentFile().mkdirs();

        Files.write(withTitle(title.or(getTitle())).toString(),
                destFile, Charsets.UTF_8);
    }

    public File getStoreFile(Optional<String> path, Optional<String> title) {
        String basePath = System.getProperty("restx.recorder.basePath", "src/main/resources/specs");
        return new File(basePath + "/" + path.or("") + "/"
                + title.or(getTitle()).replace(' ', '_').replace('/', '_') + ".spec.yaml");
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

    public String getTitle() {
        return title;
    }

    public RestxSpec withTitle(String title) {
        return new RestxSpec(title, given, whens);
    }

    public ImmutableList<Given> getGiven() {
        return given;
    }

    public ImmutableList<When> getWhens() {
        return whens;
    }

}
