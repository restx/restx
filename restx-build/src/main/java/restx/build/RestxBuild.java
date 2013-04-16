package restx.build;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 10:49 PM
 */
public class RestxBuild {
    public static interface Parser {
        public ModuleDescriptor parse(InputStream stream) throws IOException;
    }
    public static interface Generator {
        public void generate(ModuleDescriptor md, Writer w) throws IOException;
        public String getDefaultFileName();
    }

    // don't want to introduce a dependency just for that
    public static String toString(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Usage: restx-build module.restx.json pom.xml
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3 || !args[0].equalsIgnoreCase("convert")) {
            System.out.println("usage: restx-build convert <from> <to>\n" +
                    "\teg: restx-build convert module.restx.json pom.xml");
            System.exit(1);
        }

        Path from = Paths.get(args[1]);
        Path to = Paths.get(args[2]);

        Parser parser = guessParserFor(from);
        Generator generator = guessGeneratorFor(to);

        System.out.println("using parser:    " + parser.getClass().getSimpleName());
        System.out.println("using generator: " + generator.getClass().getSimpleName());

        try (FileInputStream inputStream = new FileInputStream(from.toFile());
                FileWriter writer = new FileWriter(to.toFile())) {
            ModuleDescriptor md = parser.parse(inputStream);
            generator.generate(md, writer);
        }
        System.out.println("conversion done.");
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
}
