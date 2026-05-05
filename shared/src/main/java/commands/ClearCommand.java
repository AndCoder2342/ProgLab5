package commands;

import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;
import java.util.Hashtable;
import manager.Product;

/**
 * Команда очистки коллекции
 */
public class ClearCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public CommandResult execute(RequestContext context) {
        try {
            // получаем объект менеджера (как Object)
            Object cmObject = context.getCollectionManager();

            // через рефлексию получаем внутреннюю коллекцию (Hashtable)
            @SuppressWarnings("unchecked")
            Hashtable<Long, Product> collection = (Hashtable<Long, Product>)
                    cmObject.getClass().getMethod("getCollection").invoke(cmObject);

            // запоминаем размер до очистки
            int sizeBefore = collection.size();

            // очищаем коллекцию
            collection.clear();

            return CommandResult.ok("Коллекция очищена. Удалено элементов: " + sizeBefore, sizeBefore);

        } catch (Exception e) {
            return CommandResult.error("Ошибка при очистке коллекции: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "очистить коллекцию";
    }

    @Override
    public String getName() {
        return "clear";
    }
}