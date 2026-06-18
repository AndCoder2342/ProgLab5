package commands;

import java.io.Serializable;

/**
 * Команда завершения программы
 * Выполняется на КЛИЕНТЕ, не отправляется на сервер
 */
public class ExitCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "завершить программу (без сохранения в файл)";
    }

    @Override
    public String getName() {
        return "exit";
    }
}