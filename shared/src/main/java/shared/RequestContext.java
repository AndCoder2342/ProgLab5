package shared;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * Универсальный контекст запроса
 */
public class RequestContext {
    private final UUID requestId;
    private final UUID clientId;
    private final InetSocketAddress clientAddress;
    private final Object collectionManager;

    public RequestContext(UUID requestId, UUID clientId,
                          InetSocketAddress clientAddress, Object collectionManager) {
        this.requestId = requestId;
        this.clientId = clientId;
        this.clientAddress = clientAddress;
        this.collectionManager = collectionManager;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public InetSocketAddress getClientAddress() {
        return clientAddress;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCollectionManager() {
        return (T) collectionManager;
    }
}