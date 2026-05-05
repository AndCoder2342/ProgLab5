package commands;

import shared.RequestContext; // Контекст выполнения (для сервера)
import java.io.Serializable;

public interface Command extends Serializable {
    /**
     * Выполняет команду на сервере
     * @param context контекст с доступом к коллекции, логгеру и т.д.
     * @return результат выполнения
     */
    CommandResult execute(RequestContext context);

    String getDescription();
    String getName();

    // По умолчанию команда не требует аргументов
    default boolean requiresArguments() { return false; }
}