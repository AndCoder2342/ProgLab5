package shared;

import commands.Command;
import java.io.Serializable;
import java.util.UUID;

public class Request implements Serializable {
    private final UUID requestId;      // Уникальный ID для трассировки
    private final UUID clientId;       // ID клиента для сессии
    private final Command command;     // Команда + аргументы как объект
    private final long timestamp;      // Для таймаутов и replay-защиты
    private int retryCount;            // Для механизма повторных отправок

    public Request(UUID clientId, Command command) {
        this.requestId = UUID.randomUUID();
        this.clientId = clientId;
        this.command = command;
        this.timestamp = System.currentTimeMillis();
        this.retryCount = 0;
    }

    // Геттеры
    public UUID getRequestId() { return requestId; }
    public UUID getClientId() { return clientId; }
    public Command getCommand() { return command; }
    public long getTimestamp() { return timestamp; }
    public int getRetryCount() { return retryCount; }
    public void incrementRetry() { this.retryCount++; }

    @Override
    public String toString() {
        return "Request{" + requestId + ", cmd=" + command.getName() + "}";
    }
}