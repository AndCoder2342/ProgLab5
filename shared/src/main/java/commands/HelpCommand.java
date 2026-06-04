package commands;

import commands.Command;
import java.io.Serializable;

/**
 * Команда помощи.
 * Возвращает список доступных команд.
 */
public class HelpCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "вывести справку по доступным командам";
    }

    @Override
    public String getName() {
        return "help";
    }
}