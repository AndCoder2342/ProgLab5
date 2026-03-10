package io;

import java.io.IOException;
import java.util.Scanner;

/**
 * чтение консоли
 */
public class ConsoleReader implements Reader {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public String readNextLine() throws IOException {
        return scanner.nextLine();
    }

    @Override
    public boolean hasNextLine() throws IOException {
        return scanner.hasNextLine();
    }

    @Override
    public void close() {
        scanner.close();
    }
}