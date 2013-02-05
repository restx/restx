package restx.converters;

/**
 * User: xavierhanin
 * Date: 2/5/13
 * Time: 11:54 PM
 */
public abstract class SimpleStringConverter<T> implements StringConverter<T> {
    private final Class<T> convertedClass;

    protected SimpleStringConverter(Class<T> convertedClass) {
        this.convertedClass = convertedClass;
    }

    @Override
    public Class<T> getConvertedClass() {
        return convertedClass;
    }
}
