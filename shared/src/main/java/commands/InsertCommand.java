package commands;

import manager.Product;
import shared.RequestContext;
import shared.CommandResult;
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
    public CommandResult execute(RequestContext context) {
        try {
            //  получаем объект менеджера
            Object cmObject = context.getCollectionManager();

            // вызываем метод insert(product) через рефлексию
            // серверный CollectionManager должен иметь метод insert(Product)
            cmObject.getClass().getMethod("insert", Product.class).invoke(cmObject, product);

            return CommandResult.ok(
                    "Продукт успешно добавлен",
                    product.getId()
            );

        } catch (Exception e) {
            return CommandResult.error("Ошибка при добавлении: " + e.getMessage());
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

    public Product getProduct() {
        return product;
    }
}