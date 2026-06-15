package server.commands;

import commands.*;
import manager.CollectionManager;
import manager.User;
import manager.UserManager;
import shared.CommandResult;
import shared.Request;
import shared.RequestContext;
import org.tinylog.Logger;

import java.util.Optional;

public class CommandExecutor {

    /**
     * Выполняет команду с проверкой авторизации
     */
    public CommandResult execute(Request request, RequestContext context, CollectionManager collectionManager) {
        Command command = request.getCommand();


        if (command instanceof RegisterCommand) {
            return executeRegister((RegisterCommand) command, collectionManager);
        }


        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || password == null || username.isEmpty()) {
            return CommandResult.error("Требуется авторизация. Войдите в систему.");
        }


        UserManager userManager = collectionManager.getUserManager();


        Optional<User> userOpt = userManager.login(username, password);
        if (userOpt.isEmpty()) {
            return CommandResult.error("Неверный логин или пароль");
        }

        int userId = userOpt.get().getId();
        Logger.info(" Авторизация: username={}, userId={}", username, userId);


        try {
            if (command instanceof HelpCommand) {
                return executeHelp((HelpCommand) command);
            } else if (command instanceof WhoAmICommand) {
                return CommandResult.ok("Вы: " + username + " (ID: " + userId + ")", userId);
            } else if (command instanceof InfoCommand) {
                return executeInfo((InfoCommand) command, collectionManager);
            } else if (command instanceof ShowCommand) {
                return executeShow((ShowCommand) command, collectionManager);
            } else if (command instanceof InsertCommand) {
                return executeInsert((InsertCommand) command, collectionManager, userId);
            } else if (command instanceof UpdateCommand) {
                return executeUpdate((UpdateCommand) command, collectionManager, userId);
            } else if (command instanceof RemoveKeyCommand) {
                return executeRemoveKey((RemoveKeyCommand) command, collectionManager, userId);
            } else if (command instanceof ClearCommand) {
                return executeClear((ClearCommand) command, collectionManager, userId);
            } else if (command instanceof SaveCommand) {
                return executeSave((SaveCommand) command, collectionManager);
            } else if (command instanceof RemoveGreaterCommand) {
                return executeRemoveGreater((RemoveGreaterCommand) command, collectionManager, userId);
            } else if (command instanceof RemoveLowerCommand) {
                return executeRemoveLower((RemoveLowerCommand) command, collectionManager, userId);
            } else if (command instanceof ReplaceIfGreaterCommand) {
                return executeReplaceIfGreater((ReplaceIfGreaterCommand) command, collectionManager, userId);
            } else if (command instanceof FilterContainsNameCommand) {
                return executeFilterContainsName((FilterContainsNameCommand) command, collectionManager);
            } else if (command instanceof CountGreaterThanManufacturerCommand) {
                return executeCountGreaterThanManufacturer((CountGreaterThanManufacturerCommand) command, collectionManager);
            } else if (command instanceof GroupCountingByManufacturerCommand) {
                return executeGroupCountingByManufacturer((GroupCountingByManufacturerCommand) command, collectionManager);
            } else {
                return CommandResult.error("Неизвестная команда: " + command.getName());
            }
        } catch (Exception e) {
            return CommandResult.error("Ошибка выполнения: " + e.getMessage());
        }
    }


    private CommandResult executeHelp(HelpCommand cmd) {
        String helpText = """
            Доступные команды:
            help : вывести справку по доступным командам
            info : вывести информацию о коллекции
            show : вывести все элементы коллекции
            insert {element} : добавить новый элемент
            update id {element} : обновить элемент по id
            remove_key id : удалить элемент по ключу
            clear : очистить коллекцию (только свои элементы)
            save : сохранить коллекцию в файл
            remove_greater {element} : удалить элементы больше заданного
            remove_lower {element} : удалить элементы меньше заданного
            replace_if_greater {element} : заменить если новый больше
            filter_contains_name name : фильтрация по подстроке
            count_greater_than_manufacturer : подсчёт по производителю
            group_counting_by_manufacturer : группировка по производителю
            """;
        return CommandResult.ok(helpText, null);
    }

    private CommandResult executeInfo(InfoCommand cmd, CollectionManager cm) {
        String info = cm.getInfo();
        return CommandResult.ok("Информация о коллекции:", info);
    }

    private CommandResult executeShow(ShowCommand cmd, CollectionManager cm) {
        var products = cm.getAll();
        return CommandResult.ok("Всего элементов: " + products.size(), products);
    }

    private CommandResult executeFilterContainsName(FilterContainsNameCommand cmd, CollectionManager cm) {
        var products = cm.filterContainsName(cmd.getSubstring());
        return CommandResult.ok("Найдено: " + products.size(), products);
    }

    private CommandResult executeCountGreaterThanManufacturer(CountGreaterThanManufacturerCommand cmd, CollectionManager cm) {
        long count = cm.countGreaterThanManufacturer(cmd.getOrganization());
        return CommandResult.ok("Количество: " + count, count);
    }

    private CommandResult executeGroupCountingByManufacturer(GroupCountingByManufacturerCommand cmd, CollectionManager cm) {
        var result = cm.groupCountingByManufacturer();
        return CommandResult.ok("Группировка по производителю:", result);
    }

    private CommandResult executeSave(SaveCommand cmd, CollectionManager cm) {
        return CommandResult.ok("Коллекция сохранена", null);
    }


    private CommandResult executeInsert(InsertCommand cmd, CollectionManager cm, int userId) {
        Logger.info("INSERT: userId={}, product={}", userId, cmd.getProduct().getName());

        boolean success = cm.insert(cmd.getProduct(), userId);
        if (success) {
            return CommandResult.ok("Продукт добавлен", null);
        } else {
            return CommandResult.error("Не удалось добавить продукт");
        }
    }

    private CommandResult executeUpdate(UpdateCommand cmd, CollectionManager cm, int userId) {
        Logger.info("UPDATE: userId={}, productId={}", userId, cmd.getId());

        boolean success = cm.update(cmd.getId(), cmd.getProduct(), userId);
        if (success) {
            return CommandResult.ok("Продукт обновлён", null);
        } else {
            return CommandResult.error("Не удалось обновить продукт или нет прав");
        }
    }

    private CommandResult executeRemoveKey(RemoveKeyCommand cmd, CollectionManager cm, int userId) {
        Logger.info("REMOVE_KEY: userId={}, productId={}", userId, cmd.getId());

        boolean success = cm.removeKey(cmd.getId(), userId);
        if (success) {
            return CommandResult.ok("Продукт удалён", null);
        } else {
            return CommandResult.error("Продукт не найден или нет прав");
        }
    }

    private CommandResult executeClear(ClearCommand cmd, CollectionManager cm, int userId) {
        int count = cm.clear(userId);
        return CommandResult.ok("Удалено элементов: " + count, null);
    }

    private CommandResult executeRemoveGreater(RemoveGreaterCommand cmd, CollectionManager cm, int userId) {
        int count = cm.removeGreater(cmd.getProduct(), userId);
        return CommandResult.ok("Удалено элементов: " + count, null);
    }

    private CommandResult executeRemoveLower(RemoveLowerCommand cmd, CollectionManager cm, int userId) {
        int count = cm.removeLower(cmd.getProduct(), userId);
        return CommandResult.ok("Удалено элементов: " + count, null);
    }

    private CommandResult executeReplaceIfGreater(ReplaceIfGreaterCommand cmd, CollectionManager cm, int userId) {
        boolean success = cm.replaceIfGreater(cmd.getId(), cmd.getProduct(), userId);
        if (success) {
            return CommandResult.ok("Продукт заменён", null);
        } else {
            return CommandResult.error("Новый продукт не больше старого или нет прав");
        }
    }

    /**
     * Обработка команды регистрации
     */
    private CommandResult executeRegister(RegisterCommand cmd, CollectionManager collectionManager) {
        UserManager userManager = collectionManager.getUserManager();

        int result = userManager.register(cmd.getUsername(), cmd.getPassword());

        if (result > 0) {
            return CommandResult.ok(
                    "Пользователь зарегистрирован! Ваш ID: " + result + ". Теперь войдите в систему.",
                    result
            );
        } else if (result == -2) {
            return CommandResult.error("Пользователь с таким именем уже существует");
        } else if (result == -3) {
            return CommandResult.error("Логин должен быть >3 символов, пароль >4 символов");
        } else {
            return CommandResult.error("Ошибка сервера при регистрации");
        }
    }
}