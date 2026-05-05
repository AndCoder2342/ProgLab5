package shared;

import java.io.Serializable;
import commands.Command;
import java.util.UUID;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID requestId;
    private final UUID clientId;
    private final Command command;

    public Request(UUID clientId, Command command) {
        this.requestId = UUID.randomUUID();
        this.clientId = clientId;
        this.command = command;
    }

    public UUID getRequestId() { return requestId; }
    public UUID getClientId() { return clientId; }
    public Command getCommand() { return command; }
}