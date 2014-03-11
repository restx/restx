package restx.json;

/**
*/
class Buffers {
    static final ThreadLocal<Buffers> BUFFERS = new ThreadLocal<Buffers>() {
        @Override
        protected Buffers initialValue() {
            return new Buffers();
        }
    };

    final char[] readerBuffer  = new char[8000];
    final char[] readerBuffer2 = new char[8000];
    final char[] textBuffer    = new char[2000];
}
