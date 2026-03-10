
package commands;

import manager.CollectionManager;
import manager.Product;
import manager.InputHelper;

/**
 * команда удаления элементов, превышающих заданный.
 */
public class RemoveGreaterCommand implements Command {
    private final CollectionManager manager;

    public RemoveGreaterCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean execute() {
        try {
            System.out.println("Введите продукт для сравнения:");
            Product product = InputHelper.readProductFromConsole();
            manager.removeGreater(product);
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            return true;
        }
    }

    @Override
    public String getDescription() {
        return "удалить из коллекции все элементы, превышающие заданный";
    }

    @Override
    public String getName() {
        return "remove_greater";
    }
}