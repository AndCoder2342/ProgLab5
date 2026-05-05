package server.commands;

import commands.Command;
import commands.CommandResult;
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

// Пример реализации команды с Stream API:
// commands/RemoveGreaterCommand.java (обновлённая)
package commands;

import shared.RequestContext;
import manager.Product;
import java.util.stream.Collectors;

public class RemoveGreaterCommand implements Command {
    private final Product threshold;

    public RemoveGreaterCommand(Product threshold) {
        this.threshold = threshold;
    }

    @Override
    public CommandResult execute(RequestContext context) {
        var collectionManager = context.getCollectionManager();
        var logger = context.getLogger();

        // Stream API + лямбда: фильтрация и удаление
        var toRemove = collectionManager.getAll().stream()
                .filter(Objects::nonNull)
                .filter(p -> p.compareTo(threshold) > 0) // Использует Comparable<Product>
                .map(Product::getId)
                .collect(Collectors.toList());

        toRemove.forEach(id -> {
            collectionManager.removeKey(id);
            logger.debug("Удалён продукт с id={}", id);
        });

        logger.info("Удалено {} элементов, превышающих заданный", toRemove.size());
        return CommandResult.ok(
                "Удалено элементов: " + toRemove.size(),
                toRemove.size()
        );
    }

    @Override
    public String getDescription() {
        return "удалить из коллекции все элементы, превышающие заданный";
    }

    @Override
    public String getName() {
        return "remove_greater";
    }
}