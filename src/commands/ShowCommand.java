
package commands;

import manager.CollectionManager;
import manager.Product;

/**
 * команда показа всех элементов коллекции.
 */
public class ShowCommand implements Command {
    private final CollectionManager manager;

    public ShowCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean execute() {
        if (manager.getCollection().isEmpty()) {
            System.out.println("Коллекция пуста");
            return true;
        }

        manager.getAll().stream()
                .sorted()
                .forEach(product -> System.out.println(product));
        return true;
    }

    @Override
    public String getDescription() {
        return "вывести в стандартный поток вывода все элементы коллекции";
    }

    @Override
    public String getName() {
        return "show";
    }
}