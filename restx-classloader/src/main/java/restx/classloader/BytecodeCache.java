package restx.classloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;

/**
 * Used to speed up compilation time
 */
public class BytecodeCache {
    private final static Logger logger = LoggerFactory.getLogger(BytecodeCache.class);
    private static File tmpDir = new File("tmp");

    /**
     * Delete the bytecode
     * @param name Cache name
     */
    public static void deleteBytecode(String name) {
        try {
            if (!useCache()) {
                return;
            }
            File f = cacheFile(name.replace("/", "_").replace("{", "_").replace("}", "_").replace(":", "_"));
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Retrieve the bytecode if source has not changed
     * @param name The cache name
     * @param source The source code
     * @return The bytecode
     */
    public static byte[] getBytecode(String name, String source) {
        try {
            if (!useCache()) {
                return null;
            }
            File f = cacheFile(name.replace("/", "_").replace("{", "_").replace("}", "_").replace(":", "_"));
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);
                // Read hash
                int offset = 0;
                int read = -1;
                StringBuilder hash = new StringBuilder();
                while ((read = fis.read()) != 0) {
                    hash.append((char) read);
                    offset++;
                }
                if (!hash(source).equals(hash.toString())) {

                    logger.trace("Bytecode too old ({} != {}})", hash, hash(source));
                    return null;
                }
                byte[] byteCode = new byte[(int) f.length() - (offset + 1)];
                fis.read(byteCode);
                fis.close();
                return byteCode;
            }

            logger.trace("Cache MISS for {}", name);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cache the bytecode
     * @param byteCode The bytecode
     * @param name The cache name
     * @param source The corresponding source
     */
    public static void cacheBytecode(byte[] byteCode, String name, String source) {
        try {
            if (!useCache()) {
                return;
            }
            File f = cacheFile(name.replace("/", "_").replace("{", "_").replace("}", "_").replace(":", "_"));
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(hash(source).getBytes("utf-8"));
            fos.write(0);
            fos.write(byteCode);
            fos.close();

            logger.trace("{} cached", name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Build a hash of the source code.
     * To efficiently track source code modifications.
     */
    static String hash(String text) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update((text).getBytes("utf-8"));
            byte[] digest = messageDigest.digest();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < digest.length; ++i) {
                int value = digest[i];
                if (value < 0) {
                    value += 256;
                }
                builder.append(Integer.toHexString(value));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the real file that will be used as cache.
     */
    static File cacheFile(String id) {
        File dir = new File(tmpDir, "bytecode/dev");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, id);
    }

    private static boolean useCache() {
        return true;
    }
}
