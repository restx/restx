package restx.classloader;

/**
 * User: xavierhanin
 * Date: 7/23/13
 * Time: 12:03 PM
 */
public class CompilationException extends RuntimeException {
    private final VirtualFile javaFile;
    private final String message;
    private final int sourceLineNumber;
    private final int sourceStart;
    private final int sourceEnd;

    public CompilationException(VirtualFile javaFile, String message, int sourceLineNumber, int sourceStart, int sourceEnd) {
        this.javaFile = javaFile;
        this.message = message;
        this.sourceLineNumber = sourceLineNumber;
        this.sourceStart = sourceStart;
        this.sourceEnd = sourceEnd;
    }

    public VirtualFile getJavaFile() {
        return javaFile;
    }

    public String getMessage() {
        return message;
    }

    public int getSourceLineNumber() {
        return sourceLineNumber;
    }

    public int getSourceStart() {
        return sourceStart;
    }

    public int getSourceEnd() {
        return sourceEnd;
    }

    @Override
    public String toString() {
        return "CompilationException{" +
                "javaFile=" + javaFile +
                ", message='" + message + '\'' +
                ", sourceLineNumber=" + sourceLineNumber +
                ", sourceStart=" + sourceStart +
                ", sourceEnd=" + sourceEnd +
                '}';
    }
}
