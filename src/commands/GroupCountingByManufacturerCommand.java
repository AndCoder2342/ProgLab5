
package commands;

import manager.CollectionManager;

import java.util.Map;

/**
 * команда группировки по производителю.
 */
public class GroupCountingByManufacturerCommand implements Command {
    private final CollectionManager manager;

    public GroupCountingByManufacturerCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean execute() {
        Map<String, Long> groups = manager.groupCountingByManufacturer();
        System.out.println("Группировка по производителю:");
        groups.forEach((manufacturer, count) ->
                System.out.println(manufacturer + ": " + count)
        );
        return true;
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