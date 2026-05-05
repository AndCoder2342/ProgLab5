package client;

import client.console.ConsoleReader;
import client.network.UdpClient;
import commands.*;
import shared.Request;
import shared.Response;
import manager.InputHelper;
import manager.Product;
import manager.Organization;

import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) {
        try {
            // подключение к серверу
            UdpClient client = new UdpClient("localhost", 1337);
            Scanner scanner = new Scanner(System.in);
            ConsoleReader consoleReader = new ConsoleReader(scanner);

            System.out.println("Клиент запущен");
            System.out.println("Введите команду (help для списка):");

            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) break;

                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                // парсим
                String[] parts = input.split(" ", 2);
                String cmdName = parts[0];
                String commandArgs = parts.length > 1 ? parts[1] : null;

                Command command = null;

                // обработка

                // обрабатываются на клиенте:
                if ("exit".equals(cmdName)) {
                    System.out.println("Выход...");
                    client.close();
                    return;
                }

                if ("execute_script".equals(cmdName)) {
                    if (commandArgs == null || commandArgs.isEmpty()) {
                        System.out.println("Использование: execute_script <filename>");
                        continue;
                    }
                    // выполняем скрипт локально на клиенте
                    executeScript(commandArgs, client, consoleReader);
                    continue;
                }

                // простые команды без аргументов
                switch (cmdName) {
                    case "help":
                        command = new HelpCommand();
                        break;
                    case "info":
                        command = new InfoCommand();
                        break;
                    case "show":
                        command = new ShowCommand();
                        break;
                    case "clear":
                        command = new ClearCommand();
                        break;
                    case "save":
                        command = new SaveCommand();
                        break;
                    case "group_counting_by_manufacturer":
                        command = new GroupCountingByManufacturerCommand();
                        break;

                    // команды с одним аргументом строка или число
                    case "remove_key":
                        if (commandArgs == null) {
                            System.out.println("Использование: remove_key <id>");
                            continue;
                        }
                        try {
                            Long id = Long.parseLong(commandArgs.trim());
                            command = new RemoveKeyCommand(id);
                        } catch (NumberFormatException e) {
                            System.out.println("Ошибка: ID должен быть числом");
                            continue;
                        }
                        break;

                    case "filter_contains_name":
                        if (commandArgs == null || commandArgs.isEmpty()) {
                            System.out.println("Использование: filter_contains_name <подстрока>");
                            continue;
                        }
                        command = new FilterContainsNameCommand(commandArgs.trim());
                        break;

                    // команды, требующие ввода сложного объекта (Product)
                    case "insert":
                        System.out.println("Введите данные нового продукта:");
                        Product newProduct = InputHelper.readProductFromConsole(scanner);
                        if (newProduct != null) {
                            command = new InsertCommand(newProduct);
                        } else {
                            System.out.println("Ошибка ввода продукта");
                            continue;
                        }
                        break;

                    case "remove_greater":
                        System.out.println("Введите продукт для сравнения (удалит БОЛЬШИЕ):");
                        Product greaterProduct = InputHelper.readProductFromConsole(scanner);
                        if (greaterProduct != null) {
                            command = new RemoveGreaterCommand(greaterProduct);
                        } else {
                            System.out.println("Ошибка ввода продукта");
                            continue;
                        }
                        break;

                    case "remove_lower":
                        System.out.println("Введите продукт для сравнения (удалит МЕНЬШИЕ):");
                        Product lowerProduct = InputHelper.readProductFromConsole(scanner);
                        if (lowerProduct != null) {
                            command = new RemoveLowerCommand(lowerProduct);
                        } else {
                            System.out.println("Ошибка ввода продукта");
                            continue;
                        }
                        break;

                    case "count_greater_than_manufacturer":
                        System.out.println("Введите организацию для сравнения:");
                        Organization org = InputHelper.readOrganization(scanner);
                        if (org != null) {
                            command = new CountGreaterThanManufacturerCommand(org);
                        } else {
                            System.out.println("Ошибка ввода организации");
                            continue;
                        }
                        break;

                    case "replace_if_greater":
                        if (commandArgs == null) {
                            System.out.println("Использование: replace_if_greater <id>");
                            continue;
                        }
                        try {
                            Long replaceId = Long.parseLong(commandArgs.trim());
                            System.out.println("Введите новый продукт:");
                            Product replaceProduct = InputHelper.readProductFromConsole(scanner);
                            if (replaceProduct != null) {
                                command = new ReplaceIfGreaterCommand(replaceId, replaceProduct);
                            } else {
                                System.out.println("Ошибка ввода продукта");
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Ошибка: ID должен быть числом");
                            continue;
                        }
                        break;

                    case "update":
                        if (commandArgs == null) {
                            System.out.println("Использование: update <id>");
                            continue;
                        }
                        try {
                            Long updateId = Long.parseLong(commandArgs.trim());
                            System.out.println("Введите обновлённые данные продукта:");
                            Product updateProduct = InputHelper.readProductFromConsole(scanner);
                            if (updateProduct != null) {
                                updateProduct.setId(updateId); // сохраняем ключ
                                command = new UpdateCommand(updateId, updateProduct);
                            } else {
                                System.out.println("Ошибка ввода продукта");
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Ошибка: ID должен быть числом");
                            continue;
                        }
                        break;

                    default:
                        System.out.println("!!! Неизвестная команда. Введите 'help' для списка.");
                        continue;
                }

                // отправка запроса на сервер
                if (command != null) {
                    try {
                        Request request = new Request(client.getClientId(), command);
                        Response response = client.sendRequest(request);

                        // вывод
                        System.out.println("\nОтвет сервера:");
                        System.out.println(response.getMessage());

                        // если в ответе есть данные
                        if (response.getData() != null) {
                            System.out.println("\nДанные:");
                            printResponseData(response.getData());
                        }
                        System.out.println("─────────────────────────────\n");

                    } catch (Exception e) {
                        System.err.println("!!! Ошибка при отправке запроса: " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("!!! Критическая ошибка клиента: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Вывод данных из ответа сервера (списки, карты, простые значения)
     */
    @SuppressWarnings("unchecked")
    private static void printResponseData(Object data) {
        if (data == null) return;

        if (data instanceof String) {
            System.out.println(data);
        } else if (data instanceof Iterable) {
            for (Object item : (Iterable<?>) data) {
                System.out.println("  • " + item);
            }
        } else if (data instanceof java.util.Map) {
            ((java.util.Map<?, ?>) data).forEach((k, v) ->
                    System.out.println("  " + k + ": " + v)
            );
        } else if (data instanceof Number) {
            System.out.println("  Значение: " + data);
        } else {
            System.out.println("  " + data);
        }
    }

    /**
     * Выполнение скрипта на клиенте
     * Читает файл и отправляет команды по одной
     */
    private static void executeScript(String filename, UdpClient client, ConsoleReader consoleReader) {
        consoleReader.executeScript(filename, client);
    }
}