package commands;

import commands.Command;
import java.io.Serializable;

public class WhoAmICommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() { return "whoami"; }

    @Override
    public String getDescription() { return "показать текущего пользователя"; }
}