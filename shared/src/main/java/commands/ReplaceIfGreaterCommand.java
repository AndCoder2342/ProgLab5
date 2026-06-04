package commands;

import manager.Product;
import commands.Command;
import java.io.Serializable;

/**
 * Команда замены элемента, если новый больше старого
 */
public class ReplaceIfGreaterCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final Product newProduct;

    public ReplaceIfGreaterCommand(Long id, Product newProduct) {
        this.id = id;
        this.newProduct = newProduct;
    }

    @Override
    public String getDescription() {
        return "заменить значение по ключу, если новое значение больше старого";
    }

    @Override
    public String getName() {
        return "replace_if_greater";
    }

    public Long getId() {
        return id;
    }

    public Product getNewProduct() {
        return newProduct;
    }

    public Product getProduct() {
        return newProduct;
    }
}