package server.commands;

import commands.Command;
import shared.CommandResult;
import shared.RequestContext;
import manager.Product;
import org.tinylog.Logger;
import java.util.*;
import java.util.stream.Collectors;

public class CommandExecutor {

    public CommandResult execute(Command command, RequestContext context) {
        Logger.debug("Выполнение команды {} (запрос {})",
                command.getName(), context.getRequestId());

        try {
            CommandResult result = command.execute(context);
            Logger.info("Команда {} выполнена: {}",
                    command.getName(), result.isSuccess() ? "успех" : "ошибка");
            return result;
        } catch (Exception e) {
            Logger.error(e, "Исключение при выполнении команды {}", command.getName());
            return CommandResult.error("Внутренняя ошибка сервера: " + e.getMessage());
        }
    }
}