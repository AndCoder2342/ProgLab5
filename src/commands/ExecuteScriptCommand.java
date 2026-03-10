
package commands;

import manager.CollectionManager;
import manager.Invoker;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * команда выполнения скрипта
 */
public class ExecuteScriptCommand implements Command {
    private final CollectionManager manager;
    private final Invoker invoker;

    public ExecuteScriptCommand(CollectionManager manager, Invoker invoker) {
        this.manager = manager;
        this.invoker = invoker;
    }

    @Override
    public boolean execute() {
        try {
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            System.out.print("Введите путь к файлу скрипта: ");
            String filename = scanner.nextLine().trim();

            File file = new File(filename);
            if (!file.exists() || !file.canRead()) {
                System.err.println("Ошибка: файл не найден или не доступен для чтения");
                return false;
            }

            try (Scanner fileScanner = new Scanner(file)) {
                // В методе execute() добавьте проверку:
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    System.out.println("Выполнение: " + line);
                    boolean continueExecution = invoker.executeCommand(line);
                    if (!continueExecution) {
                        break; // Если команда вернула false (например exit)
                    }
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Ошибка при чтении скрипта: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "считать и исполнить скрипт из указанного файла";
    }

    @Override
    public String getName() {
        return "execute_script";
    }
}