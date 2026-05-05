package commands;

import manager.Product;
import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.Hashtable;

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
    public CommandResult execute(RequestContext context) {
        try {
            // получаем collectionManager через рефлексию
            Object cmObject = context.getCollectionManager();

            // получаем коллекцию через рефлексию
            @SuppressWarnings("unchecked")
            Hashtable<Long, Product> collection = (Hashtable<Long, Product>)
                    cmObject.getClass().getMethod("getCollection").invoke(cmObject);

            // фильтрация и удаление через Stream API
            var toRemove = collection.values().stream()
                    .filter(p -> p != null)
                    .filter(p -> p.compareTo(product) > 0) // Больше заданного
                    .map(Product::getId)
                    .collect(Collectors.toList());

            // удаление
            toRemove.forEach(id -> collection.remove(id));

            return CommandResult.ok(
                    "Удалено элементов: " + toRemove.size(),
                    toRemove.size()
            );

        } catch (Exception e) {
            return CommandResult.error("Ошибка: " + e.getMessage());
        }
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