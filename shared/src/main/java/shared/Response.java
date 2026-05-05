package shared;

import java.io.Serializable;
import java.util.UUID;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID requestId;
    private final boolean success;
    private final String message;
    private final Object data;

    public Response(UUID requestId, boolean success, String message, Object data) {
        this.requestId = requestId;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Фабричные методы для удобства
    public static Response ok(UUID requestId, String message, Object data) {
        return new Response(requestId, true, message, data);
    }

    public static Response error(UUID requestId, String message) {
        return new Response(requestId, false, message, null);
    }

    public UUID getRequestId() { return requestId; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}