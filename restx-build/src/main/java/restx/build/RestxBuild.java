package restx.build;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 10:49 PM
 */
public class RestxBuild {
    public static interface Parser {
        public ModuleDescriptor parse(Path path) throws IOException;
        public ModuleDescriptor parse(InputStream stream) throws IOException;
    }
    public static interface Generator {
        public void generate(ModuleDescriptor md, Writer w) throws IOException;
        public String getDefaultFileName();
    }

    public static void main(String[] args) throws IOException {
        final PrintStream out = System.out;
        if (args.length != 3 || !args[0].equalsIgnoreCase("convert")) {
            out.println("usage: restx-build convert <from> <to>\n" +
                    "\teg: restx-build convert md.restx.json pom.xml\n\n" +
                    "you can also use **/ syntax to convert a bunch of files:" +
                    "\t    restx-build convert **/md.restx.json module.ivy");
            System.exit(1);
        }

        convert(args[1], args[2]);
        out.println("conversion done.");
    }

    /**
     * Converts one or a bunch of module descriptor to another format.
     *
     * @param from either a module descriptor path, or a path using <code>**</code> notation a la Ant
     * @param to the name of the target module descriptor, relative to where from module descriptors are found
     * @return the list of paths converted
     * @throws IOException
     */
    public static List<Path> convert(String from, final String to) throws IOException {
        final List<Path> converted = new ArrayList<>();
        int idx = from.indexOf("**/");
        if (idx != -1) {
            Path startFrom = Paths.get(from.substring(0, idx));
            final String name = from.substring(idx + "**/".length());
            Files.walkFileTree(startFrom, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isRegularFile() && file.endsWith(name)) {
                        convert(file, file.getParent().resolve(to));
                        converted.add(file);
                    }
                    return CONTINUE;
                }
            });
        } else {
            Path fromPath = Paths.get(from);
            Path toPath = Paths.get(to);

            convert(fromPath, toPath);
            converted.add(fromPath);
        }
        return converted;
    }

    /**
     * Converts one module descriptor to another format.
     *
     * @param fromPath the path of the module descriptor to convert
     * @param toPath the path of the target module descriptor
     * @throws IOException
     */
    public static void convert(Path fromPath, Path toPath) throws IOException {
        Parser parser = guessParserFor(fromPath);
        Generator generator = guessGeneratorFor(toPath);

        try (FileWriter writer = new FileWriter(toPath.toFile())) {
            ModuleDescriptor md = parser.parse(fromPath);
            generator.generate(md, writer);
        }
    }

    private static Generator guessGeneratorFor(Path path) {
        if (path.toString().endsWith(".ivy") || path.endsWith("ivy.xml")) {
            return new IvySupport();
        }
        if (path.endsWith("pom.xml")) {
            return new MavenSupport();
        }
        return new RestxJsonSupport();
    }

    private static Parser guessParserFor(Path path) {
        if (path.endsWith("pom.xml")) {
            return new MavenSupport();
        }
        return new RestxJsonSupport();
    }

    public static List<Path> resolveForeignModuleDescriptorsIn(Path directory) {
        List<Path> possibleForeignModuleDescriptors = Arrays.asList(
                directory.resolve("pom.xml"), directory.resolve("module.ivy"));

        List<Path> existingForeignModuleDescriptors = new ArrayList<>();
        for(Path possibleForeignModuleDescriptor : possibleForeignModuleDescriptors){
            if(Files.exists(possibleForeignModuleDescriptor)) {
                existingForeignModuleDescriptors.add(possibleForeignModuleDescriptor);
            }
        }
        return existingForeignModuleDescriptors;
    }
}
