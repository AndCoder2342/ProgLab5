package io;

import java.io.IOException;

/**
 * интерфейс для чтения
 */
public interface Reader extends AutoCloseable {
    /**
     * читает следующую строку
     * @return прочитанная строка или null
     * @throws IOException если произошла ошибка io
     */
    String readNextLine() throws IOException;

    /**
     * есть ли следующая строка
     * @return true если есть следующая строка
     * @throws IOException если произошла ошибка io
     */
    boolean hasNextLine() throws IOException;
}