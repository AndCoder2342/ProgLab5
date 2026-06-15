package shared;

import commands.Command;
import java.io.Serializable;
import java.util.UUID;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID requestId;
    private final String username;
    private final String password;
    private final Command command;


    public Request(String username, String password, Command command) {
        this.requestId = UUID.randomUUID();
        this.username = username;
        this.password = password;
        this.command = command;
    }


    public Request(UUID clientId, Command command) {
        this(null, null, command);
    }

    public UUID getRequestId() { return requestId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Command getCommand() { return command; }
}