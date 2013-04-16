package restx.build;

import restx.build.org.json.JSONArray;
import restx.build.org.json.JSONObject;
import restx.build.org.json.XML;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 2:08 PM
 */
public class MavenSupport implements RestxBuild.Parser, RestxBuild.Generator {
    static class Parser {
        public ModuleDescriptor parse(InputStream stream) throws IOException {
            JSONObject jsonObject = XML.toJSONObject(RestxBuild.toString(stream)).getJSONObject("project");

            GAV parent;
            if (jsonObject.has("parent")) {
                JSONObject parentObject = jsonObject.getJSONObject("parent");
                parent = getGav(parentObject);
            } else {
                parent = null;
            }
            GAV gav = getGav(jsonObject);
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

                    scopeDependencies.add(new ModuleDependency(getGav(dep)));
                }
            }

            return new ModuleDescriptor(parent, gav, packaging,
                    properties, new HashMap<String,List<ModuleFragment>>(), dependencies);
        }

        private GAV getGav(JSONObject jsonObject) {
            return new GAV(jsonObject.getString("groupId"), jsonObject.getString("artifactId"), String.valueOf(jsonObject.get("version")));
        }
    }
    static class Generator {
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
                    w.write("        </dependency>\n");
                }
            }
            w.write("    </dependencies>\n");

            for (ModuleFragment fragment : md.getFragments("maven")) {
                fragment.write(md, w);
            }

            w.write(FOOTER);
        }

        private void toMavenGAV(GAV gav, String indent, Writer w) throws IOException {
            writeXmlTag(w, indent, "groupId", gav.getGroupId());
            writeXmlTag(w, indent, "artifactId", gav.getArtifactId());
            writeXmlTag(w, indent, "version", gav.getVersion());
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

    public ModuleDescriptor parse(InputStream stream) throws IOException {
        return parser.parse(stream);
    }

    public void generate(ModuleDescriptor md, Writer w) throws IOException {
        generator.generate(md, w);
    }

    @Override
    public String getDefaultFileName() {
        return "pom.xml";
    }
}
