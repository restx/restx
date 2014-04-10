package restx.specs;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import restx.common.RestxConfig;
import restx.config.SettingsKey;
import restx.factory.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
* User: xavierhanin
* Date: 3/12/13
* Time: 9:51 PM
*/
public class RestxSpec {
    @Component
    public static class StorageSettingsConfig implements StorageSettings {
        private final RestxConfig config;

        public StorageSettingsConfig(RestxConfig config) {
            this.config = config;
        }

        @Override
        public String recorderBasePath() {
            return config.getString("restx.recorder.basePath").get();
        }

        @Override
        public String recorderBaseSpecPath() {
            return config.getString("restx.recorder.baseSpecPath").get();
        }
    }

    public static interface StorageSettings {
        @SettingsKey(key = "restx.recorder.basePath", defaultValue = "src/main/resources")
        String recorderBasePath();
        @SettingsKey(key = "restx.recorder.baseSpecPath", defaultValue = "specs")
        String recorderBaseSpecPath();
    }

    public static class Storage {
        public static Storage with(StorageSettings settings) {
            return new Storage(settings);
        }

        private final StorageSettings settings;

        private Storage(StorageSettings settings) {
            this.settings = settings;
        }

        public File getStoreFile(String path) {
            if (path.startsWith("/")) {
                return new File(path);
            } else {
                String basePath = settings.recorderBasePath();
                return new File(basePath + "/" + path);
            }
        }

        public File store(RestxSpec spec) throws IOException {
            File storeFile = getStoreFile(spec.getPath());
            spec.store(storeFile);
            return storeFile;
        }

        public String buildPath(Optional<String> dir, String title) {
            return dir.or(settings.recorderBaseSpecPath()) + "/" + title.replace(' ', '_').replace('/', '_') + ".spec.yaml";
        }
    }

    private final String path;
    private final String title;
    private final ImmutableList<? extends Given> given;
    private final ImmutableList<? extends When<?>> whens;

    public RestxSpec(String path, String title,
                     ImmutableList<? extends Given> given, ImmutableList<? extends When<?>> whens) {
        this.path = checkNotNull(path);
        this.title = checkNotNull(title);
        this.given = given;
        this.whens = whens;
    }

    /**
     * Stores this spec as a .spec.yaml file.
     *
     * @throws IOException in case of IO error while saving file.
     */
    public void store(File destFile) throws IOException {
        destFile.getParentFile().mkdirs();

        Files.write(this.toString(),
                destFile, Charsets.UTF_8);
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

    public RestxSpec withWhens(ImmutableList<When<?>> whens) {
        return new RestxSpec(path, title, given, whens);
    }

    public RestxSpec withWhenAt(int index, When when) {
        List<When<?>> whens = new ArrayList<>(getWhens());
        whens.set(index, when);
        return withWhens(ImmutableList.copyOf(whens));
    }

    public ImmutableList<? extends Given> getGiven() {
        return given;
    }

    public ImmutableList<? extends When<?>> getWhens() {
        return whens;
    }
}
