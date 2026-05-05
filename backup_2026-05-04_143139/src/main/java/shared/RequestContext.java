package shared;

import manager.CollectionManager;
import org.tinylog.Logger;
import java.net.InetSocketAddress;

public class RequestContext {
    private final CollectionManager collectionManager;
    private final Logger logger;
    private final InetSocketAddress clientAddress; // Для отправки ответа
    private final Request request; // Исходный запрос для трассировки

    public RequestContext(CollectionManager cm, Logger logger,
                          InetSocketAddress clientAddr, Request request) {
        this.collectionManager = cm;
        this.logger = logger;
        this.clientAddress = clientAddr;
        this.request = request;
    }

    public CollectionManager getCollectionManager() { return collectionManager; }
    public Logger getLogger() { return logger; }
    public InetSocketAddress getClientAddress() { return clientAddress; }
    public UUID getRequestId() { return request.getRequestId(); }
    public UUID getClientId() { return request.getClientId(); }
}