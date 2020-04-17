package restx.apidocs.doclet;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

/**
 * Date: 19/5/14
 * Time: 21:39
 */
public class ApidocsDocletRunner {
    private Path targetDir;
    private Collection<Path> sources = new ArrayList<>();

    public ApidocsDocletRunner setTargetDir(final Path targetDir) {
        this.targetDir = targetDir;
        return this;
    }

    public ApidocsDocletRunner addSources(Path sources) {
        this.sources.add(sources);
        return this;
    }


    public void run() {
        List<String> javadocargs = new ArrayList<>();
        String javaSpecificationVersion = System.getProperty("java.specification.version");

        if (isLegacyDocletAvailable(javaSpecificationVersion)) {
            javadocargs.addAll(asList("-d", targetDir.toAbsolutePath().toString()));
        }

        javadocargs.addAll(asList("-doclet", "restx.apidocs.doclet.ApidocsDoclet",
                    "-restx-target-dir", targetDir.toAbsolutePath().toString(),
                    "-disable-standard-doclet",
                    "-quiet"));

        for (Path source : sources) {
            javadocargs.add(source.toAbsolutePath().toString());
        }


        com.sun.tools.javadoc.Main.execute(javadocargs.toArray(new String[javadocargs.size()]));
    }

    public ApidocsDocletRunner addSources(Collection<Path> sources) {
        this.sources.addAll(sources);
        return this;
    }

    boolean isLegacyDocletAvailable(String javaSpecificationVersion) {
        return Objects.nonNull(javaSpecificationVersion) && javaSpecificationVersion.startsWith("1.") ||
                "9".equals(javaSpecificationVersion);
    }
}
