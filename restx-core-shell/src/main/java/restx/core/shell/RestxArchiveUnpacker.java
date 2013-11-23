package restx.core.shell;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

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

    public void unpack(Path jarFile, Path destinationDirectory) {
        try ( JarFile jar = new JarFile(jarFile.toFile()) ) {
            for(Enumeration<JarEntry> jarEntries = jar.entries(); jarEntries.hasMoreElements();){
                JarEntry entry = jarEntries.nextElement();
                String entryPath = entry.getName();
                if(entryPath.startsWith(CHROOT) && !entry.isDirectory()) {
                    Path chrootedFile = destinationDirectory.resolve(entryPath.substring(CHROOT.length()));
                    Files.createParentDirs(chrootedFile.toFile());
                    try(
                      InputStream is = jar.getInputStream(entry);
                      OutputStream os = new FileOutputStream(chrootedFile.toFile());
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

        try {
            Path dependenciesJarFile = destinationDirectory.resolve("target/dependency").resolve(jarFile.getFileName());
            Files.createParentDirs(dependenciesJarFile.toFile());
            if(!jarFile.equals(dependenciesJarFile)) {
                java.nio.file.Files.move(jarFile, dependenciesJarFile);
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
