package restx.common;

import com.google.common.base.Optional;

/**
 * User: xavierhanin
 * Date: 9/24/13
 * Time: 9:52 PM
 */
public interface RestxConfig {
    public Iterable<ConfigElement> elements();
    public Optional<ConfigElement> getElement(String elementKey);
    public Optional<String> getString(String elementKey);
    public Optional<Integer> getInt(String elementKey);
    public Optional<Boolean> getBoolean(String elementKey);
}
