package commands;

import java.io.Serializable;

/**
 * Команда сохранения коллекции в файл.
 */
public class SaveCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "сохранить коллекцию в файл (доступно только серверу)";
    }

    @Override
    public String getName() {
        return "save";
    }
}