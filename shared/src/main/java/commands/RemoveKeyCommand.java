package commands;

import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;
import java.util.Hashtable;
import manager.Product;

/**
 * Команда удаления элемента по ключу
 */
public class RemoveKeyCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    private final Long key;

    public RemoveKeyCommand(Long key) {
        this.key = key;
    }

    @Override
    public CommandResult execute(RequestContext context) {
        try {
            // получаем collectionManager через рефлексию
            Object cmObject = context.getCollectionManager();

            // получаем коллекцию через рефлексию
            @SuppressWarnings("unchecked")
            Hashtable<Long, Product> collection = (Hashtable<Long, Product>)
                    cmObject.getClass().getMethod("getCollection").invoke(cmObject);

            // проверяем существование ключа
            if (!collection.containsKey(key)) {
                return CommandResult.error("Элемент с ключом " + key + " не найден");
            }

            // удаляем элемент
            collection.remove(key);

            return CommandResult.ok(
                    "Элемент с ключом " + key + " успешно удален",
                    key
            );

        } catch (Exception e) {
            return CommandResult.error("Ошибка при удалении: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "удалить элемент из коллекции по его ключу";
    }

    @Override
    public String getName() {
        return "remove_key";
    }

    public Long getKey() {
        return key;
    }
}