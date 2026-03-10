
package commands;

import manager.CollectionManager;
import manager.Product;

import java.util.List;
import java.util.Scanner;

/**
 * команда фильтрации по имени.
 */
public class FilterContainsNameCommand implements Command {
    private final CollectionManager manager;

    public FilterContainsNameCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean execute() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите подстроку для поиска: ");
            String name = scanner.nextLine().trim();

            List<Product> result = manager.filterContainsName(name);
            if (result.isEmpty()) {
                System.out.println("Ничего не найдено");
            } else {
                System.out.println("Найдено продуктов: " + result.size());
                result.forEach(System.out::println);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "вывести элементы, значение поля name которых содержит заданную подстроку";
    }

    @Override
    public String getName() {
        return "filter_contains_name";
    }
}