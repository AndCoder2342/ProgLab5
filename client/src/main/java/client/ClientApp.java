package client;

import client.console.ConsoleReader;
import client.network.UdpClient;
import commands.*;
import shared.Request;
import shared.Response;
import manager.InputHelper;
import manager.Product;
import manager.Organization;

import java.util.Map;
import java.util.Scanner;

public class ClientApp {
    private static int userId = -1;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {

            UdpClient client = new UdpClient("localhost", 1337);
            ConsoleReader consoleReader = new ConsoleReader(scanner);


            System.out.println("=== Авторизация ===");
            String username = null;
            String password = null;

            while (true) {
                System.out.print("Введите логин (или 'reg' для регистрации): ");
                String input = scanner.nextLine().trim();

                if ("reg".equalsIgnoreCase(input)) {

                    System.out.println("=== Регистрация ===");
                    System.out.print("Придумайте логин (мин. 3 символа): ");
                    String regUsername = scanner.nextLine().trim();

                    System.out.print("Придумайте пароль (мин. 4 символа): ");
                    String regPassword = scanner.nextLine().trim();

                    // Создаём команду регистрации
                    RegisterCommand regCmd = new RegisterCommand(regUsername, regPassword);

                    // Для регистрации: логин/пароль в теле команды, в заголовке - null
                    Request regRequest = new Request(null, null, regCmd);

                    try {
                        System.out.println("Отправка запроса на регистрацию...");
                        Response regResponse = client.sendRequest(regRequest);

                        if (regResponse != null) {
                            System.out.println("\nСервер: " + regResponse.getMessage());

                            // После успешной регистрации:
                            if (regResponse.isSuccess()) {
                                System.out.println("\n Регистрация успешна! Автоматический вход...");
                                username = regUsername;
                                password = regPassword;
                                // Получаем userId из ответа
                                if (regResponse.getData() instanceof Integer) {
                                    userId = (Integer) regResponse.getData();
                                }
                                System.out.println("✓ Вы вошли как: " + username + " (ID: " + userId + ")");
                                System.out.println("===================\n");
                                break;
                            }
                        } else {
                            System.out.println("Сервер не ответил (таймаут)");
                        }
                    } catch (Exception e) {
                        System.out.println("Ошибка связи: " + e.getMessage());
                    }
                    // continue не нужен, цикл повторится сам

                } else if (!input.isEmpty()) {

                    username = input;
                    System.out.print("Введите пароль: ");
                    password = scanner.nextLine().trim();

                    // Проверяем вход простой командой (help)
                    Request loginCheck = new Request(username, password, new HelpCommand());
                    Response checkResponse = client.sendRequest(loginCheck);

                    if (checkResponse != null && checkResponse.isSuccess()) {
                        System.out.println("✓ Авторизован как: " + username);
                        System.out.println("===================\n");
                        break;
                    } else {
                        System.out.println("Неверный логин или пароль. Попробуйте снова.\n");
                    }
                }
            }

            System.out.println("Клиент запущен. Введите команду (help для списка):");


            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) break;

                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                String[] parts = input.split(" ", 2);
                String cmdName = parts[0];
                String commandArgs = parts.length > 1 ? parts[1] : null;

                Command command = null;


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
                    consoleReader.executeScript(commandArgs, client);
                    continue;
                }


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
                            System.out.println("Введите новые данные продукта:");
                            Product updateProduct = InputHelper.readProductFromConsole(scanner);
                            if (updateProduct != null) {
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
                }

                if (command != null) {
                    try {
                        // ПРАВИЛЬНО: передаём username/password для авторизации
                        Request request = new Request(username, password, command);
                        Response response = client.sendRequest(request);

                        if (response == null) {
                            System.out.println("\nСервер не ответил (таймаут). Проверьте подключение.\n");
                            continue;
                        }

                        System.out.println("\nОтвет сервера:");
                        System.out.println(response.getMessage());

                        if (response.getData() != null) {
                            System.out.println("\nДанные:");
                            printResponseData(response.getData());
                        }
                        System.out.println("─────────────────────────────\n");

                    } catch (Exception e) {
                        System.err.println("Ошибка при отправке запроса: " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Критическая ошибка клиента: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Вывод данных из ответа сервера
     */
    private static void printResponseData(Object data) {
        if (data == null) return;

        if (data instanceof String) {
            System.out.println(data);
        } else if (data instanceof Iterable) {
            for (Object item : (Iterable<?>) data) {
                System.out.println("  • " + item);
            }
        } else if (data instanceof Map) {
            ((Map<?, ?>) data).forEach((k, v) ->
                    System.out.println("  " + k + ": " + v)
            );
        } else if (data instanceof Number) {
            System.out.println("  Значение: " + data);
        } else {
            System.out.println("  " + data);
        }
    }
}