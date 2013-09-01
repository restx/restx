package restx.specs;

/**
* @author xavierhanin
*/
public abstract class When<T extends Then> {
    private final T then;

    protected When(T then) {
        this.then = then;
    }

    public T getThen() {
        return then;
    }

    public abstract void toString(StringBuilder sb);
}
