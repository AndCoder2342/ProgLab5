package manager;

import commands.Command;

import java.util.HashMap;
import java.util.Map;

/**
 * класс инвокер
 */
public class Invoker {
    private final Map<String, Command> commandMap = new HashMap<>();

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

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();

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
     * возвращает мапу команд
     */
    public Map<String, Command> getCommandMap() {
        return commandMap;
    }
}