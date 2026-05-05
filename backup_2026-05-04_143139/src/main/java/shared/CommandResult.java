package commands;

import java.io.Serializable;

public class CommandResult implements Serializable {
    private final boolean success;
    private final String message;
    private final Object data; // Коллекция, число, строка — что вернул сервер

    public CommandResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static CommandResult ok(String message, Object data) {
        return new CommandResult(true, message, data);
    }

    public static CommandResult error(String message) {
        return new CommandResult(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}