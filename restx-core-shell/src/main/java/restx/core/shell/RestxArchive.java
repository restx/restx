package restx.core.shell;

import com.google.common.base.Throwables;
import restx.AppSettings;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author fcamblor
 */
public class RestxArchive {

    private static final String CHROOT = "META-INF/restx/app/";
    private final Path jarFile;

    public RestxArchive(Path jarFile) {
        this.jarFile = jarFile;
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
