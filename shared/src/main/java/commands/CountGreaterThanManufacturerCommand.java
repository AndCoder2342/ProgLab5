package commands;

import manager.Organization;
import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;
import java.util.Hashtable;
import manager.Product;

/**
 * Команда подсчета элементов с производителем больше заданного
 */
public class CountGreaterThanManufacturerCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    private final Organization organization;

    public CountGreaterThanManufacturerCommand(Organization organization) {
        this.organization = organization;
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

            // Stream API для подсчета
            long count = collection.values().stream()
                    .filter(p -> p != null)
                    .filter(p -> p.getManufacturer() != null)
                    .filter(p -> {
                        // сравниваем имена производителей лексикографически
                        String prodName = p.getManufacturer().getName();
                        String orgName = organization.getName();
                        return prodName != null && orgName != null &&
                                prodName.compareTo(orgName) > 0;
                    })
                    .count();

            return CommandResult.ok(
                    "Количество элементов: " + count,
                    count
            );

        } catch (Exception e) {
            return CommandResult.error("Ошибка: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "вывести количество элементов, значение поля manufacturer которых больше заданного";
    }

    @Override
    public String getName() {
        return "count_greater_than_manufacturer";
    }

    public Organization getOrganization() {
        return organization;
    }
}