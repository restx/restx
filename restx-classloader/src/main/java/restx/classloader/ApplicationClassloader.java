package restx.classloader;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.classloader.ApplicationClasses.ApplicationClass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.instrument.ClassDefinition;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The application classLoader. 
 * Load the classes from the application Java sources files.
 */
public class ApplicationClassloader extends ClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationClassloader.class);


    private final ClassStateHashCreator classStateHashCreator = new ClassStateHashCreator();

    /**
     * A representation of the current state of the ApplicationClassloader.
     * It gets a new value each time the state of the classloader changes.
     */
    private final AtomicLong currentState = new AtomicLong();

    /**
     * This protection domain applies to all loaded classes.
     */
    private final ProtectionDomain protectionDomain;
    
    private final File applicationPath;
    private final ImmutableList<VirtualFile> roots;
    private final ImmutableList<VirtualFile> javaPath;

    private final ApplicationClasses classes;
    private final ApplicationCompiler compiler;
    

    public ApplicationClassloader(File applicationPath, String... sources) {
        super(ApplicationClassloader.class.getClassLoader());
        
        this.applicationPath = applicationPath;

        VirtualFile appRoot = VirtualFile.open(ImmutableList.<VirtualFile>of(), applicationPath);
        roots = ImmutableList.of(appRoot);

        ImmutableList.Builder<VirtualFile> javaPathsBuilder = ImmutableList.builder();
        for (String s : sources) {
            javaPathsBuilder.add(appRoot.child(s));
        }
        javaPath = javaPathsBuilder.build();

        pathHash = computePathHash();
        try {
            CodeSource codeSource = new CodeSource(new URL("file:" + applicationPath.getAbsolutePath()), (Certificate[]) null);
            Permissions permissions = new Permissions();
            permissions.add(new AllPermission());
            protectionDomain = new ProtectionDomain(codeSource, permissions);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        classes = new ApplicationClasses(javaPath);
        compiler = new ApplicationCompiler(classes, this);
    }

    /**
     * You know ...
     */
    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }

        // First check if it's an application Class
        Class<?> applicationClass = loadApplicationClass(name);
        if (applicationClass != null) {
            if (resolve) {
                resolveClass(applicationClass);
            }
            return applicationClass;
        }

        // Delegate to the classic classloader
        return super.loadClass(name, resolve);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~
    public Class<?> loadApplicationClass(String name) {

        if (ApplicationClasses.isClass(name)) {
            Class maybeAlreadyLoaded = findLoadedClass(name);
            if(maybeAlreadyLoaded != null) {
                return maybeAlreadyLoaded;
            }
        }

//        if (usePrecompiled) {
//            try {
//                File file = getFile("precompiled/java/" + name.replace(".", "/") + ".class");
//                if (!file.exists()) {
//                    return null;
//                }
//                byte[] code = IO.readContent(file);
//                Class<?> clazz = findLoadedClass(name);
//                if (clazz == null) {
//                    if (name.endsWith("package-info")) {
//                        definePackage(getPackageName(name), null, null, null, null, null, null, null);
//                    } else {
//                        loadPackage(name);
//                    }
//                    clazz = defineClass(name, code, 0, code.length, protectionDomain);
//                }
//                ApplicationClass applicationClass = classes.getApplicationClass(name);
//                if (applicationClass != null) {
//                    applicationClass.javaClass = clazz;
//                    if (!applicationClass.isClass()) {
//                        applicationClass.javaPackage = applicationClass.javaClass.getPackage();
//                    }
//                }
//                return clazz;
//            } catch (Exception e) {
//                throw new RuntimeException("Cannot find precompiled class file for " + name);
//            }
//        }

        long start = System.currentTimeMillis();
        ApplicationClasses.ApplicationClass applicationClass = classes.getApplicationClass(name);
        if (applicationClass != null) {
            if (applicationClass.isDefinable()) {
                return applicationClass.javaClass;
            }
            byte[] bc = BytecodeCache.getBytecode(name, applicationClass.javaSource);

            logger.trace("Compiling code for {}", name);

            if (!applicationClass.isClass()) {
                definePackage(applicationClass.getPackage(), null, null, null, null, null, null, null);
            } else {
                loadPackage(name);
            }
            if (bc != null) {
                applicationClass.enhancedByteCode = bc;
                applicationClass.javaClass = defineClass(applicationClass.name, applicationClass.enhancedByteCode, 0, applicationClass.enhancedByteCode.length, protectionDomain);
                resolveClass(applicationClass.javaClass);
                if (!applicationClass.isClass()) {
                    applicationClass.javaPackage = applicationClass.javaClass.getPackage();
                }

                logger.trace("{} to load class {} from cache", System.currentTimeMillis() - start, name);

                return applicationClass.javaClass;
            }
            if (applicationClass.javaByteCode != null || compile(applicationClass) != null) {
                applicationClass.enhance();
                applicationClass.javaClass = defineClass(applicationClass.name, applicationClass.enhancedByteCode, 0, applicationClass.enhancedByteCode.length, protectionDomain);
                BytecodeCache.cacheBytecode(applicationClass.enhancedByteCode, name, applicationClass.javaSource);
                resolveClass(applicationClass.javaClass);
                if (!applicationClass.isClass()) {
                    applicationClass.javaPackage = applicationClass.javaClass.getPackage();
                }

                logger.trace("{} to load class {}", System.currentTimeMillis() - start, name);

                return applicationClass.javaClass;
            }
            classes.removeClass(name);
        }
        return null;
    }


    /**
     * Compile the class from Java source
     * @return the bytes that comprise the class file
     */
    public byte[] compile(ApplicationClass applicationClass) {
        long start = System.currentTimeMillis();

        // this call has the side effect to fill applicationClass.javaByteCode
        compiler.compile(new String[]{applicationClass.name});

        logger.trace("{} ms to compile class {}", System.currentTimeMillis() - start, applicationClass.name);

        return applicationClass.javaByteCode;
    }


    private String getPackageName(String name) {
        int dot = name.lastIndexOf('.');
        return dot > -1 ? name.substring(0, dot) : "";
    }

    private void loadPackage(String className) {
        // find the package class name
        int symbol = className.indexOf("$");
        if (symbol > -1) {
            className = className.substring(0, symbol);
        }
        symbol = className.lastIndexOf(".");
        if (symbol > -1) {
            className = className.substring(0, symbol) + ".package-info";
        } else {
            className = "package-info";
        }
        if (findLoadedClass(className) == null) {
            loadApplicationClass(className);
        }
    }

    /**
     * Search for the byte code of the given class.
     */
    protected byte[] getClassDefinition(String name) {
        name = name.replace(".", "/") + ".class";
        InputStream is = getResourceAsStream(name);
        if (is == null) {
            return null;
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = is.read(buffer, 0, buffer.length)) > 0) {
                os.write(buffer, 0, count);
            }
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * You know ...
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        for (VirtualFile vf : javaPath) {
            VirtualFile res = vf.child(name);
            if (res != null && res.exists()) {
                return res.inputstream();
            }
        }
        return super.getResourceAsStream(name);
    }

    /**
     * You know ...
     */
    @Override
    public URL getResource(String name) {
        for (VirtualFile vf : javaPath) {
            VirtualFile res = vf.child(name);
            if (res != null && res.exists()) {
                try {
                    return res.getRealFile().toURI().toURL();
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return super.getResource(name);
    }

    /**
     * You know ...
     */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> urls = new ArrayList<URL>();
        for (VirtualFile vf : javaPath) {
            VirtualFile res = vf.child(name);
            if (res != null && res.exists()) {
                try {
                    urls.add(res.getRealFile().toURI().toURL());
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        Enumeration<URL> parent = super.getResources(name);
        while (parent.hasMoreElements()) {
            URL next = parent.nextElement();
            if (!urls.contains(next)) {
                urls.add(next);
            }
        }
        final Iterator<URL> it = urls.iterator();
        return new Enumeration<URL>() {

            public boolean hasMoreElements() {
                return it.hasNext();
            }

            public URL nextElement() {
                return it.next();
            }
        };
    }

    /**
     * Detect Java changes
     */
    public void detectChanges() {
        // Now check for file modification
        List<ApplicationClass> modifieds = new ArrayList<ApplicationClass>();
        for (ApplicationClass applicationClass : classes.all()) {
            if (applicationClass.timestamp < applicationClass.javaFile.lastModified()) {
                applicationClass.refresh();
                modifieds.add(applicationClass);
            }
        }
        Set<ApplicationClass> modifiedWithDependencies = new HashSet<ApplicationClass>();
        modifiedWithDependencies.addAll(modifieds);
        if (modifieds.size() > 0) {
            // plugins class change notification
//            modifiedWithDependencies.addAll(pluginCollection.onClassesChange(modifieds));
        }
        List<ClassDefinition> newDefinitions = new ArrayList<ClassDefinition>();
        boolean dirtySig = false;
        for (ApplicationClass applicationClass : modifiedWithDependencies) {
            if (compile(applicationClass) == null) {
                classes.removeClass(applicationClass.name);
                bumpState();
            } else {
                int sigChecksum = applicationClass.sigChecksum;
                applicationClass.enhance();
                if (sigChecksum != applicationClass.sigChecksum) {
                    dirtySig = true;
                }
                BytecodeCache.cacheBytecode(applicationClass.enhancedByteCode, applicationClass.name, applicationClass.javaSource);
                newDefinitions.add(new ClassDefinition(applicationClass.javaClass, applicationClass.enhancedByteCode));
                bumpState();
            }
        }
        if (newDefinitions.size() > 0) {
//            Cache.clear();
            if (HotswapAgent.isEnabled()) {
                try {
                    HotswapAgent.reload(newDefinitions.toArray(new ClassDefinition[newDefinitions.size()]));
                } catch (Throwable e) {
                    throw new RuntimeException("Need reload", e);
                }
            } else {
                throw new RuntimeException("Need reload - launch your JVM with -javaagent:<path/to/restx-classloader.jar>");
            }
        }
        // Check signature (variable name & annotations aware !)
        if (dirtySig) {
            throw new RuntimeException("Signature change !");
        }

        // Now check if there is new classes or removed classes
        int hash = computePathHash();
        if (hash != this.pathHash) {
            // Remove class for deleted files !!
            for (ApplicationClass applicationClass : classes.all()) {
                if (!applicationClass.javaFile.exists()) {
                    classes.removeClass(applicationClass.name);
                    bumpState();
                }
                if (applicationClass.name.contains("$")) {
                    classes.removeClass(applicationClass.name);
                    bumpState();
                    // Ok we have to remove all classes from the same file ...
                    VirtualFile vf = applicationClass.javaFile;
                    for (ApplicationClass ac : classes.all()) {
                        if (ac.javaFile.equals(vf)) {
                            classes.removeClass(ac.name);
                        }
                    }
                }
            }
            throw new RuntimeException("Path has changed");
        }
    }

    private void bumpState() {
        currentState.incrementAndGet();
    }

    public long getCurrentState() {
        return currentState.get();
    }

    /**
     * Used to track change of the application sources path
     */
    int pathHash = 0;

    int computePathHash() {
        return classStateHashCreator.computePathHash(javaPath);
    }

    /**
     * Try to load all .java files found.
     * @return The list of well defined Class
     */
    public List<Class> getAllClasses() {
        if (allClasses == null) {
            allClasses = new ArrayList<Class>();

//            if(!pluginCollection.compileSources()) {
//
//                List<ApplicationClass> all = new ArrayList<ApplicationClass>();
//
//                for (VirtualFile virtualFile : javaPath) {
//                    all.addAll(getAllClasses(virtualFile));
//                }
//                List<String> classNames = new ArrayList<String>();
//                for (int i = 0; i < all.size(); i++) {
//                        ApplicationClass applicationClass = all.get(i);
//                    if (applicationClass != null && !applicationClass.compiled && applicationClass.isClass()) {
//                        classNames.add(all.get(i).name);
//                    }
//                }
//
//                classes.compiler.compile(classNames.toArray(new String[classNames.size()]));
//
//            }

            for (ApplicationClass applicationClass : classes.all()) {
                Class clazz = loadApplicationClass(applicationClass.name);
                if (clazz != null) {
                    allClasses.add(clazz);
                }
            }

            Collections.sort(allClasses, new Comparator<Class>() {

                public int compare(Class o1, Class o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }
        return allClasses;
    }
    List<Class> allClasses = null;

    /**
     * Retrieve all application classes assignable to this class.
     * @param clazz The superclass, or the interface.
     * @return A list of class
     */
    public List<Class> getAssignableClasses(Class clazz) {
        getAllClasses();
        List<Class> results = new ArrayList<Class>();
        if (clazz != null) {
            for (ApplicationClass applicationClass : classes.all()) {
                if (!applicationClass.isClass()) {
                    continue;
                }
                try {
                    loadClass(applicationClass.name);
                } catch (ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    if (clazz.isAssignableFrom(applicationClass.javaClass) && !applicationClass.javaClass.getName().equals(clazz.getName())) {
                        results.add(applicationClass.javaClass);
                    }
                } catch (Exception e) {
                }
            }
        }
        return results;
    }

    /**
     * Find a class in a case insensitive way
     * @param name The class name.
     * @return a class
     */
    public Class getClassIgnoreCase(String name) {
        getAllClasses();
        for (ApplicationClass c : classes.all()) {
            if (c.name.equalsIgnoreCase(name) || c.name.replace("$", ".").equalsIgnoreCase(name)) {
                return loadApplicationClass(c.name);
            }
        }
        return null;
    }

    /**
     * Retrieve all application classes with a specific annotation.
     * @param clazz The annotation class.
     * @return A list of class
     */
    public List<Class> getAnnotatedClasses(Class<? extends Annotation> clazz) {
        getAllClasses();
        List<Class> results = new ArrayList<Class>();
        for (ApplicationClass applicationClass : classes.all()) {
            if (!applicationClass.isClass()) {
                continue;
            }
            try {
                loadClass(applicationClass.name);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            if (applicationClass.javaClass != null && applicationClass.javaClass.isAnnotationPresent(clazz)) {
                results.add(applicationClass.javaClass);
            }
        }
        return results;
    }

    public List<Class> getAnnotatedClasses(Class[] clazz) {
        List<Class> results = new ArrayList<Class>();
        for (Class<? extends Annotation> cl : clazz) {
            results.addAll(getAnnotatedClasses(cl));
        }
        return results;
    }
    

    // ~~~ Intern
    List<ApplicationClass> getAllClasses(String basePackage) {
        List<ApplicationClass> res = new ArrayList<ApplicationClass>();
        for (VirtualFile virtualFile : javaPath) {
            res.addAll(getAllClasses(virtualFile, basePackage));
        }
        return res;
    }

    List<ApplicationClass> getAllClasses(VirtualFile path) {
        return getAllClasses(path, "");
    }

    List<ApplicationClass> getAllClasses(VirtualFile path, String basePackage) {
        if (basePackage.length() > 0 && !basePackage.endsWith(".")) {
            basePackage += ".";
        }
        List<ApplicationClass> res = new ArrayList<ApplicationClass>();
        for (VirtualFile virtualFile : path.list()) {
            scan(res, basePackage, virtualFile);
        }
        return res;
    }

    void scan(List<ApplicationClass> classes, String packageName, VirtualFile current) {
        if (!current.isDirectory()) {
            if (current.getName().endsWith(".java") && !current.getName().startsWith(".")) {
                String classname = packageName + current.getName().substring(0, current.getName().length() - 5);
                classes.add(this.classes.getApplicationClass(classname));
            }
        } else {
            for (VirtualFile virtualFile : current.list()) {
                scan(classes, packageName + current.getName() + ".", virtualFile);
            }
        }
    }

    void scanPrecompiled(List<ApplicationClass> classes, String packageName, VirtualFile current) {
        if (!current.isDirectory()) {
            if (current.getName().endsWith(".class") && !current.getName().startsWith(".")) {
                String classname = packageName.substring(5) + current.getName().substring(0, current.getName().length() - 6);
                classes.add(this.classes.getApplicationClass(classname));
            }
        } else {
            for (VirtualFile virtualFile : current.list()) {
                scanPrecompiled(classes, packageName + current.getName() + ".", virtualFile);
            }
        }
    }

    @Override
    public String toString() {
        return "(restx) " + (allClasses == null ? "" : allClasses.toString());
    }

}
