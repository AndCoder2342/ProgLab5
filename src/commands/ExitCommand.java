
package commands;

/**
 * команда завершения программы.
 */
public class ExitCommand implements Command {
    @Override
    public boolean execute() {
        System.out.println("Завершение программы...");
        return false; // Возвращаем false для выхода из цикла
    }

    @Override
    public String getDescription() {
        return "завершить программу (без сохранения в файл)";
    }

    @Override
    public String getName() {
        return "exit";
    }
}