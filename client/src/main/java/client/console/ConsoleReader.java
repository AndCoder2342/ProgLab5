package client.console;

import client.network.UdpClient;
import commands.*;
import shared.Request;
import shared.Response;
import manager.InputHelper;
import manager.Product;
import manager.Organization;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Сканер команд из консоли и скриптов
 */
public class ConsoleReader {
    private final Scanner scanner;

    public ConsoleReader(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Выполняет скрипт: читает файл и отправляет команды на сервер
     */
    public void executeScript(String filename, UdpClient client) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.err.println("!!! Файл не найден: " + filename);
                return;
            }
            if (!file.canRead()) {
                System.err.println("!!! Нет прав на чтение: " + filename);
                return;
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

                    // парсим и выполняем команду
                    processCommand(line, client);
                }
            }

            System.out.println("Скрипт завершён: " + filename);

        } catch (FileNotFoundException e) {
            System.err.println("!!! Ошибка: файл не найден");
        } catch (Exception e) {
            System.err.println("!!! Ошибка при выполнении скрипта: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает одну команду из скрипта
     */
    private void processCommand(String input, UdpClient client) {
        String[] parts = input.split(" ", 2);
        String cmdName = parts[0];
        String args = parts.length > 1 ? parts[1] : null;

        // пропускаем команды, которые нельзя выполнять из скрипта
        if ("execute_script".equals(cmdName) || "exit".equals(cmdName)) {
            System.out.println("!!! Команда '" + cmdName + "' пропущена в скрипте");
            return;
        }

        // упрощенная обработка

        Command command = null;

        switch (cmdName) {
            case "help":
            case "info":
            case "show":
            case "clear":
            case "save":
            case "group_counting_by_manufacturer":
                command = createSimpleCommand(cmdName);
                break;

            case "remove_key":
                if (args != null) {
                    try {
                        command = new RemoveKeyCommand(Long.parseLong(args.trim()));
                    } catch (NumberFormatException ignored) {}
                }
                break;

            case "filter_contains_name":
                if (args != null && !args.isEmpty()) {
                    command = new FilterContainsNameCommand(args.trim());
                }
                break;
        }

        if (command != null) {
            try {
                Request request = new Request(client.getClientId(), command);
                Response response = client.sendRequest(request);
                System.out.println("    Ответ: " + response.getMessage());
            } catch (Exception e) {
                System.err.println("!!! Ошибка: " + e.getMessage());
            }
        } else {
            System.out.println("!!! Команда не распознана или требует интерактивного ввода");
        }
    }

    /**
     * Создаёт команду без аргументов по имени
     */
    private Command createSimpleCommand(String name) {
        return switch (name) {
            case "help" -> new HelpCommand();
            case "info" -> new InfoCommand();
            case "show" -> new ShowCommand();
            case "clear" -> new ClearCommand();
            case "save" -> new SaveCommand();
            case "group_counting_by_manufacturer" -> new GroupCountingByManufacturerCommand();
            default -> null;
        };
    }
}