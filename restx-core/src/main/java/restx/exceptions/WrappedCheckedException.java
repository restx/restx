package restx.exceptions;

public class WrappedCheckedException extends RuntimeException {
    public WrappedCheckedException(Exception cause) {
        super(cause);
    }
}
