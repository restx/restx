package restx.tests.json;

/**
 * Date: 3/2/14
 * Time: 22:05
 */
public class StringJsonSource implements JsonSource {
    private final String name;
    private final String content;

    public StringJsonSource(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String content() {
        return content;
    }

    @Override
    public String toString() {
        return "StringJsonSource{" +
                "name='" + name + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
