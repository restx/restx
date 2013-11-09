package restx.entity;

/**
 * Similar to Void, except that an instance is declared, making it possible to have
 * Optional.of(Empty.EMPTY)
 */
public final class Empty {
    public static final Empty EMPTY = new Empty();

    private Empty() {}
}
