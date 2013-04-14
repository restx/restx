package restx.build;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.*;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 2:08 PM
 */
public class RestxJsonSupport implements RestxBuild.Parser, RestxBuild.Generator {

    static class Generator {
        public void generate(ModuleDescriptor md, Writer w) throws IOException {
            w.write("{\n");

            if (md.getParent() != null) {
                w.write(String.format("    \"parent\": \"%s\",\n", md.getParent()));
            }
            w.write(String.format("    \"module\": \"%s\",\n\n", md.getGav()));

            w.write("    \"properties\": {\n");
            for (Iterator<Map.Entry<String, String>> iterator = md.getProperties().entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> entry = iterator.next();
                w.write(String.format("        \"%s\": \"%s\"", entry.getKey(), entry.getValue()));
                if (iterator.hasNext()) {
                    w.write(",");
                }
                w.write("\n");
            }
            w.write("    },\n\n");


            w.write("    \"dependencies\": {\n");
            Set<String> scopes = md.getDependencyScopes();
            for (Iterator<String> itScopes = scopes.iterator(); itScopes.hasNext(); ) {
                String scope = itScopes.next();
                w.write(String.format("        \"%s\": [\n", scope));
                for (Iterator<ModuleDependency> itDeps = md.getDependencies(scope).iterator(); itDeps.hasNext(); ) {
                    ModuleDependency dependency = itDeps.next();
                    w.write(String.format("            \"%s\"", dependency.getGav()));
                    if (itDeps.hasNext()) {
                        w.write(",");
                    }
                    w.write("\n");
                }
                w.write("        ]");
                if (itScopes.hasNext()) {
                    w.write(",");
                }
                w.write("\n");
            }
            w.write("    }\n");

            w.write("}\n");
        }
    }

    static class Parser {
        public ModuleDescriptor parse(InputStream inputStream) throws IOException {
            JSONObject jsonObject = new JSONObject(new JSONTokener(new InputStreamReader(inputStream, "UTF-8")));

            GAV parent = null;
            if (jsonObject.has("parent")) {
                parent = GAV.parse(jsonObject.getString("parent"));
            }
            GAV gav = GAV.parse(jsonObject.getString("module"));

            String packaging = "jar";
            Map<String, String> properties = new LinkedHashMap<>();
            if (jsonObject.has("properties")) {
                JSONObject props = jsonObject.getJSONObject("properties");
                for (Object p : props.keySet()) {
                    String key = p.toString();
                    properties.put(key, props.getString(key));
                }
            }

            Map<String, List<ModuleDependency>> dependencies = new LinkedHashMap<>();
            if (jsonObject.has("dependencies")) {
                JSONObject scopes = jsonObject.getJSONObject("dependencies");
                for (Object s : scopes.keySet()) {
                    String scope = s.toString();
                    List<ModuleDependency> scopeDeps = new ArrayList<>();
                    dependencies.put(scope, scopeDeps);

                    JSONArray deps = scopes.getJSONArray(scope);
                    for (int i = 0; i < deps.length(); i++) {
                        scopeDeps.add(new ModuleDependency(GAV.parse(deps.getString(i))));
                    }
                }
            }

            return new ModuleDescriptor(parent, gav, packaging, properties, dependencies);
        }
    }

    private final Generator generator = new Generator();
    private final Parser parser = new Parser();

    public void generate(ModuleDescriptor md, Writer w) throws IOException {
        generator.generate(md, w);
    }

    public ModuleDescriptor parse(InputStream inputStream) throws IOException {
        return parser.parse(inputStream);
    }
}
