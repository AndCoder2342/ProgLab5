
package manager;

import enums.UnitOfMeasure;

import java.util.Scanner;

/**
 * вспомогательный класс для ввода данных с консоли
 */
public class InputHelper {
    private static Scanner scanner = new Scanner(System.in);

    /**
     * пересоздает сканер (нужно после EOF).
     */
    public static void resetScanner() {
        scanner = new Scanner(System.in);
    }

    /**
     * читает продукт с консоли.
     */
    public static Product readProductFromConsole() {
        Product product = new Product();

        try {

            product.setName(readString("Введите название", false));


            System.out.println("Ввод координат:");
            product.setCoordinates(readCoordinates());


            product.setPrice(readInt("Введите цену (больше 0)", 1, Integer.MAX_VALUE));


            System.out.println("Единицы измерения:");
            for (UnitOfMeasure unit : UnitOfMeasure.values()) {
                System.out.println("  " + unit.name());
            }
            String unitStr = readString("Введите единицу измерения (или пустую строку для null)", true);
            if (!unitStr.isEmpty()) {
                try {
                    product.setUnitOfMeasure(UnitOfMeasure.valueOf(unitStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    System.err.println("Неверная единица измерения, установлено null");
                }
            }


            String orgChoice = readString("Ввести производителя? (да/нет)", false);
            if (orgChoice.equalsIgnoreCase("да") || orgChoice.equalsIgnoreCase("yes")) {
                product.setManufacturer(readOrganization());
            }

            return product;
        } catch (java.util.NoSuchElementException e) {
            System.out.println("\nВвод прерван (Ctrl+D)");
            resetScanner();
            throw new RuntimeException("Ввод прерван пользователем");
        }
    }

    /**
     * читает координаты с консоли
     */
    private static Coordinates readCoordinates() {
        Coordinates coords = new Coordinates();
        coords.setX(readInt("Введите координату X (макс. 875)", null, 875));
        coords.setY(readFloat("Введите координату Y"));
        return coords;
    }

    /**
     * читает организацию с консоли
     */
    public static Organization readOrganization() {
        Organization org = new Organization();

        try {
            org.setName(readString("Введите название", false));
            org.setFullName(readString("Введите полное название (или пустую строку)", true));
            org.setAnnualTurnover(readLong("Введите годовой оборот (больше 0)", 1L, Long.MAX_VALUE));
            org.setEmployeesCount(readInt("Введите количество сотрудников (больше 0)", 1, Integer.MAX_VALUE));

            return org;
        } catch (java.util.NoSuchElementException e) {
            System.out.println("\nВвод прерван (Ctrl+D)");
            resetScanner();
            throw new RuntimeException("Ввод прерван пользователем");
        }
    }

    /**
     * читает строку с консоли с валидацией
     */
    private static String readString(String prompt, boolean allowEmpty) {
        while (true) {
            System.out.print(prompt + ": ");

            if (!scanner.hasNextLine()) {
                throw new java.util.NoSuchElementException("EOF получен");
            }

            String input = scanner.nextLine().trim();

            if (allowEmpty && input.isEmpty()) {
                return "";
            }

            if (!input.isEmpty()) {
                return input;
            }

            System.err.println("Ошибка: поле не может быть пустым");
        }
    }

    /**
     * читает целое число с консоли с валидацией
     */
    private static int readInt(String prompt, Integer min, Integer max) {
        while (true) {
            System.out.print(prompt + ": ");

            if (!scanner.hasNextLine()) {
                throw new java.util.NoSuchElementException("EOF получен");
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.err.println("введите число");
                continue;
            }

            try {
                int value = Integer.parseInt(input);

                if (min != null && value < min) {
                    System.err.println("значение должно быть не меньше " + min);
                    continue;
                }

                if (max != null && value > max) {
                    System.err.println("значение должно быть не больше " + max);
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                System.err.println("введите корректное целое число");
            }
        }
    }

    /**
     * читает long с консоли с валидацией
     */
    private static long readLong(String prompt, Long min, Long max) {
        while (true) {
            System.out.print(prompt + ": ");

            if (!scanner.hasNextLine()) {
                throw new java.util.NoSuchElementException("EOF получен");
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.err.println("введите число");
                continue;
            }

            try {
                long value = Long.parseLong(input);

                if (min != null && value < min) {
                    System.err.println("значение должно быть не меньше " + min);
                    continue;
                }

                if (max != null && value > max) {
                    System.err.println("значение должно быть не больше " + max);
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                System.err.println("введите корректное число");
            }
        }
    }

    /**
     * читает float с консоли с валидацией
     */
    private static float readFloat(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");

            if (!scanner.hasNextLine()) {
                throw new java.util.NoSuchElementException("EOF получен");
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.err.println("введите число");
                continue;
            }

            try {
                return Float.parseFloat(input);
            } catch (NumberFormatException e) {
                System.err.println("введите корректное число");
            }
        }
    }
}