package restx.core.shell;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import restx.AppSettings;
import restx.build.RestxBuild;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * @author fcamblor
 */
public class RestxArchive {

    private static final String CHROOT = "META-INF/restx/app/";
    private Path jarFile;

    public RestxArchive(Path jarFile) {
        this.jarFile = jarFile;
    }

    private static class JarCopierFileVisitor extends SimpleFileVisitor<Path> {
        private final Path startingDirectory;
        private final String targetDirPrefix;
        private List<String> excludes;
        private final JarOutputStream jarOS;

        private JarCopierFileVisitor(Path startingDirectory, JarOutputStream jarOS, String targetDirPrefix, List<String> excludes) {
            this.startingDirectory = startingDirectory;
            this.jarOS = jarOS;
            this.targetDirPrefix = targetDirPrefix;
            this.excludes = excludes;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            super.preVisitDirectory(dir, attrs);

            String directoryRelativizedName = startingDirectory.relativize(dir).toString().replace("\\", "/");
            if(!directoryRelativizedName.isEmpty()) {
                if(!directoryRelativizedName.endsWith("/")) {
                    directoryRelativizedName += "/";
                }

                for(String exclude : excludes){
                    if(directoryRelativizedName.startsWith(exclude)){
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }

                String targetDirectoryPath = targetDirPrefix + directoryRelativizedName;

                JarEntry dirEntry = new JarEntry(targetDirectoryPath);
                dirEntry.setTime(Files.getLastModifiedTime(dir).toMillis());
                jarOS.putNextEntry(dirEntry);
                jarOS.closeEntry();
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            super.visitFile(file, attrs);

            copyFileToJar(jarOS, startingDirectory, targetDirPrefix, file);

            return FileVisitResult.CONTINUE;
        }
    }

    private static void createJarDirIfNotExists(JarOutputStream jarOS, Path rootDir, String path) throws IOException {
        if(!Files.exists(rootDir.resolve(path))){
            JarEntry dirEntry = new JarEntry(path+"/");
            jarOS.putNextEntry(dirEntry);
            jarOS.closeEntry();
        }
    }

    private static void copyFileToJar(JarOutputStream jarOS, Path rootDir, String targetDirPrefix, Path file) throws IOException {
        JarEntry fileEntry = new JarEntry(targetDirPrefix + rootDir.relativize(file).toString());

        try { fileEntry.setTime(Files.getLastModifiedTime(file).toMillis()); }
        catch (IOException e) {}

        jarOS.putNextEntry(fileEntry);
        Files.copy(file, jarOS);
        jarOS.closeEntry();
    }

    public void pack(Path workingDirectory, final Path targetClassesDirectory, List<String> packagingExcludes) {
        try(final JarOutputStream jarOS = new JarOutputStream(new FileOutputStream(jarFile.toFile()))) {

            // Generating md.restx.json if it doesn't exist yet
            // Because a restx archive without this descriptor won't be considered as valid restx archive
            Path md = workingDirectory.resolve("md.restx.json");
            boolean generatedMd = false;
            if(!Files.exists(md)){
                List<Path> foreignModuleDescriptors = RestxBuild.resolveForeignModuleDescriptorsIn(workingDirectory);
                if(!foreignModuleDescriptors.isEmpty()) {
                    Path firstModuleDescriptor = Iterables.getFirst(foreignModuleDescriptors, null);
                    RestxBuild.convert(firstModuleDescriptor, md);
                    generatedMd = true;
                } else {
                    throw new RuntimeException("Project descriptor (either a md.restx.json, or a foreign descriptor (pom, ivy, ...) file) is missing !");
                }
            }

            try {
                Files.walkFileTree(targetClassesDirectory, new JarCopierFileVisitor(targetClassesDirectory, jarOS, "", Collections.<String>emptyList()));

                // Ensuring CHROOT is made available in target jar
                String path = "";
                for(String chrootChunk  : Splitter.on("/").split(CHROOT)){
                    if(!chrootChunk.isEmpty()) {
                        path += chrootChunk;
                        createJarDirIfNotExists(jarOS, targetClassesDirectory, path);
                        path += "/";
                    }
                }

                // Copying everything into CHROOT directory
                Files.walkFileTree(workingDirectory, new JarCopierFileVisitor(workingDirectory, jarOS, CHROOT, packagingExcludes));
            } finally {
                if(generatedMd) {
                    md.toFile().delete();
                }
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    public void unpack(Path destinationDirectory, AppSettings appSettings) {
        Path targetClassesDir = destinationDirectory.resolve(appSettings.targetClasses());
        try ( JarFile jar = new JarFile(jarFile.toFile()) ) {
            if(jar.getJarEntry(CHROOT+"md.restx.json") == null) {
                throw new IllegalArgumentException("File "+jarFile+" is not a restx archive (no md.restx.json file found) !");
            }
            for(Enumeration<JarEntry> jarEntries = jar.entries(); jarEntries.hasMoreElements();){
                JarEntry entry = jarEntries.nextElement();
                String entryPath = entry.getName();
                if(!entry.isDirectory()) {
                    Path destinationFile;
                    // Unpacking chrooted files in app root directory..
                    if(entryPath.startsWith(CHROOT)) {
                        Path chrootedFile = destinationDirectory.resolve(entryPath.substring(CHROOT.length()));
                        destinationFile = chrootedFile;
                    // ... and unpacking other files in classes directory
                    } else {
                        destinationFile = targetClassesDir.resolve(entryPath);
                    }

                    com.google.common.io.Files.createParentDirs(destinationFile.toFile());
                    try(InputStream jarInputStream = jar.getInputStream(entry)) {
                        Files.copy(jarInputStream, destinationFile);
                    }
                }
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
