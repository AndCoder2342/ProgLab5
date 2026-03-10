package io;

import java.io.IOException;

/**
 *
 */
public class FileWriter implements Writer {
    private final String filepath;
    private final java.io.FileWriter fileWriter;

    /**
     * создает вритер
     * @param filepath путь к файлу
     * @throws IOException если файл не может быть создан
     */
    public FileWriter(String filepath) throws IOException {
        this.filepath = filepath;
        fileWriter = new java.io.FileWriter(filepath);
    }

    @Override
    public void write(String line) throws IOException {
        fileWriter.write(line);
    }

    @Override
    public void writeln(String line) throws IOException {
        fileWriter.write(line + System.lineSeparator());
    }

    @Override
    public void close() throws IOException {
        fileWriter.close();
    }

    public String getFilepath() {
        return filepath;
    }
}