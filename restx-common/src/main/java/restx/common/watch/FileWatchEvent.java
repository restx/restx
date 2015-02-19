package restx.common.watch;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
* User: xavierhanin
* Date: 7/27/13
* Time: 2:17 PM
*/
public class FileWatchEvent {
    public static FileWatchEvent newInstance(Path root, Path dir, Path path, WatchEvent.Kind<?> kind, int count) {
        return new FileWatchEvent(root, normalizePath(root, dir.resolve(normalizePath(dir, path))), kind, count);
    }

    /**
     * Create a new {@link FileWatchEvent} from a reference, and apply the new specified kind.
     * @param ref the reference
     * @param newKind the new kind
     * @return the created event
     */
    public static FileWatchEvent fromWithKind(FileWatchEvent ref, WatchEvent.Kind<?> newKind) {
        return new FileWatchEvent(ref.dir, ref.path, newKind, ref.count);
    }

    private final Path dir;
    private final Path path;
    private final WatchEvent.Kind<?> kind;
    private final int count;

    private FileWatchEvent(Path dir, Path path, WatchEvent.Kind<?> kind, int count) {
        this.dir = dir;
        this.path = path;
        this.kind = kind;
        this.count = count;
    }

    public Path getDir() {
        return dir;
    }

    public Path getPath() {
        return path;
    }

    public WatchEvent.Kind<?> getKind() {
        return kind;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "FileWatchEvent{" +
                "dir=" + dir +
                ", path=" + path +
                ", kind=" + kind +
                ", count=" + count +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileWatchEvent that = (FileWatchEvent) o;

        if (!dir.equals(that.dir)) return false;
        if (!kind.equals(that.kind)) return false;
        if (!path.equals(that.path)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dir.hashCode();
        result = 31 * result + path.hashCode();
        result = 31 * result + kind.hashCode();
        return result;
    }

    private static Path normalizePath(Path dir, Path path) {
        if (path.startsWith(dir)) {
            return dir.relativize(path);
        }
        if (path.isAbsolute() && path.startsWith(dir.toAbsolutePath())) {
            return dir.toAbsolutePath().relativize(path);
        }
        return path;
    }
}
