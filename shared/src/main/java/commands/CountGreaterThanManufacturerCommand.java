package commands;

import manager.Organization;
import java.io.Serializable;

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