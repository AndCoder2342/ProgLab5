package io;

import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
public class ConsoleWriter implements Writer {
    private final PrintWriter printWriter = new PrintWriter(System.out, true);

    @Override
    public void write(String line) throws IOException {
        printWriter.print(line);
    }

    @Override
    public void writeln(String line) throws IOException {
        printWriter.println(line);
    }

    @Override
    public void close() {
        printWriter.flush();
    }
}