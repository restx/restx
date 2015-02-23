package restx.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This abstract class is used to extract generic types.
 * <p>
 * The {@code type} attribute hold the {@link Type} of &lt;T&gt;
 * <p>
 * For example if this class is used like this:
 * <pre>
 *     Type listStringType = new TypeReference&lt;List&lt;String&gt;&gt;() {}.getType();
 * </pre>
 * The {@code listStringType} will reference the type {@code List<String>}.
 * <p>
 * The idea is based on Gafter's blog post: <a href="http://gafter.blogspot.fr/2006/12/super-type-tokens.html?m=1">
 *     http://gafter.blogspot.fr/2006/12/super-type-tokens.html?m=1</a>
 *
 *
 * @author apeyrard
 */
public abstract class TypeReference<T> {
	private final Type type;

	@SuppressWarnings("unchecked")
	protected TypeReference() {
		Type superClass = getClass().getGenericSuperclass();
		if (superClass instanceof Class<?>) {
			throw new IllegalArgumentException("TypeReference must be constructed with type information.");
		}
		this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
	}

	public Type getType() { return type; }
}


