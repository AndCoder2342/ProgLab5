package commands;

import manager.CollectionManager;
import manager.Invoker;
import manager.ScriptDepthTracker;

import java.io.File;
import java.util.Scanner;

/**
 * команда выполнения скрипта
 */
public class ExecuteScriptCommand implements commands.Command {
    private final CollectionManager manager;
    private final Invoker invoker;

    public ExecuteScriptCommand(CollectionManager manager, Invoker invoker) {
        this.manager = manager;
        this.invoker = invoker;
    }

    @Override
    public boolean execute() {
        try {
            String filename = null;

            // проверяем есть ли аргумент команды
            String[] args = invoker.getLastCommandArgs();
            if (args != null && args.length > 1) {
                filename = args[1].trim();
            } else {
                // если нет аргумента, запрашиваем интерактивно
                Scanner scanner = new Scanner(System.in);
                System.out.print("Введите путь к файлу скрипта: ");
                filename = scanner.nextLine().trim();
            }

            if (filename == null || filename.isEmpty()) {
                System.err.println("Ошибка: не указан путь к файлу скрипта");
                return true;
            }

            File file = new File(filename);
            if (!file.exists()) {
                System.err.println("Ошибка: файл не найден: " + filename);
                return true;
            }

            if (!file.canRead()) {
                System.err.println("Ошибка: нет прав на чтение файла: " + filename);
                return true;
            }

            // проверяем глубину рекурсии
            if (!ScriptDepthTracker.enterScript()) {
                System.err.println("Ошибка: превышена максимальная глубина вложенности скриптов (" +
                        ScriptDepthTracker.MAX_DEPTH + ")");
                ScriptDepthTracker.exitScript();
                return true;
            }

            System.out.println("Выполнение скрипта: " + filename);

            try (Scanner fileScanner = new Scanner(file)) {
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();

                    // пропускаем пустые строки и комментарии
                    if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                        continue;
                    }

                    System.out.println(">>> " + line);

                    // выполняем команду
                    boolean continueExecution = invoker.executeCommand(line);

                    if (!continueExecution) {
                        System.out.println("Скрипт завершён командой exit");
                        break;
                    }
                }
            }

            ScriptDepthTracker.exitScript();
            System.out.println("Скрипт завершён: " + filename);
            return true;

        } catch (Exception e) {
            ScriptDepthTracker.exitScript();
            System.err.println("Ошибка при выполнении скрипта: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public String getDescription() {
        return "считать и исполнить скрипт из указанного файла (поддерживается вложенность до 3 уровней)";
    }

    @Override
    public String getName() {
        return "execute_script";
    }
}