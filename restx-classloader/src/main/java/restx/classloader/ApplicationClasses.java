package restx.classloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Application classes container.
 */
public class ApplicationClasses {

    /**
     * Cache of all compiled classes
     */
    private final ConcurrentMap<String, ApplicationClass> classes = new ConcurrentHashMap<>();
    private final Iterable<VirtualFile> javaPath;

    public ApplicationClasses(Iterable<VirtualFile> javaPath) {
        this.javaPath = javaPath;
    }

    /**
     * Clear the classes cache
     */
    public void clear() {
        classes.clear();
    }

    /**
     * Get a class by name
     * @param name The fully qualified class name
     * @return The ApplicationClass or null
     */
    public ApplicationClass getApplicationClass(String name) {
        ApplicationClass applicationClass = classes.get(name);
        if (applicationClass != null) {
            return applicationClass;
        }

        VirtualFile javaFile = getJava(javaPath, name);
        if (javaFile != null) {
            classes.putIfAbsent(name, new ApplicationClass(javaFile, name));
        }
        return classes.get(name);
    }

    /**
     * All loaded classes.
     * @return All loaded classes
     */
    public List<ApplicationClass> all() {
        return new ArrayList<ApplicationClass>(classes.values());
    }

    /**
     * Put a new class to the cache.
     */
    public void add(ApplicationClass applicationClass) {
        classes.put(applicationClass.name, applicationClass);
    }

    /**
     * Remove a class from cache
     */
    public void remove(ApplicationClass applicationClass) {
        classes.remove(applicationClass.name);
    }

    public void remove(String applicationClass) {
        classes.remove(applicationClass);
    }

    /**
     * Does this class is already loaded ?
     * @param name The fully qualified class name
     */
    public boolean hasClass(String name) {
        return classes.containsKey(name);
    }

    public void removeClass(String name) {
        classes.remove(name);
    }

    /**
     * Represent a application class
     */
    public class ApplicationClass {
        private final Logger logger = LoggerFactory.getLogger(ApplicationClass.class);

        /**
         * The fully qualified class name
         */
        public String name;
        /**
         * A reference to the java source file
         */
        public VirtualFile javaFile;
        /**
         * The Java source
         */
        public String javaSource;
        /**
         * The compiled byteCode
         */
        public byte[] javaByteCode;
        /**
         * The enhanced byteCode
         */
        public byte[] enhancedByteCode;
        /**
         * The in JVM loaded class
         */
        public Class<?> javaClass;
        /**
         * The in JVM loaded package
         */
        public Package javaPackage;
        /**
         * Last time than this class was compiled
         */
        public Long timestamp = 0L;
        /**
         * Is this class compiled
         */
        boolean compiled;
        /**
         * Signatures checksum
         */
        public int sigChecksum;

        public ApplicationClass() {
        }

        public ApplicationClass(VirtualFile javaFile, String name) {
            this.name = name;
            this.javaFile = javaFile;
            this.refresh();
        }

        /**
         * Need to refresh this class !
         */
        public void refresh() {
            if (this.javaFile != null) {
                this.javaSource = this.javaFile.contentAsString();
            }
            this.javaByteCode = null;
            this.enhancedByteCode = null;
            this.compiled = false;
            this.timestamp = 0L;
        }

//        static final ClassPool enhanceChecker_classPool = Enhancer.newClassPool();
//        static final CtClass ctPlayPluginClass = enhanceChecker_classPool.makeClass(PlayPlugin.class.getName());

        /**
         * Enhance this class
         * @return the enhanced byteCode
         */
        public byte[] enhance() {
            this.enhancedByteCode = this.javaByteCode;
            if (isClass()) {

                // before we can start enhancing this class we must make sure it is not a PlayPlugin.
                // PlayPlugins can be included as regular java files in a Play-application.
                // If a PlayPlugin is present in the application, it is loaded when other plugins are loaded.
                // All plugins must be loaded before we can start enhancing.
                // This is a problem when loading PlayPlugins bundled as regular app-class since it uses the same classloader
                // as the other (soon to be) enhanched play-app-classes.
//                boolean shouldEnhance = true;
//                try {
//                    CtClass ctClass = enhanceChecker_classPool.makeClass(new ByteArrayInputStream(this.enhancedByteCode));
//                    if (ctClass.subclassOf(ctPlayPluginClass)) {
//                        shouldEnhance = false;
//                    }
//                } catch( Exception e) {
//                    // nop
//                }

//                if (shouldEnhance) {
//                    Play.pluginCollection.enhance(this);
//                }
            }
//            if (System.getProperty("precompile") != null) {
//                try {
//                    // emit bytecode to standard class layout as well
//                    File f = Play.getFile("precompiled/java/" + (name.replace(".", "/")) + ".class");
//                    f.getParentFile().mkdirs();
//                    FileOutputStream fos = new FileOutputStream(f);
//                    fos.write(this.enhancedByteCode);
//                    fos.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            return this.enhancedByteCode;

        }

        /**
         * Is this class already compiled but not defined ?
         * @return if the class is compiled but not defined
         */
        public boolean isDefinable() {
            return compiled && javaClass != null;
        }

        public boolean isClass() {
            return ApplicationClasses.isClass(this.name);
        }

        public String getPackage() {
            int dot = name.lastIndexOf('.');
            return dot > -1 ? name.substring(0, dot) : "";
        }

        /**
         * Unload the class
         */
        public void uncompile() {
            this.javaClass = null;
        }

        /**
         * Call back when a class is compiled.
         * @param code The bytecode.
         */
        public void compiled(byte[] code) {
            javaByteCode = code;
            enhancedByteCode = code;
            compiled = true;
            this.timestamp = this.javaFile.lastModified();
        }

        @Override
        public String toString() {
            return name + " (compiled:" + compiled + ")";
        }
    }

    // ~~ Utils
    /**
     * Retrieve the corresponding source file for a given class name.
     * It handles innerClass too !
     * @param name The fully qualified class name 
     * @return The virtualFile if found
     */
    public static VirtualFile getJava(Iterable<VirtualFile> javaPath, String name) {
        String fileName = name;
        if (fileName.contains("$")) {
            fileName = fileName.substring(0, fileName.indexOf("$"));
        }
        fileName = fileName.replace(".", "/") + ".java";
        for (VirtualFile path : javaPath) {
            VirtualFile javaFile = path.child(fileName);
            if (javaFile.exists()) {
                return javaFile;
            }
        }
        return null;
    }

    public static boolean isClass(String name) {
        return !name.endsWith("package-info");
    }

    @Override
    public String toString() {
        return classes.toString();
    }
}
