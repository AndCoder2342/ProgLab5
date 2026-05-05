package manager;

import manager.Product;
import manager.Organization;
import manager.Coordinates;
import enums.UnitOfMeasure;
import java.util.Scanner;

/**
 * Утилита для ввода данных с консоли
 */
public class InputHelper {

    /**
     * Считывает Product с консоли
     */
    public static Product readProductFromConsole(Scanner scanner) {
        try {
            Product product = new Product();

            System.out.print("  Введите название продукта: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("Название не может быть пустым");
            product.setName(name);

            System.out.print("  Введите координату X (целое число): ");
            int x = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("  Введите координату Y (число): ");
            float y = Float.parseFloat(scanner.nextLine().trim());

            // создаём Coordinates через сеттеры
            Coordinates coords = new Coordinates();
            coords.setX(x);
            coords.setY(y);
            product.setCoordinates(coords);

            System.out.print("  Введите цену (должна быть > 0): ");
            int price = Integer.parseInt(scanner.nextLine().trim());
            if (price <= 0) throw new IllegalArgumentException("Цена должна быть больше 0");
            product.setPrice(price);

            System.out.print("  Введите единицу измерения (KILOGRAM, PIECES, BOXES или пустую строку для null): ");
            String unitStr = scanner.nextLine().trim().toUpperCase();
            if (!unitStr.isEmpty()) {
                try {
                    product.setUnitOfMeasure(UnitOfMeasure.valueOf(unitStr));
                } catch (IllegalArgumentException e) {
                    System.out.println("!!! Неверная единица измерения, установлено null");
                }
            }

            System.out.println("Ввод данных производителя (Organization)");
            Organization org = readOrganization(scanner);
            product.setManufacturer(org);

            return product;

        } catch (NumberFormatException e) {
            System.err.println("!!! Ошибка формата числа: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("!!! Ошибка ввода: " + e.getMessage());
            return null;
        }
    }

    /**
     * Считывает Organization с консоли
     */
    public static Organization readOrganization(Scanner scanner) {
        try {
            Organization org = new Organization();

            System.out.print("    Введите название организации: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("!!! Название пустое, организация будет null");
                return null;
            }
            org.setName(name);

            System.out.print("    Введите полное название (или пустую строку): ");
            String fullName = scanner.nextLine().trim();
            if (!fullName.isEmpty()) {
                org.setFullName(fullName);
            }

            System.out.print("  Введите годовой оборот (или пустую строку): ");
            String turnoverStr = scanner.nextLine().trim();
            if (!turnoverStr.isEmpty()) {
                Long turnover = Long.parseLong(turnoverStr);
                org.setAnnualTurnover(turnover);
            }

            System.out.print("    Введите количество сотрудников (должно быть >= 0): ");
            int employees = Integer.parseInt(scanner.nextLine().trim());
            if (employees < 0) throw new IllegalArgumentException("Количество сотрудников не может быть отрицательным");
            org.setEmployeesCount(employees);

            System.out.print("    Введите тип организации (COMMERCIAL, NONPROFIT, GOVERNMENT или пустую строку): ");
            String typeStr = scanner.nextLine().trim().toUpperCase();
            if (!typeStr.isEmpty()) {
                try {
                } catch (IllegalArgumentException ignored) {}
            }

            return org;

        } catch (Exception e) {
            System.err.println("!!! Ошибка ввода организации: " + e.getMessage());
            return null;
        }
    }
}