
package commands;

import manager.CollectionManager;
import manager.Product;
import manager.InputHelper;

import java.util.Scanner;

/**
 * команда обновления элемента
 */
public class UpdateCommand implements Command {
    private final CollectionManager manager;

    public UpdateCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean execute() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите ID продукта для обновления: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            System.out.println("Введите новые данные продукта:");
            Product newProduct = InputHelper.readProductFromConsole();

            return manager.update(id, newProduct);
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: ID должен быть числом");
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении: " + e.getMessage());
            return true;
        }
    }

    @Override
    public String getDescription() {
        return "обновить значение элемента коллекции, id которого равен заданному";
    }

    @Override
    public String getName() {
        return "update";
    }
}