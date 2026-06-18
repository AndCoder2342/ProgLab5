package commands;

import java.io.Serializable;

/**
 * Команда группировки по производителю
 */
public class GroupCountingByManufacturerCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "сгруппировать элементы коллекции по значению поля manufacturer";
    }

    @Override
    public String getName() {
        return "group_counting_by_manufacturer";
    }
}