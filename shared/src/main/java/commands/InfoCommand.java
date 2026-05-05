package commands;

import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;

/**
 * Команда вывода информации о коллекции
 */
public class InfoCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public CommandResult execute(RequestContext context) {
        try {
            // получаем объект менеджера
            Object cmObject = context.getCollectionManager();

            // вызываем метод getInfo() через рефлексию
            String info = (String) cmObject.getClass().getMethod("getInfo").invoke(cmObject);

            return CommandResult.ok("Информация о коллекции:", info);

        } catch (Exception e) {
            return CommandResult.error("Ошибка: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "вывести в стандартный поток вывода информацию о коллекции";
    }

    @Override
    public String getName() {
        return "info";
    }
}