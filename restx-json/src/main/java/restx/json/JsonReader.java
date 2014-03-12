package restx.json;

import java.io.IOException;
import java.io.Reader;

/**
 */
public abstract class JsonReader implements AutoCloseable {
    public static JsonReader with(Reader reader) throws IOException {
        Buffers buffers = Buffers.BUFFERS.get();
        int loaded = reader.read(buffers.readerBuffer, 0, buffers.readerBuffer.length);

        if (loaded < buffers.readerBuffer.length) {
            int r = reader.read(buffers.readerBuffer, loaded, buffers.readerBuffer.length - loaded);
            if (r == -1) {
                reader.close();
                return new SimpleJsonReader(buffers, loaded);
            } else {
                loaded += r;
            }
        }

        // not able to load everything in one buffer
        return new BufferedJsonReader(reader, buffers, loaded);
    }

    public abstract int readChar() throws IOException;

    public abstract int current();

    public abstract int index();

    public abstract void skip(int skip) throws IOException;

    public abstract char lookAhead(int i) throws IOException;

    public abstract String readString() throws IOException;

    public abstract String newString(int l) throws IOException;

    public abstract void nextLine();

    public abstract int line();

    public abstract int column();
}
