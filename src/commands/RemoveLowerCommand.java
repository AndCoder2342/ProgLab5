
package commands;

import manager.CollectionManager;
import manager.Product;
import manager.InputHelper;

/**
 * команда удаления элементов, меньших заданного.
 */
public class RemoveLowerCommand implements Command {
    private final CollectionManager manager;

    public RemoveLowerCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean execute() {
        try {
            System.out.println("Введите продукт для сравнения:");
            Product product = InputHelper.readProductFromConsole();
            manager.removeLower(product);
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "удалить из коллекции все элементы, меньшие, чем заданный";
    }

    @Override
    public String getName() {
        return "remove_lower";
    }
}