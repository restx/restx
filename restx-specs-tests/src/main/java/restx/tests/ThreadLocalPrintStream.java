package restx.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

/**
 * Date: 26/12/13
 * Time: 21:07
 */
public class ThreadLocalPrintStream extends PrintStream {
    private final ThreadLocal<PrintStream> current;

    public ThreadLocalPrintStream(final PrintStream def) {
        super(new ByteArrayOutputStream());
        current = new ThreadLocal<PrintStream>() {
            @Override
            protected PrintStream initialValue() {
                return def;
            }
        };
    }

    public void setCurrent(PrintStream stream) {
        current.set(stream);
    }

    public void clearCurrent() {
        current.remove();
    }

    @Override
    public void flush() {
        current().flush();
    }

    @Override
    public void close() {
        current().close();
    }

    @Override
    public boolean checkError() {
        return current().checkError();
    }

    @Override
    public void write(int b) {
        current().write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        current().write(buf, off, len);
    }

    @Override
    public void print(boolean b) {
        current().print(b);
    }

    @Override
    public void print(char c) {
        current().print(c);
    }

    @Override
    public void print(int i) {
        current().print(i);
    }

    @Override
    public void print(long l) {
        current().print(l);
    }

    @Override
    public void print(float f) {
        current().print(f);
    }

    @Override
    public void print(double d) {
        current().print(d);
    }

    @Override
    public void print(char[] s) {
        current().print(s);
    }

    @Override
    public void print(String s) {
        current().print(s);
    }

    @Override
    public void print(Object obj) {
        current().print(obj);
    }

    @Override
    public void println() {
        current().println();
    }

    @Override
    public void println(boolean x) {
        current().println(x);
    }

    @Override
    public void println(char x) {
        current().println(x);
    }

    @Override
    public void println(int x) {
        current().println(x);
    }

    @Override
    public void println(long x) {
        current().println(x);
    }

    @Override
    public void println(float x) {
        current().println(x);
    }

    @Override
    public void println(double x) {
        current().println(x);
    }

    @Override
    public void println(char[] x) {
        current().println(x);
    }

    @Override
    public void println(String x) {
        current().println(x);
    }

    @Override
    public void println(Object x) {
        current().println(x);
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        return current().printf(format, args);
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        return current().printf(l, format, args);
    }

    @Override
    public PrintStream format(String format, Object... args) {
        return current().format(format, args);
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        return current().format(l, format, args);
    }

    @Override
    public PrintStream append(CharSequence csq) {
        return current().append(csq);
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        return current().append(csq, start, end);
    }

    @Override
    public PrintStream append(char c) {
        return current().append(c);
    }

    @Override
    public void write(byte[] b) throws IOException {
        current().write(b);
    }

    PrintStream current() {
        return current.get();
    }

}
