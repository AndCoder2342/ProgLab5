package commands;

import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;
import java.util.Map;
import java.util.Hashtable;
import java.util.stream.Collectors;
import manager.Product;

/**
 * Команда группировки по производителю.
 */
public class GroupCountingByManufacturerCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public CommandResult execute(RequestContext context) {
        try {
            // получаем collectionManager через рефлексию
            Object cmObject = context.getCollectionManager();

            // получаем коллекцию через рефлексию
            @SuppressWarnings("unchecked")
            Hashtable<Long, Product> collection = (Hashtable<Long, Product>)
                    cmObject.getClass().getMethod("getCollection").invoke(cmObject);

            // Stream API для группировки
            Map<String, Long> groups = collection.values().stream()
                    .filter(p -> p != null)
                    .collect(Collectors.groupingBy(
                            p -> p.getManufacturer() != null ? p.getManufacturer().getName() : "null",
                            Collectors.counting()
                    ));

            // формируем красивое сообщение
            StringBuilder result = new StringBuilder("Группировка по производителю:\n");
            groups.forEach((manufacturer, count) ->
                    result.append("  ").append(manufacturer).append(": ").append(count).append("\n")
            );

            return CommandResult.ok(
                    "Группировка выполнена. Всего групп: " + groups.size(),
                    groups
            );

        } catch (Exception e) {
            return CommandResult.error("Ошибка: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "сгруппировать элементы коллекции по значению поля manufacturer";
    }

    @Override
    public String getName() {
        return "group_counting_by_manufacturer";
    }
}