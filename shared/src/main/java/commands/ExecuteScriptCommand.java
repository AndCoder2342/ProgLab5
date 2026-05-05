package commands;

import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;
import java.io.File;

/**
 * Команда выполнения скрипта
 * ВАЖНО: Эта команда выполняется на КЛИЕНТЕ, а не на сервере!
 */
public class ExecuteScriptCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    private final String filename;

    public ExecuteScriptCommand(String filename) {
        this.filename = filename;
    }


    public CommandResult execute(RequestContext context) {
        // не выполняется на сервере!
        return CommandResult.error(
                "execute_script выполняется на клиенте локально"
        );
    }


    public String getDescription() {
        return "считать и исполнить скрипт из указанного файла (выполняется на клиенте)";
    }


    public String getName() {
        return "execute_script";
    }

    public String getFilename() {
        return filename;
    }

    /**
     * валидация файла скрипта
     */
    public static boolean validateScriptFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            System.err.println("Ошибка: не указан путь к файлу скрипта");
            return false;
        }

        File file = new File(filename);
        if (!file.exists()) {
            System.err.println("Ошибка: файл не найден: " + filename);
            return false;
        }

        if (!file.canRead()) {
            System.err.println("Ошибка: нет прав на чтение файла: " + filename);
            return false;
        }

        return true;
    }
}