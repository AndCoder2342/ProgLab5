package commands;

import commands.Command;
import java.io.Serializable;

/**
 * Команда демонстрации всех элементов коллекции
 */
public class ShowCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "вывести в стандартный поток вывода все элементы коллекции";
    }

    @Override
    public String getName() {
        return "show";
    }
}