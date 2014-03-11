package restx.json;

import com.google.common.base.CharMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
*/
public abstract class JsonParser<T> {
    protected final JsonReader reader;

    public JsonParser(JsonReader reader) {
        this.reader = reader;
    }

    public final T read() throws IOException {
        skipWhitespaceIfNeeded();


        if (reader.current() != '{') {
            throw complain();
        }

        reader.readChar();

        skipWhitespaceIfNeeded();

        T result = newInstance();
        if (reader.current() == '}') {
            reader.readChar();
            return result;
        }

        while (true) {
            readProperty(result);
            skipWhitespaceIfNeeded();

            switch (reader.current()) {
                case ',':
                    reader.readChar();
                    skipWhitespaceIfNeeded();
                    break;
                case '}':
                    reader.readChar();
                    return result;
                default:
                    throw complain();
            }
        }
    }

    protected abstract T newInstance();

    protected void readProperty(T result) throws IOException {
        String property = readPropertyName();
        skipWhitespaceIfNeeded();

        if (reader.current() != ':') {
            throw complain();
        }

        reader.readChar();
        skipWhitespaceIfNeeded();

        readProperty(property, result);
    }

    protected void skipToPropertyValue(int propertyNameLength) throws IOException {
        reader.skip(propertyNameLength + 1);
        skipWhitespaceIfNeeded();

        if (reader.current() != ':') {
            throw complain();
        }

        reader.readChar();
        skipWhitespaceIfNeeded();
    }

    protected abstract void readProperty(String property, T result) throws IOException;

    protected final long readLong() throws IOException {
        long l;
        l = reader.current() - '0';
        if (l < 0 || l > 9) {
            throw complain();
        }

        reader.readChar();
        while (true) {
            int d = reader.current() - '0';
            if (d < 0 || d > 9) {
                return l;
            }
            l = l*10 + d;
            reader.readChar();
        }
    }

    protected final int readInt() throws IOException {
        int l;
        l = reader.current() - '0';
        if (l < 0 || l > 9) {
            throw complain();
        }

        reader.readChar();
        while (true) {
            int d = reader.current() - '0';
            if (d < 0 || d > 9) {
                return l;
            }
            l = l*10 + d;
            reader.readChar();
        }
    }

    protected final Integer readInteger() throws IOException {
        if (reader.current() == 'n') {
            readNull();
            return null;
        }
        return readInt();
    }

    protected final double readDoublePrimitive() throws IOException {
        int i = 0;
        for (; CharMatcher.DIGIT.matches(reader.lookAhead(i)); i++) {
        }
        if (i==0) {
            throw complain();
        }

        if (reader.lookAhead(i) == '.') {
            i++;

            for (; CharMatcher.DIGIT.matches(reader.lookAhead(i)); i++) {
            }
        }
        return Double.parseDouble(reader.newString(i));
    }

    protected final void readNull() throws IOException {
        if (reader.current() != 'n') {
            throw complain();
        }
        reader.readChar();
        if (reader.current() != 'u') {
            throw complain();
        }
        reader.readChar();
        if (reader.current() != 'l') {
            throw complain();
        }
        reader.readChar();
        if (reader.current() != 'l') {
            throw complain();
        }
        reader.readChar();
    }

    protected final <V> List<V> readObjectArray(JsonParser<V> objectParser) throws IOException {
        if (reader.current() != '[') {
            throw complain();
        }

        reader.readChar();

        if (reader.current() == ']') {
            return Collections.emptyList();
        }

        List<V> results = new ArrayList<V>();
        while (true) {
            results.add(objectParser.read());

            switch (reader.current()) {
                case ',':
                    reader.readChar();
                    skipWhitespaceIfNeeded();
                    break;
                case ']':
                    reader.readChar();
                    return results;
                default:
                    throw complain();
            }
        }
    }

    protected final String readString() throws IOException {
        if (reader.current() != '"') {
            throw complain();
        }

        reader.readChar();

        String s = reader.readString();

        reader.readChar();

        return s;
    }

    protected final String readPropertyName() throws IOException {
        return readString();
    }

    protected final RuntimeException complain() {
        return new RuntimeException("error at " + reader.index() + " (" + ((char) reader.current()) + ")");
    }

    protected final void skipWhitespaceIfNeeded() throws IOException {
        while (reader.current() <= ' ') {
            reader.readChar();
        }
    }
}
