package restx.plugins;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.LogOptions;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.addAll;

/**
 * User: xavierhanin
 * Date: 5/4/13
 * Time: 2:46 PM
 */
public class ModulesManager {
    private final Logger logger = LoggerFactory.getLogger(ModulesManager.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final URL url;
    private Ivy ivy = new Ivy();

    public ModulesManager(URL url) {
        this.url = url;
        try {
            ivy.configureDefault();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ModuleDescriptor> searchModules(String q) throws IOException {
        if ("category=shell".equalsIgnoreCase(q)) {
            List<ModuleDescriptor> mds = loadAllRestxModuleDescriptors();
            List<ModuleDescriptor> modules = new ArrayList<>();
            for (ModuleDescriptor md : mds) {
                if (md.getCategory().equals("shell")) {
                    modules.add(md);
                }
            }
            return modules;
        } else {
            throw new UnsupportedOperationException("querying for modules other than shell plugins is not supported yet.");
        }
    }

    public List<File> download(List<ModuleDescriptor> modules, File toDir) throws IOException {
        if (!toDir.exists()) {
            if (!toDir.mkdirs()) {
                throw new IOException("can't create directory " + toDir);
            }
        }
        List<File> files = new ArrayList<>();
        for (ModuleDescriptor module : modules) {
            try {
                ResolveReport report = ivy.resolve(toMrid(module.getId()),
                        (ResolveOptions) new ResolveOptions()
                            .setConfs(new String[]{"master", "runtime"})
                            .setLog(LogOptions.LOG_QUIET), false);
                for (ArtifactDownloadReport artifactDownloadReport : report.getAllArtifactsReports()) {
                    File localFile = artifactDownloadReport.getLocalFile();
                    File to = new File(toDir, localFile.getName());
                    Files.copy(localFile, to);
                    files.add(to);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return files;
    }

    ModuleRevisionId toMrid(String id) {
        String[] parts = id.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("can't parse module id '" + id + "': it must be of the form groupId:artifactId:version");
        }
        return ModuleRevisionId.newInstance(parts[0], parts[1], parts[2]);
    }

    private List<ModuleDescriptor> loadAllRestxModuleDescriptors() throws IOException {
        List<ModuleDescriptor> modules = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(url.openStream(), Charsets.UTF_8)) {
            addAll(modules, mapper
                    .reader(new TypeReference<ArrayList<ModuleDescriptor>>() { })
                    .<Iterable<ModuleDescriptor>>readValue(reader));
        }

        return modules;
    }
}
