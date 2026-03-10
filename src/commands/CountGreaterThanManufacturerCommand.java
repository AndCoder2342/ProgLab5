
package commands;

import manager.CollectionManager;
import manager.Organization;
import manager.InputHelper;

/**
 * команда подсчета элементов с производителем больше заданногок
 */
public class CountGreaterThanManufacturerCommand implements Command {
    private final CollectionManager manager;

    public CountGreaterThanManufacturerCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean execute() {
        try {
            System.out.println("Введите организацию для сравнения:");
            Organization org = InputHelper.readOrganization();
            long count = manager.countGreaterThanManufacturer(org);
            System.out.println("Количество элементов: " + count);
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            return false;
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
}