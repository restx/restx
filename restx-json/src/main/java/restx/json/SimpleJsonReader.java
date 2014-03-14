package restx.json;

import java.io.IOException;

/**
 */
final class SimpleJsonReader extends JsonReader {
    private final char[] buffer;
    private final char[] textBuffer;

    private final int loaded; // currently loaded chars from bufferStartIndex.

    private int pos = -1;   // current position in current buffer
    private int line = -1;
    private int startLineIndex;

    public SimpleJsonReader(Buffers buffers, int loaded) {
        buffer = buffers.readerBuffer;
        textBuffer = buffers.readerBuffer2;
        pos = startLineIndex = 0;
        line = 1;
        this.loaded = loaded;
    }

    public final int readChar() throws IOException {
        if (++pos >= loaded) {
            return -1;
        }
        return buffer[pos];
    }

    public final int current() {
        return buffer[pos];
    }

    public final int index() {
        return pos;
    }

    public void skip(int skip) throws IOException {
        pos += skip;
        if (pos >= loaded) {
            throw new IOException("can't skip to " + index() + " not available chars");
        }
    }

    public char lookAhead(int i) throws IOException {
        int aheadPos = pos + i;
        if (aheadPos >= loaded) {
            throw new IOException("can't read at " + (aheadPos) + ": end of source reached");
        }
        return buffer[aheadPos];
    }

    public String readString() throws IOException {
        int len = loaded;
        for (int j = pos; j < len; j++) {
            if (buffer[j] == '"') {
                String s = new String(buffer, pos, j - pos);
                pos = j;
                return s;
            } else if (buffer[j] == '\\') {
                int charsLen = j - pos;
                int destPos = 0;
                System.arraycopy(buffer, pos, textBuffer, destPos, charsLen);
                pos += charsLen + 1;
                destPos += charsLen;

                for (j+=2; j < len; j++) {
                    if (buffer[j] == '"') {
                        charsLen = j - pos;
                        System.arraycopy(buffer, pos, textBuffer, destPos, charsLen);
                        pos += charsLen;
                        String s = new String(textBuffer, 0, destPos + charsLen);
                        return s;
                    } else if (buffer[j] == '\\') {
                        charsLen = j - pos;
                        System.arraycopy(buffer, pos, textBuffer, destPos, charsLen);
                        pos += charsLen + 1;
                        destPos += charsLen;
                        j++;
                    }
                }
            }
        }
        throw new IOException("unterminated string: end of source reached");
    }

    public String newString(int l) throws IOException {
        if (pos + l > loaded) {
            throw new IllegalArgumentException("can't read string up to never reached char, you must call lookAhead before");
        }

        String s = new String(buffer, pos, l);
        skip(l);
        return s;
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public void nextLine() {
        line++;
        startLineIndex = pos + 1;
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public int column() {
        return pos - startLineIndex + 1;
    }
}
