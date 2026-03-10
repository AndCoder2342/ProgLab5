// src/main/java/io/Writer.java
package io;

import java.io.IOException;

/**
 * интерфейс для записи
 */
public interface Writer extends AutoCloseable {
    /**
     * записывает строку без перевода строки
     * @param line строка для записи
     * @throws IOException если произошла ошибка  io
     */
    void write(String line) throws IOException;

    /**
     * записывает строку с переводом строки
     * @param line строка для записи
     * @throws IOException если произошла io
     */
    void writeln(String line) throws IOException;
}