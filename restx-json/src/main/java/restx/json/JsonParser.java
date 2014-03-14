package restx.json;

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

    protected final long readPrimitveLong() throws IOException {
        long num = 1;
        if (reader.current() == '-') {
            num = -1; reader.readChar();
        } else if (reader.current() == '+') {
            reader.readChar();
        }
        int d = reader.current() - '0';
        if (d < 0 || d > 9) {
            throw complain();
        }
        num = num * d;

        reader.readChar();
        while (true) {
            d = reader.current() - '0';
            if (d < 0 || d > 9) {
                return num;
            }
            num = num*10 + d;
            reader.readChar();
        }
    }

    protected final int readPrimitveInt() throws IOException {
        int num = 1;
        if (reader.current() == '-') {
            num = -1; reader.readChar();
        } else if (reader.current() == '+') {
            reader.readChar();
        }
        int d = reader.current() - '0';
        if (d < 0 || d > 9) {
            throw complain();
        }
        num = num * d;

        reader.readChar();
        while (true) {
            d = reader.current() - '0';
            if (d < 0 || d > 9) {
                return num;
            }
            num = num*10 + d;
            reader.readChar();
        }
    }

    protected final Integer readInteger() throws IOException {
        if (reader.current() == 'n') {
            readNull();
            return null;
        }
        return readPrimitveInt();
    }

    protected final double readPrimitveDouble() throws IOException {
        int i = 0;
        char c = reader.lookAhead(i);
        while (!(c < ' ' || c == '}' || c == ',')) {
            c = reader.lookAhead(++i);
        }
        if (i==0) {
            throw complain();
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
        return new RuntimeException("error at line " + reader.line() + " col " + reader.column() + " (" + ((char) reader.current()) + ")");
    }

    protected final void skipWhitespaceIfNeeded() throws IOException {
        while (reader.current() <= ' ') {
            if (reader.current() == '\n') {
                reader.nextLine();
            }
            reader.readChar();
        }
    }
}
