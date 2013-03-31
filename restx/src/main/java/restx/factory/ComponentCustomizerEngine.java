package restx.factory;

/**
 * User: xavierhanin
 * Date: 3/31/13
 * Time: 12:51 AM
 */
public interface ComponentCustomizerEngine {
    <T> boolean canCustomize(Name<T> name);
    <T> ComponentCustomizer<T> getCustomizer(Name<T> name);
}
