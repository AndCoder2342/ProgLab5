package client.console;

import commands.*;
import manager.*;
import enums.UnitOfMeasure;
import org.tinylog.Logger;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleReader {
    private final Scanner scanner = new Scanner(System.in);
    private final UUID clientId;

    public ConsoleReader(UUID clientId) {
        this.clientId = clientId;
    }

    public Request readCommand() {
        System.out.print("> ");

        if (!scanner.hasNextLine()) {
            Logger.info("Получен EOF, завершение клиента");
            return null;
        }

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return null;

        String[] parts = input.split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : null;

        try {
            Command command = parseCommand(commandName, args);
            if (command == null) {
                System.err.println("Неизвестная команда: " + commandName);
                return null;
            }

            Logger.debug("Сформирована команда: {}", command.getName());
            return new Request(clientId, command);

        } catch (ValidationException e) {
            System.err.println("Ошибка валидации: " + e.getMessage());
            Logger.warn("Ошибка валидации ввода: {}", e.getMessage());
            return null;
        }
    }

    private Command parseCommand(String name, String args) throws ValidationException {
        return switch (name) {
            case "help" -> new HelpCommand();
            case "info" -> new InfoCommand();
            case "show" -> new ShowCommand();
            case "insert" -> {
                System.out.println("Ввод данных продукта:");
                Product product = InputHelper.readProductFromConsole();
                yield new InsertCommand(product);
            }
            case "exit" -> new ExitCommand();
            // ... остальные команды
            case "save" -> {
                Logger.warn("Команда 'save' недоступна на клиенте, выполняется только на сервере");
                yield null;
            }
            default -> null;
        };
    }
}