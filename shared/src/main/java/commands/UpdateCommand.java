package commands;

import manager.Product;
import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;

/**
 * Команда обновления элемента
 * Теперь это просто DTO (Data Transfer Object)
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
    public CommandResult execute(RequestContext context) {
        return CommandResult.ok("Команда update принята. ID: " + id, null);
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
}