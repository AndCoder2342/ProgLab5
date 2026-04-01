package manager;

import enums.UnitOfMeasure;

import java.util.Scanner;

/**
 * вспомогательный класс для ввода данных с консоли
 */
public class InputHelper {
    private static Scanner scanner = new Scanner(System.in);

    /**
     * пересоздает сканер (нужно после EOF)
     */
    public static void resetScanner() {
        scanner = new Scanner(System.in);
    }

    /**
     * читает продукт с консоли
     */
    public static Product readProductFromConsole() {
        Product product = new Product();

        try {

            product.setName(readString("Введите название", false));


            System.out.println("Ввод координат:");
            product.setCoordinates(readCoordinates());


            product.setPrice(readInt("Введите цену (больше 0)", 1, Integer.MAX_VALUE));


            System.out.println("Доступные единицы измерения:");
            for (UnitOfMeasure unit : UnitOfMeasure.values()) {
                System.out.println("  " + unit.name());
            }

            int maxAttempts = 2;
            int attempt = 0;
            boolean unitSet = false;

            while (attempt < maxAttempts && !unitSet) {
                System.out.print("Введите единицу измерения (или пустую строку для null): ");
                String unitStr = scanner.nextLine().trim();

                if (unitStr.isEmpty()) {
                    // пустая строка  это ок,тогда null
                    break;
                }

                try {
                    product.setUnitOfMeasure(UnitOfMeasure.valueOf(unitStr.toUpperCase()));
                    unitSet = true;
                } catch (IllegalArgumentException e) {
                    attempt++;
                    if (attempt >= maxAttempts) {
                        System.err.println("Ошибка: превышено количество попыток ввода");
                        throw new RuntimeException("Превышено количество попыток ввода единицы измерения");
                    }
                    System.err.println("Ошибка: неверная единица измерения. Доступные значения:");
                    for (UnitOfMeasure unit : UnitOfMeasure.values()) {
                        System.out.println("  " + unit.name());
                    }
                    System.err.println("(попытка " + (attempt + 1) + " из " + maxAttempts + ")");
                }
            }


            String orgChoice = readString("Ввести производителя? (да/нет)", false);
            if (orgChoice.equalsIgnoreCase("да") || orgChoice.equalsIgnoreCase("yes") || orgChoice.equalsIgnoreCase("y")) {
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
        int maxAttempts = 2;
        int attempt = 0;

        while (attempt < maxAttempts) {
            System.out.print(prompt + ": ");

            if (!scanner.hasNextLine()) {
                throw new java.util.NoSuchElementException("EOF получен");
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                attempt++;
                if (attempt >= maxAttempts) {
                    System.err.println("Ошибка: превышено количество попыток ввода");
                    throw new RuntimeException("Превышено количество попыток ввода числа");
                }
                System.err.println("Ошибка: введите число (попытка " + (attempt + 1) + " из " + maxAttempts + ")");
                continue;
            }

            try {
                int value = Integer.parseInt(input);

                if (min != null && value < min) {
                    attempt++;
                    if (attempt >= maxAttempts) {
                        System.err.println("Ошибка: превышено количество попыток ввода");
                        throw new RuntimeException("Превышено количество попыток ввода числа");
                    }
                    System.err.println("Ошибка: значение должно быть не меньше " + min +
                            " (попытка " + (attempt + 1) + " из " + maxAttempts + ")");
                    continue;
                }

                if (max != null && value > max) {
                    attempt++;
                    if (attempt >= maxAttempts) {
                        System.err.println("Ошибка: превышено количество попыток ввода");
                        throw new RuntimeException("Превышено количество попыток ввода числа");
                    }
                    System.err.println("Ошибка: значение должно быть не больше " + max +
                            " (попытка " + (attempt + 1) + " из " + maxAttempts + ")");
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    System.err.println("Ошибка: превышено количество попыток ввода");
                    throw new RuntimeException("Превышено количество попыток ввода числа");
                }
                System.err.println("Ошибка: введите корректное целое число (попытка " +
                        (attempt + 1) + " из " + maxAttempts + ")");
            }
        }

        throw new RuntimeException("Превышено количество попыток ввода");
    }

    /**
     * читает long с консоли с валидацией
     */
    private static long readLong(String prompt, Long min, Long max) {
        int maxAttempts = 2;
        int attempt = 0;

        while (attempt < maxAttempts) {
            System.out.print(prompt + ": ");

            if (!scanner.hasNextLine()) {
                throw new java.util.NoSuchElementException("EOF получен");
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                attempt++;
                if (attempt >= maxAttempts) {
                    System.err.println("Ошибка: превышено количество попыток ввода");
                    throw new RuntimeException("Превышено количество попыток ввода числа");
                }
                System.err.println("Ошибка: введите число (попытка " + (attempt + 1) + " из " + maxAttempts + ")");
                continue;
            }

            try {
                long value = Long.parseLong(input);

                if (min != null && value < min) {
                    attempt++;
                    if (attempt >= maxAttempts) {
                        System.err.println("Ошибка: превышено количество попыток ввода");
                        throw new RuntimeException("Превышено количество попыток ввода числа");
                    }
                    System.err.println("Ошибка: значение должно быть не меньше " + min +
                            " (попытка " + (attempt + 1) + " из " + maxAttempts + ")");
                    continue;
                }

                if (max != null && value > max) {
                    attempt++;
                    if (attempt >= maxAttempts) {
                        System.err.println("Ошибка: превышено количество попыток ввода");
                        throw new RuntimeException("Превышено количество попыток ввода числа");
                    }
                    System.err.println("Ошибка: значение должно быть не больше " + max +
                            " (попытка " + (attempt + 1) + " из " + maxAttempts + ")");
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    System.err.println("Ошибка: превышено количество попыток ввода");
                    throw new RuntimeException("Превышено количество попыток ввода числа");
                }
                System.err.println("Ошибка: введите корректное число (попытка " +
                        (attempt + 1) + " из " + maxAttempts + ")");
            }
        }

        throw new RuntimeException("Превышено количество попыток ввода");
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