package commands;

import commands.Command;
import java.io.Serializable;

/**
 * Команда фильтрации по подстроке во всех полях
 */
public class FilterContainsNameCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    private final String substring;

    public FilterContainsNameCommand(String substring) {
        this.substring = substring;
    }

    @Override
    public String getDescription() {
        return "вывести элементы, значение любого поля которых содержит заданную подстроку";
    }

    @Override
    public String getName() {
        return "filter_contains_name";
    }

    public String getSubstring() {
        return substring;
    }
}