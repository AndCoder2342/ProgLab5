package commands;

import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;

/**
 * Команда помощи.
 * Возвращает список доступных команд.
 */
public class HelpCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public CommandResult execute(RequestContext context) {
        String helpText = "Доступные команды:\n" +
                "  info : вывести в стандартный поток вывода информацию о коллекции\n" +
                "  show : вывести в стандартный поток вывода все элементы коллекции\n" +
                "  clear : очистить коллекцию\n" +
                "  save : сохранить коллекцию в файл\n" +
                "  exit : завершить программу (без сохранения в файл)\n" +
                "  help : вывести справку по доступным командам\n" +
                "  insert {element} : добавить новый элемент с заданным ключом\n" +
                "  remove_key key : удалить элемент из коллекции по его ключу\n" +
                "  update id {element} : обновить значение элемента коллекции, id которого равен заданному\n" +
                "  remove_greater {element} : удалить из коллекции все элементы, превышающие заданный\n" +
                "  remove_lower {element} : удалить из коллекции все элементы, меньшие, чем заданный\n" +
                "  filter_contains_name name : вывести элементы, значение любого поля которых содержит заданную подстроку\n" +
                "  count_greater_than_manufacturer {element} : вывести количество элементов, значение поля manufacturer которых больше заданного\n" +
                "  group_counting_by_manufacturer : сгруппировать элементы коллекции по значению поля manufacturer\n" +
                "  execute_script file_name : считать и исполнить скрипт из указанного файла";

        return CommandResult.ok(helpText, null);
    }

    @Override
    public String getDescription() {
        return "вывести справку по доступным командам";
    }

    @Override
    public String getName() {
        return "help";
    }
}