
package commands;

import manager.CollectionManager;
import manager.Product;
import manager.InputHelper;

import java.util.Scanner;

/**
 * команда замены элемента, если новый больше старого.
 */
public class ReplaceIfGreaterCommand implements Command {
    private final CollectionManager manager;

    public ReplaceIfGreaterCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean execute() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите ID продукта для замены: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            System.out.println("Введите новый продукт:");
            Product newProduct = InputHelper.readProductFromConsole();

            return manager.replaceIfGreater(id, newProduct);
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            return true;
        }
    }

    @Override
    public String getDescription() {
        return "заменить значение по ключу, если новое значение больше старого";
    }

    @Override
    public String getName() {
        return "replace_if_greater";
    }
}