package commands;

import manager.Product;
import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.Hashtable;

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
    public CommandResult execute(RequestContext context) {
        try {
            // получаем collectionManager как Object и делаем приведение
            Object cmObject = context.getCollectionManager();

            // используем рефлексию или приводим к Hashtable (так как CollectionManager использует Hashtable)
            @SuppressWarnings("unchecked")
            Hashtable<Long, Product> collection = (Hashtable<Long, Product>)
                    cmObject.getClass().getMethod("getCollection").invoke(cmObject);

            var toRemove = collection.values().stream()
                    .filter(p -> p != null)
                    .filter(p -> p.compareTo(product) < 0)
                    .map(Product::getId)
                    .collect(Collectors.toList());

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