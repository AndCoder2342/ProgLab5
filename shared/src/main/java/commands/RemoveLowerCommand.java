package commands;

import manager.Product;
import java.io.Serializable;

/**
 * Команда удаления элементов, меньших заданного
 */
public class RemoveLowerCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    private final Product product;

    public RemoveLowerCommand(Product product) {
        this.product = product;
    }

    @Override
    public String getDescription() {
        return "удалить из коллекции все элементы, меньшие, чем заданный";
    }

    @Override
    public String getName() {
        return "remove_lower";
    }

    public Product getProduct() {
        return product;
    }
}