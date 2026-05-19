package commands;

import shared.RequestContext;
import shared.CommandResult;
import java.io.Serializable;

public class ExecuteScriptCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;
    private final String filename;

    public ExecuteScriptCommand(String filename) {
        this.filename = filename;
    }

    @Override
    public CommandResult execute(RequestContext context) {
        return CommandResult.error("execute_script выполняется только на клиенте");
    }

    @Override
    public String getName() {
        return "execute_script";
    }

    @Override
    public String getDescription() {
        return "выполнить скрипт из файла (на клиенте)";
    }

    public String getFilename() {
        return filename;
    }
}