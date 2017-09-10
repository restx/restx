package restx.build;

import restx.build.org.json.JSONArray;
import restx.build.org.json.JSONObject;
import restx.build.org.json.JSONTokener;

import java.io.*;
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
                if (iterator.hasNext() || !md.getPropertiesFileReferences().isEmpty()) {
                    w.write(",");
                }
                w.write("\n");
            }
            if(!md.getPropertiesFileReferences().isEmpty()) {
                w.write("      \"@files\": [\n");
                for (Iterator<String> itFileRef = md.getPropertiesFileReferences().iterator(); itFileRef.hasNext(); ) {
                    w.write(String.format("        \"%s\"", itFileRef.next()));
                    if(itFileRef.hasNext()) {
                        w.write(",");
                    }
                    w.write("\n");
                }
                w.write("        ]\n");
            }
            w.write("    },\n\n");

            if(!md.getFragmentTypes().isEmpty()) {
                w.write("    \"fragments\": {\n");
                for (Iterator<String> itFragmentType = md.getFragmentTypes().iterator(); itFragmentType.hasNext(); ) {
                    String fragmentType = itFragmentType.next();
                    w.write(String.format("      \"%s\": [\n", fragmentType));
                    for (Iterator<ModuleFragment> itFragment = md.getFragments(fragmentType).iterator(); itFragment.hasNext(); ) {
                        w.write(String.format("        \"%s\"", itFragment.next().getUrl()));
                        if(itFragment.hasNext()) {
                            w.write(",");
                        }
                        w.write("\n");
                    }
                    w.write("      ]");
                    if(itFragmentType.hasNext()) {
                        w.write(",");
                    }
                    w.write("\n");
                }
                w.write("    },\n\n");
            }

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

            JSONObject propertiesJSONObj = null;
            Map<String, String> properties = new LinkedHashMap<>();
            List<String> propertiesFileReferences = new ArrayList<>();
            if (jsonObject.has("properties")) {
                propertiesJSONObj = jsonObject.getJSONObject("properties");
                for(Object p: propertiesJSONObj.keySet()) {
                    String key = p.toString();
                    if("@files".equals(key)) {
                        JSONArray propertyFiles = propertiesJSONObj.getJSONArray(key);
                        for (int i = 0; i < propertyFiles.length(); i++) {
                            propertiesFileReferences.add(propertyFiles.getString(i));
                        }
                    } else {
                        properties.put(key, propertiesJSONObj.getString(key));
                    }
                }
            }

            GAV parent = null;
            if (jsonObject.has("parent")) {
                parent = GAV.parse(jsonObject.getString("parent"));
            }
            GAV gav = GAV.parse(jsonObject.getString("module"));

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
                        String url = array.getString(i);
                        fragmentsForType.add(new ModuleFragment(url));
                    }

                    fragments.put(type, fragmentsForType);
                }
            }

            ModuleDescriptor parsedModuleDescriptor = new ModuleDescriptor(parent, gav, packaging, properties, propertiesFileReferences, fragments, dependencies, null);

            // Interpolating stuff in parsed module descriptor ...

            Map<String, String> interpolatedProperties = new LinkedHashMap<>();
            if(propertiesJSONObj != null) {
                loadJsonProperties(path == null ? null : path.getParent(), interpolatedProperties, propertiesJSONObj);
            }

            GAV interpolatedParent = parent==null?null:GAV.parse(expandProperties(interpolatedProperties, parent.toParseableString()));
            GAV interpolatedGAV = GAV.parse(expandProperties(interpolatedProperties, gav.toParseableString()));

            Map<String, List<ModuleDependency>> interpolatedDependencies = new LinkedHashMap<>();
            for(Map.Entry<String,List<ModuleDependency>> scopedDependenciesEntry: dependencies.entrySet()) {
                List<ModuleDependency> scopedDependencies = new ArrayList<>();
                for(ModuleDependency dep: scopedDependenciesEntry.getValue()) {
                    scopedDependencies.add(new ModuleDependency(GAV.parse(expandProperties(interpolatedProperties, dep.getGav().toParseableString()))));
                }
                interpolatedDependencies.put(scopedDependenciesEntry.getKey(), scopedDependencies);
            }

            Map<String, List<ModuleFragment>> interpolatedFragmentsPerModuleType = new LinkedHashMap<>();
            for(Map.Entry<String,List<ModuleFragment>> perModuleTypeFragment: fragments.entrySet()) {
                List<ModuleFragment> interpolatedFragments = new ArrayList<>();
                for(ModuleFragment fragment: perModuleTypeFragment.getValue()) {
                    interpolatedFragments.add(new ModuleFragment(expandProperties(interpolatedProperties, fragment.getUrl())));
                }
                interpolatedFragmentsPerModuleType.put(perModuleTypeFragment.getKey(), interpolatedFragments);
            }

            return new ModuleDescriptor(interpolatedParent, interpolatedGAV, packaging, interpolatedProperties, propertiesFileReferences, interpolatedFragmentsPerModuleType, interpolatedDependencies, parsedModuleDescriptor);
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
