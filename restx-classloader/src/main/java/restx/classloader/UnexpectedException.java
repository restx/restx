package restx.classloader;

/**
 * User: xavierhanin
 * Date: 7/23/13
 * Time: 11:55 AM
 */
public class UnexpectedException extends RuntimeException {
    public UnexpectedException(Exception ex) {
        super(ex);
    }
}
