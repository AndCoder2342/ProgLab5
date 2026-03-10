
import commands.*;
import manager.CollectionManager;
import manager.Invoker;

import java.util.Scanner;

/**
 * Главный класс приложения.
 * Запускает интерактивный режим управления коллекцией продуктов.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Менеджер коллекции продуктов ===");
        System.out.println("Для выхода введите 'exit' или нажмите Ctrl+D");


        CollectionManager collectionManager = new CollectionManager();

        try {
            collectionManager.initialize();
        } catch (RuntimeException e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            System.err.println("Убедитесь, что файл products.xml существует");
            System.exit(1);
        }

        Invoker invoker = new Invoker();

        // регистрация команд
        invoker.registerCommand("help", new HelpCommand(invoker.getCommandMap()));
        invoker.registerCommand("info", new InfoCommand(collectionManager));
        invoker.registerCommand("show", new ShowCommand(collectionManager));
        invoker.registerCommand("insert", new InsertCommand(collectionManager));
        invoker.registerCommand("update", new UpdateCommand(collectionManager));
        invoker.registerCommand("remove_key", new RemoveKeyCommand(collectionManager));
        invoker.registerCommand("clear", new ClearCommand(collectionManager));
        invoker.registerCommand("save", new SaveCommand(collectionManager));
        invoker.registerCommand("execute_script", new ExecuteScriptCommand(collectionManager, invoker));
        invoker.registerCommand("exit", new ExitCommand());
        invoker.registerCommand("remove_greater", new RemoveGreaterCommand(collectionManager));
        invoker.registerCommand("remove_lower", new RemoveLowerCommand(collectionManager));
        invoker.registerCommand("replace_if_greater", new ReplaceIfGreaterCommand(collectionManager));
        invoker.registerCommand("group_counting_by_manufacturer", new GroupCountingByManufacturerCommand(collectionManager));
        invoker.registerCommand("count_greater_than_manufacturer", new CountGreaterThanManufacturerCommand(collectionManager));
        invoker.registerCommand("filter_contains_name", new FilterContainsNameCommand(collectionManager));

        System.out.println("Программа запущена. Введите 'help' для списка команд.");


        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.print("> ");

            try {
                // проверяем есть ли следующая строка (Ctrl+D вернет false)
                if (!scanner.hasNextLine()) {
                    System.out.println("\nПолучен сигнал EOF (Ctrl+D). Завершение работы...");
                    break;
                }

                String input = scanner.nextLine();


                if (input.trim().isEmpty()) {
                    continue;
                }

                running = invoker.executeCommand(input);

            } catch (java.util.NoSuchElementException e) {
                // Ctrl+D может вызвать эту ошибку
                System.out.println("\nПолучен сигнал EOF. Завершение работы...");
                break;
            } catch (Exception e) {
                System.err.println("Ошибка при вводе: " + e.getMessage());
                // пересоздаем сканер после ошибки
                scanner = new Scanner(System.in);
            }
        }

        System.out.println("Программа завершена.");
        scanner.close();
    }
}