package restx.core.shell;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

public enum ModuleDescriptorType {
    RESTX("md.restx.json"), MAVEN("pom.xml"), IVY("module.ivy");

    private String descriptorFileName;

    ModuleDescriptorType(String descriptorFileName) {
        this.descriptorFileName = descriptorFileName;
    }

    public String getDescriptorFileName() {
        return descriptorFileName;
    }

    public File resolveDescriptorFile(Path inDirectory) {
        return inDirectory.resolve(this.getDescriptorFileName()).toFile();
    }

    public File resolveDescriptorMd5File(Path inDirectory) {
        return inDirectory.resolve(String.format("target/dependency/%s.md5", this.getDescriptorFileName())).toFile();
    }

    public static Optional<ModuleDescriptorType> firstModuleDescriptorTypeWithExistingFile(final Path inDirectory) {
        return FluentIterable
            .from(Arrays.asList(ModuleDescriptorType.RESTX))
            .filter(new Predicate<ModuleDescriptorType>() {
                @Override
                public boolean apply(ModuleDescriptorType mdType) {
                    return mdType.resolveDescriptorFile(inDirectory).exists();
                }
            }).first();
    }

}
