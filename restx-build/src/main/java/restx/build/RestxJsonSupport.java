package restx.build;

import restx.build.org.json.JSONArray;
import restx.build.org.json.JSONObject;
import restx.build.org.json.JSONTokener;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static restx.build.RestxBuildHelper.expandProperties;

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
                w.write(String.format("    \"parent\": \"%s\",\n", md.getParent().toParseableString()));
            }
            w.write(String.format("    \"module\": \"%s\",\n", md.getGav().toString()));
            if (!"jar".equals(md.getPackaging())) {
                w.write(String.format("    \"packaging\": \"%s\",\n", md.getPackaging()));
            }
            w.write("\n");

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
                    w.write(String.format("            \"%s\"", dependency.getGav().toParseableString()));
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
        public ModuleDescriptor parse(Path path) throws IOException {
            try (InputStream inputStream = Files.newInputStream(path)) {
                return parse(path, inputStream);
            }
        }
        public ModuleDescriptor parse(InputStream inputStream) throws IOException {
            return parse(null, inputStream);
        }
        private ModuleDescriptor parse(Path path, InputStream inputStream) throws IOException {
            JSONObject jsonObject = new JSONObject(new JSONTokener(new InputStreamReader(inputStream, "UTF-8")));

            Map<String, String> properties = new LinkedHashMap<>();
            if (jsonObject.has("properties")) {
                loadJsonProperties(path == null ? null : path.getParent(), properties, jsonObject.getJSONObject("properties"));
            }

            GAV parent = null;
            if (jsonObject.has("parent")) {
                parent = GAV.parse(expandProperties(properties, jsonObject.getString("parent")));
            }
            GAV gav = GAV.parse(expandProperties(properties, jsonObject.getString("module")));

            String packaging = jsonObject.has("packaging") ? jsonObject.getString("packaging") : "jar";

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

            Map<String, List<ModuleFragment>> fragments = new LinkedHashMap<>();
            if (jsonObject.has("fragments")) {
                JSONObject jsonFragments = jsonObject.getJSONObject("fragments");
                for (Object key : jsonFragments.keySet()) {
                    String type = (String) key;
                    List<ModuleFragment> fragmentsForType = new ArrayList<>();

                    JSONArray array = jsonFragments.getJSONArray(type);
                    for (int i = 0; i < array.length(); i++) {
                        String url = expandProperties(properties, array.getString(i));

                        if (url.startsWith("classpath://")) {
                            String fragmentPath = url.substring("classpath://".length());
                            InputStream stream = getClass().getResourceAsStream(fragmentPath);
                            if (stream == null) {
                                throw new IllegalArgumentException("classpath fragment not found: '" + fragmentPath + "'" +
                                        ". Check your classpath.");
                            }
                            fragmentsForType.add(new ModuleFragment(RestxBuildHelper.toString(stream)));
                        } else {
                            URL fragmentUrl = new URL(url);
                            try (InputStream stream = fragmentUrl.openStream()) {
                                fragmentsForType.add(new ModuleFragment(RestxBuildHelper.toString(stream)));
                            }
                        }
                    }

                    fragments.put(type, fragmentsForType);
                }
            }

            return new ModuleDescriptor(parent, gav, packaging, properties, fragments, dependencies);
        }

        private void loadJsonProperties(Path path, Map<String, String> properties, JSONObject props) throws IOException {
            for (Object p : props.keySet()) {
                String key = p.toString();

                if (key.equals("@files")) {
                    JSONArray propertyFiles = props.getJSONArray(key);
                    for (int i = 0; i < propertyFiles.length(); i++) {
                        String propertyFile = propertyFiles.getString(i);
                        Path propertyFilePath = path == null ? Paths.get(propertyFile) : path.resolve(propertyFile);

                        if (!propertyFilePath.toFile().exists()) {
                            throw new IllegalArgumentException(
                                    "can't resolve property file " + propertyFilePath.toAbsolutePath() + "." +
                                            " Not found." +
                                    (path == null ?
                                            " Note that parsing from mere inputstream resolve files " +
                                                    "relative to current directory." :
                                            ""));
                        }

                        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(propertyFilePath))) {
                            loadJsonProperties(propertyFilePath.getParent(), properties, new JSONObject(new JSONTokener(reader)));
                        }
                    }
                } else {
                    properties.put(key, props.getString(key));
                }
            }
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

    public ModuleDescriptor parse(Path path) throws IOException {
        return parser.parse(path);
    }

    @Override
    public String getDefaultFileName() {
        return "md.restx.json";
    }
}
