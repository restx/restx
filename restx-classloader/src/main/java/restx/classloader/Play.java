package restx.classloader;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * User: xavierhanin
 * Date: 7/23/13
 * Time: 11:54 AM
 */
public class Play {
    public static ApplicationClasses classes;
    public static File applicationPath = null;
    public static List<VirtualFile> javaPath;
    public static ApplicationClassloader classloader;
    public static List<VirtualFile> roots;

    public static final void init(String... sources) {
        applicationPath = new File(".");

        roots = new CopyOnWriteArrayList<>();
        VirtualFile appRoot = VirtualFile.open(applicationPath);
        roots.add(appRoot);

        javaPath = new CopyOnWriteArrayList<VirtualFile>();
        for (String s : sources) {
            javaPath.add(appRoot.child(s));
        }

        classes = new ApplicationClasses();
        classloader = new ApplicationClassloader();
    }
}
