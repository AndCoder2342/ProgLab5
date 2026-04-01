package manager;

import commands.Command;

import java.util.HashMap;
import java.util.Map;

/**
 * инвокер для управления командами.
 */
public class Invoker {
    private final Map<String, Command> commandMap = new HashMap<>();
    private String[] lastCommandArgs;

    /**
     * регистрирует команду
     */
    public void registerCommand(String name, Command command) {
        commandMap.put(name.toLowerCase(), command);
    }

    /**
     * выполняет команду по строке ввода
     */
    public boolean executeCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return true;
        }

        // разбиваем команду на части
        String[] parts = input.trim().split("\\s+");
        String commandName = parts[0].toLowerCase();

        // сохраняем аргументы команды
        lastCommandArgs = parts;

        Command command = commandMap.get(commandName);
        if (command == null) {
            System.err.println("Неизвестная команда: " + commandName);
            System.out.println("Введите 'help' для списка команд");
            return true;
        }

        try {
            return command.execute();
        } catch (Exception e) {
            System.err.println("Ошибка при выполнении команды: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    /**
     * возвращает аргументы последней выполненной команды
     */
    public String[] getLastCommandArgs() {
        return lastCommandArgs;
    }

    /**
     * возвращает карту команд
     */
    public Map<String, Command> getCommandMap() {
        return commandMap;
    }
}