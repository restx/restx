package restx.plugins;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.LogOptions;
import org.apache.ivy.core.module.descriptor.DefaultExcludeRule;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ArtifactId;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.plugins.matcher.ExactOrRegexpPatternMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.google.common.collect.Iterables.addAll;

/**
 * User: xavierhanin
 * Date: 5/4/13
 * Time: 2:46 PM
 */
public class ModulesManager {
    private final Logger logger = LoggerFactory.getLogger(ModulesManager.class);

    private static final Pattern MRID_PATTERN = Pattern.compile("(.+):(.+):(.+)");

    private final ObjectMapper mapper = new ObjectMapper();
    private final URL url;
    private final Ivy ivy;

    public static class DownloadOptions {
        public static final DownloadOptions DEFAULT = new DownloadOptions(Collections.<String>emptyList(), true, false);

        private final ImmutableList<String> exclusions;
        private final boolean transitive;
        private final boolean changing;

        public DownloadOptions(List<String> exclusions, boolean transitive, boolean changing) {
            this.exclusions = ImmutableList.copyOf(exclusions);
            this.transitive = transitive;
            this.changing = changing;
        }

        public ImmutableList<String> getExclusions() {
            return exclusions;
        }

        public boolean isTransitive() {
            return transitive;
        }

        public boolean isChanging() {
            return changing;
        }

        public static class Builder {
            private List<String> exclusions;
            private boolean transitive;
            private boolean changing;
            public Builder(){
                this.exclusions = new ArrayList<>(DEFAULT.exclusions);
                this.transitive = DEFAULT.transitive;
                this.changing = DEFAULT.changing;
            }
            public Builder transitive(boolean transitive) { this.transitive = transitive; return this; }
            public Builder changing(boolean changing) { this.changing = changing; return this; }
            public Builder exclusions(List<String> exclusions) { this.exclusions = exclusions; return this; }
            public DownloadOptions build(){ return new DownloadOptions(this.exclusions, this.transitive, this.changing); }
        }
    }

    public ModulesManager(URL url, Ivy ivy) {
        this.url = url;
        this.ivy = ivy;
    }

    public List<ModuleDescriptor> searchModules(String q) throws IOException {
        if (q.toLowerCase(Locale.ENGLISH).startsWith("category=")) {
            String category = q.substring("category=".length()).toLowerCase(Locale.ENGLISH);
            List<ModuleDescriptor> mds = loadAllRestxModuleDescriptors();
            List<ModuleDescriptor> modules = new ArrayList<>();
            for (ModuleDescriptor md : mds) {
                if (md.getCategory().equals(category)) {
                    modules.add(md);
                }
            }
            return modules;
        } else {
            throw new UnsupportedOperationException("querying for modules other than by category is not supported yet.");
        }
    }

    public List<File> download(List<ModuleDescriptor> modules, File toDir, DownloadOptions opts) throws IOException {
        if (!toDir.exists()) {
            if (!toDir.mkdirs()) {
                throw new IOException("can't create directory " + toDir);
            }
        }
        List<File> files = new ArrayList<>();
        for (ModuleDescriptor module : modules) {
            ivy.pushContext(); // DefaultModuleDescriptor access Ivy current context, so we need to push it.
            try {
                DefaultModuleDescriptor md = DefaultModuleDescriptor.newCallerInstance(
                        toMrid(module.getId()), new String[]{"master", "runtime"}, opts.isTransitive(), opts.isChanging());
                for (String exclude : opts.getExclusions()) {
                    DefaultExcludeRule rule = new DefaultExcludeRule(
                            new ArtifactId(toModuleId(exclude), ".*", ".*", ".*"),
                            new ExactOrRegexpPatternMatcher(),
                            null);
                    rule.addConfiguration("master");
                    rule.addConfiguration("runtime");
                    md.addExcludeRule(rule);
                }

                ResolveReport report = ivy.resolve(md,
                        (ResolveOptions) new ResolveOptions()
                                .setLog(LogOptions.LOG_QUIET)
                );
                if (!report.getAllProblemMessages().isEmpty()) {
                    throw new IllegalStateException("plugin installation failed: " + module.getId() + "\n"
                            + Joiner.on("\n").join(report.getAllProblemMessages()));
                }
                for (ArtifactDownloadReport artifactDownloadReport : report.getAllArtifactsReports()) {
                    if (artifactDownloadReport.getDownloadStatus().equals(DownloadStatus.FAILED)) {
                        logger.warn(String.format("artifact download %s.%s failed",
                                artifactDownloadReport.getName(), artifactDownloadReport.getExt()));
                        continue;
                    }
                    File localFile = artifactDownloadReport.getLocalFile();
                    File to = new File(toDir, artifactDownloadReport.getName() + "." + artifactDownloadReport.getExt());
                    Files.copy(localFile, to);
                    files.add(to);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            } finally {
                ivy.popContext();
            }
        }
        return files;
    }

    public static ModuleId toModuleId(String id) {
        String[] parts = id.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("can't parse module id '" + id + "': it must be of the form groupId:artifactId");
        }
        return ModuleId.newInstance(parts[0], parts[1]);
    }

    public static boolean isMrid(String id) {
        return MRID_PATTERN.matcher(id).matches();
    }

    public static ModuleRevisionId toMrid(String id) {
        String[] parts = id.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("can't parse module revision id '" + id + "': it must be of the form groupId:artifactId:version");
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
