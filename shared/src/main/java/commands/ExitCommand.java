package commands;

import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;

/**
 * Команда завершения программы
 * Выполняется на КЛИЕНТЕ, не отправляется на сервер
 */
public class ExitCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public CommandResult execute(RequestContext context) {
        // команда обрабатывается на клиенте
        return CommandResult.ok("Завершение программы...", null);
    }

    @Override
    public String getDescription() {
        return "завершить программу (без сохранения в файл)";
    }

    @Override
    public String getName() {
        return "exit";
    }
}