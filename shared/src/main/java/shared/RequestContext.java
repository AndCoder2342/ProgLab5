package shared;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * Контекст запроса: метаданные без бизнес-логики
 */
public class RequestContext {
    private final UUID requestId;
    private final String username;
    private final InetSocketAddress clientAddress;

    public RequestContext(UUID requestId, String username, InetSocketAddress clientAddress) {
        this.requestId = requestId;
        this.username = username;
        this.clientAddress = clientAddress;
    }

    public UUID getRequestId() { return requestId; }
    public String getUsername() { return username; }
    public InetSocketAddress getClientAddress() { return clientAddress; }
}