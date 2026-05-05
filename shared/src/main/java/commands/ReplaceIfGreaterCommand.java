package commands;

import manager.Product;
import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;
import java.util.Hashtable;

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
    public CommandResult execute(RequestContext context) {
        try {
            // получаем collectionManager через рефлексию
            Object cmObject = context.getCollectionManager();

            // получаем коллекцию
            @SuppressWarnings("unchecked")
            Hashtable<Long, Product> collection = (Hashtable<Long, Product>)
                    cmObject.getClass().getMethod("getCollection").invoke(cmObject);

            // проверяем существование
            Product oldProduct = collection.get(id);
            if (oldProduct == null) {
                return CommandResult.error("Продукт с ID " + id + " не найден");
            }

            // сравниваем
            if (newProduct.compareTo(oldProduct) > 0) {
                // новый продукт больше - заменяем
                newProduct.setId(id);
                newProduct.setCreationDate(oldProduct.getCreationDate());
                collection.put(id, newProduct);

                return CommandResult.ok(
                        "Продукт с ID " + id + " успешно заменен",
                        id
                );
            } else {
                return CommandResult.error(
                        "Новый продукт не больше старого, замена не произведена"
                );
            }

        } catch (Exception e) {
            return CommandResult.error("Ошибка: " + e.getMessage());
        }
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
}