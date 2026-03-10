//package commands;
//
//public interface Command{
//    public void execute();
//}



package commands;

/**
 * интерфейс команды
 */
public interface Command {
    /**
     * выполняет команду
     * @return true если команда выполнена успешно
     */
    boolean execute();

    /**
     * возвращает описание команды
     */
    String getDescription();

    /**
     * возвращает имя команды
     */
    String getName();
}