package restx.core.shell;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import restx.AppSettings;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author fcamblor
 */
public class RestxArchiveUnpacker {

    private static final String CHROOT = "META-INF/restx/app/";

    public void unpack(Path jarFile, Path destinationDirectory, AppSettings appSettings) {
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

                    Files.createParentDirs(destinationFile.toFile());
                    try(
                      InputStream is = jar.getInputStream(entry);
                      OutputStream os = new FileOutputStream(destinationFile.toFile());
                    ) {
                        ByteStreams.copy(is, os);
                    } catch(IOException e) {
                        throw Throwables.propagate(e);
                    }
                }
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
