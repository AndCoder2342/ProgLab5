package commands;

import manager.Product;
import commands.Command;
import java.io.Serializable;

/**
 * Команда обновления элемента
 */
public class UpdateCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final Product newProduct;

    public UpdateCommand(Long id, Product newProduct) {
        this.id = id;
        this.newProduct = newProduct;
    }

    @Override
    public String getDescription() {
        return "обновить значение элемента коллекции, id которого равен заданному";
    }

    @Override
    public String getName() {
        return "update";
    }

    // геттеры для сервера
    public Long getId() { return id; }
    public Product getNewProduct() { return newProduct; }
    public Product getProduct() {
        return newProduct;
    }
}