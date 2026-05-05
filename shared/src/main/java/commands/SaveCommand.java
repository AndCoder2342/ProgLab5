package commands;

import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;

/**
 * Команда сохранения коллекции в файл.
 * !!! ВАЖНО: Согласно требованиям лабораторной, клиент НЕ должен отправлять эту команду.
 * Она предназначена только для внутреннего использования на сервере.
 */
public class SaveCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public CommandResult execute(RequestContext context) {
        try {
            // получаем collectionManager через рефлексию
            Object cmObject = context.getCollectionManager();

            // вызываем метод save() через рефлексию
            cmObject.getClass().getMethod("save").invoke(cmObject);

            return CommandResult.ok("Коллекция сохранена в файл", null);

        } catch (Exception e) {
            return CommandResult.error("Ошибка сохранения: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "сохранить коллекцию в файл (доступно только серверу)";
    }

    @Override
    public String getName() {
        return "save";
    }
}