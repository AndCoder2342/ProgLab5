package commands;

import manager.Product;
import java.io.Serializable;

/**
 * Команда добавления нового элемента
 */
public class InsertCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    private final Product product;

    public InsertCommand(Product product) {
        this.product = product;
    }

    @Override
    public String getDescription() {
        return "добавить новый элемент с заданным ключом";
    }

    @Override
    public String getName() {
        return "insert";
    }

    public Product getProduct() {
        return product;
    }
}