package restx.json;

/**
* Date: 11/3/14
* Time: 22:35
*/
public class Buffers {
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
