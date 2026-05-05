package commands;

import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;

/**
 * Интерфейс команды для клиент-серверного взаимодействия
 */
public interface Command extends Serializable {

    /**
     * Выполняет команду на сервере
     * @param context контекст выполнения (доступ к коллекции, логгеру)
     * @return результат выполнения
     */
    CommandResult execute(RequestContext context);

    /**
     * Возвращает описание команды
     */
    String getDescription();

    /**
     * Возвращает имя команды
     */
    String getName();
}