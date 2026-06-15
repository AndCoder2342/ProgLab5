package commands;


import java.io.Serializable;

/**
 * Команда вывода информации о коллекции
 */
public class InfoCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "вывести в стандартный поток вывода информацию о коллекции";
    }

    @Override
    public String getName() {
        return "info";
    }
}