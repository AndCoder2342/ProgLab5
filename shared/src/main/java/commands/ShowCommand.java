package commands;

import manager.Product;
import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Hashtable;

/**
 * Команда демонстрации всех элементов коллекции
 */
public class ShowCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public CommandResult execute(RequestContext context) {
        try {
            // collectionManager через рефлексию
            Object cmObject = context.getCollectionManager();

            // коллекция через рефлексию
            @SuppressWarnings("unchecked")
            Hashtable<Long, Product> collection = (Hashtable<Long, Product>)
                    cmObject.getClass().getMethod("getCollection").invoke(cmObject);

            // Stream API для получения отсортированного списка
            List<Product> products = collection.values().stream()
                    .filter(p -> p != null)
                    .sorted() // сортировка по умолчанию (Comparable)
                    .collect(Collectors.toList());

            if (products.isEmpty()) {
                return CommandResult.ok("Коллекция пуста", products);
            }

            return CommandResult.ok(
                    "Всего элементов: " + products.size(),
                    products
            );

        } catch (Exception e) {
            return CommandResult.error("Ошибка: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "вывести в стандартный поток вывода все элементы коллекции";
    }

    @Override
    public String getName() {
        return "show";
    }
}