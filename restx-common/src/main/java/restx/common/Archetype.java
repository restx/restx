package restx.common;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.io.Files.asByteSink;
import static com.google.common.io.Files.asByteSource;
import static com.google.common.io.Files.asCharSource;

/**
 * Date: 6/1/14
 * Time: 23:27
 */
public class Archetype {
    public static Archetype buildArchetype(String packageName) {
        Map<String,URL> resources = MoreResources.findResources(packageName, Pattern.compile(".*"), true);

        ImmutableList.Builder<ArchetypeEntry> builder = ImmutableList.builder();
        for (String r : resources.keySet()) {
            if (r.substring(r.lastIndexOf('/') + 1).startsWith("_")) {
                builder.add(buildTemplate(packageName.replace('.', '/') + '/', r.substring(packageName.length() + 1)));
            } else {
                builder.add(new StaticArchetypeEntry(r.substring(packageName.length() + 1),
                        Resources.asByteSource(Resources.getResource(r))));
            }
        }
        return new Archetype(builder.build());
    }

    public static Archetype buildArchetype(final Path basePath) {
        try {
            final ImmutableList.Builder<ArchetypeEntry> builder = ImmutableList.builder();
            Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String name = basePath.relativize(file).toString();
                    if (file.getFileName().toString().startsWith("_")) {
                        builder.add(buildTemplate(name, asCharSource(file.toFile(), Charsets.UTF_8)));
                    } else {
                        builder.add(new StaticArchetypeEntry(name, asByteSource(file.toFile())));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return new Archetype(builder.build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static TemplateArchetypeEntry buildTemplate(String basePath, String tpl) {
        return new TemplateArchetypeEntry(Mustaches.compile(basePath + tpl),
                tpl
                        .replaceAll("^_([^/]+$)", "$1")
                        .replaceAll("/_([^/]+$)", "/$1")
                        .replaceAll("\\$([^_]+)\\$", "\\{\\{$1\\}\\}"));
    }

    private static TemplateArchetypeEntry buildTemplate(String tplPath,
                                                        CharSource charSource) {
        return new TemplateArchetypeEntry(Mustaches.compile(tplPath, charSource),
                tplPath
                        .replaceAll("^_([^/]+$)", "$1")
                        .replaceAll("/_([^/]+$)", "/$1")
                        .replaceAll("\\$([^_]+)\\$", "\\{\\{$1\\}\\}"));
    }

    public abstract static class ArchetypeEntry {
        public abstract void generate(Path path, Object scope) throws IOException;

        protected Path resolvePath(Path path, String relative, Object scope) {
            return path.resolve(Mustaches.execute(
                    Mustache.compiler().escapeHTML(false).compile(relative), scope));
        }

    }

    public static class StaticArchetypeEntry extends ArchetypeEntry {
        private final String path;
        private final ByteSource byteSource;

        public StaticArchetypeEntry(String path, ByteSource byteSource) {
            this.path = path;
            this.byteSource = byteSource;
        }

        @Override
        public void generate(Path path, Object scope) throws IOException {
            File dest = resolvePath(path, this.path, scope).toFile();
            if (dest.getParentFile() != null && !dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            byteSource.copyTo(asByteSink(dest));
        }
    }

    public static class TemplateArchetypeEntry extends ArchetypeEntry {
        private final Template template;
        private final String path;
        public TemplateArchetypeEntry(Template template, String path) {
            this.template = template;
            this.path = path;
        }

        @Override
        public void generate(Path path, Object scope) throws IOException {
            Mustaches.execute(template, scope, resolvePath(path, this.path, scope));
        }
    }

    private final ImmutableList<ArchetypeEntry> entries;

    private Archetype(ImmutableList<ArchetypeEntry> entries) {
        this.entries = entries;
    }

    public void generate(Path path, Object scope) throws IOException {
        for (ArchetypeEntry entry : entries) {
            entry.generate(path, scope);
        }
    }
}
