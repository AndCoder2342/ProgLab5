package commands;

import java.io.Serializable;

/**
 * интерфейс для всех команд
 */
public interface Command extends Serializable {

    /**
     * @return имя команды для парсинга
     */
    String getName();

    /**
     * @return описание команды для help
     */
    default String getDescription() {
        return "Нет описания";
    }

}