package commands;

import commands.Command;
import java.io.Serializable;

/**
 * Команда очистки коллекции
 */
public class ClearCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "очистить коллекцию";
    }

    @Override
    public String getName() {
        return "clear";
    }
}