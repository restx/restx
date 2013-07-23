package restx.classloader;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;

import java.io.*;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The VFS used by Play!
 */
public class VirtualFile {
    private final File realFile;
    private final Iterable<VirtualFile> roots;

    VirtualFile(Iterable<VirtualFile> roots, File file) {
        this.roots = roots;
        this.realFile = file;
    }

    public String getName() {
        return realFile.getName();
    }

    public boolean isDirectory() {
        return realFile.isDirectory();
    }

    public String relativePath() {
        List<String> path = new ArrayList<String>();
        File f = realFile;
        String prefix = "{?}";
        while (true) {
            path.add(f.getName());
            f = f.getParentFile();
            if (f == null) {
                break; // ??
            }
            String module = isRoot(f);
            if (module != null) {
                prefix = module;
                break;
            }

        }
        Collections.reverse(path);
        StringBuilder builder = new StringBuilder();
        for (String p : path) {
            builder.append("/" + p);
        }
        return prefix + builder.toString();
    }

    String isRoot(File f) {
        if (Iterables.isEmpty(roots)) {
            // empty roots means we are the root
            if (realFile.getAbsolutePath().equals(f.getAbsolutePath())) {
                return "{module:" + getName() + "}";
            }
        }
        for (VirtualFile vf : roots) {
            if (vf.realFile.getAbsolutePath().equals(f.getAbsolutePath())) {
                return "{module:" + vf.getName() + "}";
            }
        }
        return null;
    }

    public List<VirtualFile> list() {
        List<VirtualFile> res = new ArrayList<VirtualFile>();
        if (exists()) {
            File[] children = realFile.listFiles();
            for (int i = 0; i < children.length; i++) {
                res.add(new VirtualFile(roots, children[i]));
            }
        }
        return res;
    }

    public boolean exists() {
        try {
            if (realFile != null) {
                return realFile.exists();
            }
            return false;
        } catch (AccessControlException e) {
            return false;
        }
    }

    public InputStream inputstream() {
        try {
            return new FileInputStream(realFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OutputStream outputstream() {
        try {
            return new FileOutputStream(realFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long lastModified() {
        if (realFile != null) {
            return realFile.lastModified();
        }
        return 0L;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof VirtualFile) {
            VirtualFile vf = (VirtualFile) other;
            if (realFile != null && vf.realFile != null) {
                return realFile.equals(vf.realFile);
            }
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        if (realFile != null) {
            return realFile.hashCode();
        }
        return super.hashCode();
    }

    public long length() {
        return realFile.length();
    }

    public VirtualFile child(String name) {
        return new VirtualFile(roots, new File(realFile, name));
    }

    public Channel channel() {
        try {
            FileInputStream fis = new FileInputStream(realFile);
            FileChannel ch = fis.getChannel();
            return ch;
        } catch (FileNotFoundException e) {
            return null;
        }

    }

    public static VirtualFile open(Iterable<VirtualFile> roots, String file) {
        return open(roots, new File(file));
    }

    public static VirtualFile open(Iterable<VirtualFile> roots, File file) {
        return new VirtualFile(roots, file);
    }

    public String contentAsString() {
        try {
            return CharStreams.toString(CharStreams.newReaderSupplier(new InputSupplier<InputStream>() {
                @Override
                public InputStream getInput() throws IOException {
                    return inputstream();
                }
            }, Charsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public File getRealFile() {
        return realFile;
    }

    public void write(CharSequence string) {
        try {
            CharStreams.write(string, CharStreams.newWriterSupplier(new OutputSupplier<OutputStream>() {
                @Override
                public OutputStream getOutput() throws IOException {
                    return outputstream();
                }
            }, Charsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] content() {
        byte[] buffer = new byte[(int) length()];
        try {
            InputStream is = inputstream();
            is.read(buffer);
            is.close();
            return buffer;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public static VirtualFile search(Collection<VirtualFile> roots, String path) {
        for (VirtualFile file : roots) {
            if (file.child(path).exists()) {
                return file.child(path);
            }
        }
        return null;
    }

    public static VirtualFile fromRelativePath(File applicationPath, String relativePath) {
        Pattern pattern = Pattern.compile("^(\\{(.+?)\\})?(.*)$");
        Matcher matcher = pattern.matcher(relativePath);

        if(matcher.matches()) {
            String path = matcher.group(3);
            String module = matcher.group(2);
            if(module == null || module.equals("?") || module.equals("")) {
                return new VirtualFile(ImmutableList.<VirtualFile>of(), applicationPath).child(path);
            }
        }

        return null;
    }
}
