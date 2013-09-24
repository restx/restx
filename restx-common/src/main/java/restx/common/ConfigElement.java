package restx.common;

import static com.google.common.base.Preconditions.checkNotNull;

/**
* User: xavierhanin
* Date: 9/24/13
* Time: 10:25 PM
*/
public class ConfigElement {
    public static ConfigElement of(String key, String val) {
        return new ConfigElement("", "", key, val);
    }

    public static ConfigElement of(String origin, String doc, String key, String val) {
        return new ConfigElement(origin, doc, key, val);
    }

    private final String origin;
    private final String doc;
    private final String key;
    private final String value;

    private ConfigElement(String origin, String doc, String key, String value) {
        this.origin = checkNotNull(origin);
        this.doc = checkNotNull(doc);
        this.key = checkNotNull(key);
        this.value = checkNotNull(value);
    }

    public String getOrigin() {
        return origin;
    }

    public String getDoc() {
        return doc;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ConfigElement{" +
                "origin='" + origin + '\'' +
                ", doc='" + doc + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
