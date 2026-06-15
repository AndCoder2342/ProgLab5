package commands;

import commands.Command;
import java.io.Serializable;

/**
 * Команда регистрации
 */
public class RegisterCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;

    public RegisterCommand(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    @Override
    public String getName() { return "register"; }

    @Override
    public String getDescription() {
        return "зарегистрировать нового пользователя";
    }
}