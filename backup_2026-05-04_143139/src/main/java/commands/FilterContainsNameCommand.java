package commands;

import manager.CollectionManager;
import manager.Product;

import java.util.List;
import java.util.Scanner;

/**
 * kоманда фильтрации по подстроке во всех полях
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
            System.out.print("Введите подстроку для поиска (по всем полям): ");
            String substring = scanner.nextLine().trim();

            List<Product> result = manager.filterContainsName(substring);

            if (result.isEmpty()) {
                System.out.println("Ничего не найдено");
            } else {
                System.out.println("Найдено продуктов: " + result.size());
                System.out.println("========================================");
                for (Product product : result) {
                    System.out.println(product);
                    System.out.println("----------------------------------------");
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            return true;
        }
    }

    @Override
    public String getDescription() {
        return "вывести элементы, значение любого поля которых содержит заданную подстроку";
    }

    @Override
    public String getName() {
        return "filter_contains_name";
    }
}