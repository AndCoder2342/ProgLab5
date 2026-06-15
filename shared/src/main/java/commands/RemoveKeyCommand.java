package commands;

import java.io.Serializable;

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
    public Long getId() {
        return key;
    }
}