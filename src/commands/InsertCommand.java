
package commands;

import manager.CollectionManager;
import manager.Product;
import manager.InputHelper;

/**
 * команда добавления нового элементак
 */
public class InsertCommand implements Command {
    private final CollectionManager manager;

    public InsertCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean execute() {
        try {
            System.out.println("Добавление нового продукта:");
            Product product = InputHelper.readProductFromConsole();
            manager.insert(product);
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при добавлении продукта: " + e.getMessage());
            return true;
        }
    }

    @Override
    public String getDescription() {
        return "добавить новый элемент с заданным ключом";
    }

    @Override
    public String getName() {
        return "insert";
    }
}