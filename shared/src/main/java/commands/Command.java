package commands;

import java.io.Serializable;

/**
 * Базовый интерфейс для всех команд.
 * Команды - это просто DTO (данные), логика выполнения в CommandExecutor.
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

    // ✅ УБРАЛ метод execute() - логика теперь в CommandExecutor!
}