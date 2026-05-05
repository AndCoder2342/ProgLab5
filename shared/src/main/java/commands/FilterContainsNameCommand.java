package commands;

import manager.Product;
import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Hashtable;

/**
 * Команда фильтрации по подстроке во всех полях
 */
public class FilterContainsNameCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    private final String substring;

    public FilterContainsNameCommand(String substring) {
        this.substring = substring;
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

            // фильтрация через Stream API
            List<Product> result = collection.values().stream()
                    .filter(product -> product != null)
                    .filter(product -> containsInAnyField(product, substring))
                    .sorted()
                    .collect(Collectors.toList());

            if (result.isEmpty()) {
                return CommandResult.ok("Ничего не найдено", result);
            } else {
                return CommandResult.ok(
                        "Найдено продуктов: " + result.size(),
                        result
                );
            }

        } catch (Exception e) {
            return CommandResult.error("Ошибка: " + e.getMessage());
        }
    }

    /**
     * проверяет наличие подстроки в любом поле продукта
     */
    private boolean containsInAnyField(Product product, String substring) {
        if (substring == null || substring.isEmpty()) {
            return true;
        }

        String lowerSubstring = substring.toLowerCase();

        if (product.getId() != null && String.valueOf(product.getId()).contains(lowerSubstring)) {
            return true;
        }

        if (product.getName() != null && product.getName().toLowerCase().contains(lowerSubstring)) {
            return true;
        }

        if (product.getCoordinates() != null) {
            if (product.getCoordinates().getX() != null &&
                    String.valueOf(product.getCoordinates().getX()).contains(lowerSubstring)) {
                return true;
            }
            if (product.getCoordinates().getY() != null &&
                    String.valueOf(product.getCoordinates().getY()).contains(lowerSubstring)) {
                return true;
            }
        }

        if (String.valueOf(product.getPrice()).contains(lowerSubstring)) {
            return true;
        }

        if (product.getCreationDate() != null) {
            String dateString = product.getCreationDate().toString().toLowerCase();
            if (dateString.contains(lowerSubstring)) {
                return true;
            }
        }

        if (product.getUnitOfMeasure() != null) {
            String unitString = product.getUnitOfMeasure().name().toLowerCase();
            if (unitString.contains(lowerSubstring)) {
                return true;
            }
        }

        if (product.getManufacturer() != null) {
            var org = product.getManufacturer();

            if (org.getId() != null && String.valueOf(org.getId()).contains(lowerSubstring)) {
                return true;
            }

            if (org.getName() != null && org.getName().toLowerCase().contains(lowerSubstring)) {
                return true;
            }

            if (org.getFullName() != null && org.getFullName().toLowerCase().contains(lowerSubstring)) {
                return true;
            }

            if (org.getAnnualTurnover() != null &&
                    String.valueOf(org.getAnnualTurnover()).contains(lowerSubstring)) {
                return true;
            }

            if (String.valueOf(org.getEmployeesCount()).contains(lowerSubstring)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getDescription() {
        return "вывести элементы, значение любого поля которых содержит заданную подстроку";
    }

    @Override
    public String getName() {
        return "filter_contains_name";
    }

    public String getSubstring() {
        return substring;
    }
}