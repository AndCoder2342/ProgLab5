package commands;

import manager.Product;
import java.io.Serializable;

/**
 * Команда удаления элементов, превышающих заданный
 */
public class RemoveGreaterCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    private final Product product;

    public RemoveGreaterCommand(Product product) {
        this.product = product;
    }

    @Override
    public String getDescription() {
        return "удалить из коллекции все элементы, превышающие заданный";
    }

    @Override
    public String getName() {
        return "remove_greater";
    }

    public Product getProduct() {
        return product;
    }
}