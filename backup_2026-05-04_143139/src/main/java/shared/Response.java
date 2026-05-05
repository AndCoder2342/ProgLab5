package shared;

import java.io.Serializable;
import java.util.UUID;

public class Response implements Serializable {
    private final UUID requestId;      // Соответствует запросу
    private final boolean success;
    private final String message;
    private final Object data;         // Результат (коллекция, число, строка)
    private final long serverTime;
    private final AckType ackType;     // Для механизма надёжности

    public enum AckType { ACK, NACK, DUPLICATE, TIMEOUT }

    public Response(UUID requestId, boolean success, String message, Object data, AckType ackType) {
        this.requestId = requestId;
        this.success = success;
        this.message = message;
        this.data = data;
        this.serverTime = System.currentTimeMillis();
        this.ackType = ackType;
    }

    // Геттеры + фабричные методы
    public static Response ok(UUID requestId, String message, Object data) {
        return new Response(requestId, true, message, data, AckType.ACK);
    }

    public static Response error(UUID requestId, String message) {
        return new Response(requestId, false, message, null, AckType.NACK);
    }

    public static Response duplicate(UUID requestId) {
        return new Response(requestId, false, "Duplicate request", null, AckType.DUPLICATE);
    }

    public UUID getRequestId() { return requestId; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
    public AckType getAckType() { return ackType; }
}