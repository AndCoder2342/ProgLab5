
package commands;

import manager.CollectionManager;

import java.util.Scanner;

/**
 * команда удаления элемента по ключу.
 */
public class RemoveKeyCommand implements Command {
    private final CollectionManager manager;

    public RemoveKeyCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean execute() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите ключ для удаления: ");
            Long key = Long.parseLong(scanner.nextLine().trim());
            manager.removeKey(key);
            return true;
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: ключ должен быть числом");
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            return true;
        }
    }

    @Override
    public String getDescription() {
        return "удалить элемент из коллекции по его ключу";
    }

    @Override
    public String getName() {
        return "remove_key";
    }
}