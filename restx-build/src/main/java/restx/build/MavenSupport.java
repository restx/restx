package restx.build;

import restx.build.org.json.JSONArray;
import restx.build.org.json.JSONObject;
import restx.build.org.json.XML;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 2:08 PM
 */
public class MavenSupport implements RestxBuild.Parser, RestxBuild.Generator {
    static class Parser {
        public ModuleDescriptor parse(InputStream stream) throws IOException {
            JSONObject jsonObject = XML.toJSONObject(RestxBuildHelper.toString(stream)).getJSONObject("project");

            GAV parent;
            if (jsonObject.has("parent")) {
                JSONObject parentObject = jsonObject.getJSONObject("parent");
                // No packaging type is allowed on <parent> tag
                parent = getGav(parentObject, null, null);
            } else {
                parent = null;
            }
            // Packaging is defined with <packaging> tag on artefact definition
            GAV gav = getGav(jsonObject, "packaging", parent);
            String packaging = jsonObject.has("packaging") ? jsonObject.getString("packaging") : "jar";

            Map<String, String> properties = new LinkedHashMap<>();
            if (jsonObject.has("properties")) {
                JSONObject props = jsonObject.getJSONObject("properties");
                for (Object o : props.keySet()) {
                    String p = (String) o;

                    if (p.equals("maven.compiler.target") || p.equals("maven.compiler.source")) {
                        properties.put("java.version", String.valueOf(props.get(p)));
                    } else {
                        properties.put(p, String.valueOf(props.get(p)));
                    }
                }
            }

            Map<String, List<ModuleDependency>> dependencies = new LinkedHashMap<>();

            if (jsonObject.has("dependencies")) {
                JSONArray deps = jsonObject.getJSONObject("dependencies").getJSONArray("dependency");

                for (int i = 0; i < deps.length(); i++) {
                    JSONObject dep = deps.getJSONObject(i);
                    String scope = dep.has("scope") ? dep.getString("scope") : "compile";

                    List<ModuleDependency> scopeDependencies = dependencies.get(scope);
                    if (scopeDependencies == null) {
                        dependencies.put(scope, scopeDependencies = new ArrayList<>());
                    }

                    // Packaging is defined with <type> tag on dependencies
                    scopeDependencies.add(new ModuleDependency(getGav(dep, "type", null)));
                }
            }

            return new ModuleDescriptor(parent, gav, packaging,
                    properties, new HashMap<String,List<ModuleFragment>>(), dependencies);
        }

        private GAV getGav(JSONObject jsonObject, String typeKey, GAV parentGAV) {
            return new GAV(
                    jsonObject.has("groupId")?jsonObject.getString("groupId"):parentGAV==null?null:parentGAV.getGroupId(),
                    jsonObject.has("artifactId")?jsonObject.getString("artifactId"):parentGAV==null?null:parentGAV.getArtifactId(),
                    jsonObject.has("version")?String.valueOf(jsonObject.get("version")):parentGAV==null?null:parentGAV.getVersion(),
                    typeKey==null?null:jsonObject.has(typeKey)?jsonObject.getString(typeKey):parentGAV==null?null:parentGAV.getType(),
                    jsonObject.has("classifier")?jsonObject.getString("classifier"):null,
                    jsonObject.has("optional")?jsonObject.getBoolean("optional"):false);
        }
    }
    static class Generator {
        private static final Pattern PLUGIN_PATTERN = Pattern.compile("^\\s*\\Q<plugin>\\E.+\\Q</plugin>\\E\\s*$", Pattern.DOTALL);

        public void generate(ModuleDescriptor md, Writer w) throws IOException {
            w.write(HEADER);

            if (md.getParent() != null) {
                w.write("    <parent>\n");
                toMavenGAV(md.getParent(), "        ", w);
                w.write("    </parent>\n\n");
            }

            toMavenGAV(md.getGav(), "    ", w);
            writeXmlTag(w, "    ", "packaging", md.getPackaging());

            writeXmlTag(w, "    ", "name", md.getGav().getArtifactId());
            w.write("\n");

            w.write("    <properties>\n");
            for (Map.Entry<String, String> entry : md.getProperties().entrySet()) {
                if (entry.getKey().equals("java.version")) {
                    writeXmlTag(w, "        ", "maven.compiler.target", entry.getValue());
                    writeXmlTag(w, "        ", "maven.compiler.source", entry.getValue());
                } else if (entry.getKey().endsWith(".version")) {
                    if (isVersionPropertyUsed(md, entry.getKey())) {
                        writeXmlTag(w, "        ", entry.getKey(), entry.getValue());
                    }
                } else {
                    writeXmlTag(w, "        ", entry.getKey(), entry.getValue());
                }
            }
            w.write("    </properties>\n\n");

            w.write("    <dependencies>\n");
            for (String scope : md.getDependencyScopes()) {
                for (ModuleDependency dependency : md.getDependencies(scope)) {
                    w.write("        <dependency>\n");
                    toMavenGAV(dependency.getGav(), "            ", w);
                    if (!"compile".equals(scope)) {
                        writeXmlTag(w, "            ", "scope", scope);
                    }
                    if ("system".equals(scope)) {
                        if (dependency.getGav().getGroupId().equals("com.sun.tools")
                                && dependency.getGav().getArtifactId().equals("tools")) {
                            writeXmlTag(w, "            ", "systemPath", "${java.home}/../lib/tools.jar");
                        }
                    }
                    w.write("        </dependency>\n");
                }
            }
            w.write("    </dependencies>\n");

            StringWriter plugins = new StringWriter();
            StringWriter others = new StringWriter();
            for (ModuleFragment fragment : md.getFragments("maven")) {
                if (fragment.matches(PLUGIN_PATTERN)) {
                    fragment.write(md, plugins);
                } else if(fragment.resolvedContent()) {
                    fragment.write(md, others);
                }
            }

            if (plugins.toString().length() > 0) {
                w.write("    <build>\n");
                w.write("        <plugins>\n");
                w.write(plugins.toString());
                w.write("        </plugins>\n");
                w.write("    </build>\n");
            }
            if (others.toString().length() > 0) {
                w.write(others.toString());
            }

            w.write(FOOTER);
        }

        private boolean isVersionPropertyUsed(ModuleDescriptor md, String property) {
            for (String scope : md.getDependencyScopes()) {
                for (ModuleDependency dependency : md.getDependencies(scope)) {
                    if (dependency.getGav().getVersion().indexOf("${" + property + "}") != -1) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void toMavenGAV(GAV gav, String indent, Writer w) throws IOException {
            writeXmlTag(w, indent, "groupId", gav.getGroupId());
            writeXmlTag(w, indent, "artifactId", gav.getArtifactId());
            writeXmlTag(w, indent, "version", gav.getVersion());
            if(gav.getType() != null) {
                writeXmlTag(w, indent, "type", gav.getType());
            }
            if(gav.getClassifier() != null) {
                writeXmlTag(w, indent, "classifier", gav.getClassifier());
            }
            if(gav.isOptional()) {
                writeXmlTag(w, indent, "optional", Boolean.TRUE.toString());
            }
        }

        private void writeXmlTag(Writer w, String indent, String tag, String val) throws IOException {
            w.write(indent);
            w.write("<"); w.write(tag); w.write(">");
            w.write(val);
            w.write("</"); w.write(tag); w.write(">\n");
        }


        private static final String HEADER =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n\n";

        private static final String FOOTER = "</project>\n";

    }

    private final Parser parser = new Parser();
    private final Generator generator = new Generator();

    @Override
    public ModuleDescriptor parse(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return parse(inputStream);
        }
    }

    @Override
    public ModuleDescriptor parse(InputStream stream) throws IOException {
        return parser.parse(stream);
    }

    @Override
    public void generate(ModuleDescriptor md, Writer w) throws IOException {
        generator.generate(md, w);
    }

    @Override
    public String getDefaultFileName() {
        return "pom.xml";
    }
}
