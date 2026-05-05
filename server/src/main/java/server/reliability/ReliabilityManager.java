package server.reliability;

import java.net.InetSocketAddress;
import shared.Request;
import shared.Response;
import org.tinylog.Logger;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ReliabilityManager {
    private static final long REQUEST_TIMEOUT_MS = 5000;
    private static final long DUPLICATE_WINDOW_MS = 30000;

    // Кэш обработанных запросов для защиты от повторов
    private final Map<UUID, Long> processedRequests = new ConcurrentHashMap<>();

    // Очередь неподтверждённых ответов (для повторной отправки при потере)
    private final Map<UUID, PendingResponse> pendingResponses = new ConcurrentHashMap<>();

    private static class PendingResponse {
        final Response response;
        final InetSocketAddress clientAddr;
        long sentTime;
        int retryCount;

        PendingResponse(Response r, InetSocketAddress addr) {
            this.response = r;
            this.clientAddr = addr;
            this.sentTime = System.currentTimeMillis();
            this.retryCount = 0;
        }
    }

    /**
     * Проверяет, не является ли запрос дубликатом
     */
    public boolean isDuplicate(Request request) {
        UUID reqId = request.getRequestId();
        long now = System.currentTimeMillis();

        // Очищаем старые записи
        processedRequests.entrySet().removeIf(e ->
                now - e.getValue() > DUPLICATE_WINDOW_MS);

        if (processedRequests.containsKey(reqId)) {
            Logger.debug("Обнаружен дубликат запроса: {}", reqId);
            return true;
        }

        // Регистрируем новый запрос
        processedRequests.put(reqId, now);
        return false;
    }

    /**
     * Регистрирует ответ для возможной повторной отправки
     */
    public void registerForRetry(Response response, InetSocketAddress clientAddr) {
        pendingResponses.put(response.getRequestId(),
                new PendingResponse(response, clientAddr));
    }

    /**
     * Обрабатывает подтверждение от клиента
     */
    public void handleAck(UUID requestId) {
        pendingResponses.remove(requestId);
        Logger.debug("Получено подтверждение для {}", requestId);
    }

    /**
     * Возвращает ответы, требующие повторной отправки (таймаут)
     */
    public Map<UUID, PendingResponse> getTimedOutResponses() {
        long now = System.currentTimeMillis();
        Map<UUID, PendingResponse> timedOut = new ConcurrentHashMap<>();

        for (Map.Entry<UUID, PendingResponse> entry : pendingResponses.entrySet()) {
            PendingResponse pr = entry.getValue();
            // Повторная отправка если прошло > таймаута и < макс. попыток
            if (now - pr.sentTime > REQUEST_TIMEOUT_MS && pr.retryCount < 3) {
                pr.retryCount++;
                pr.sentTime = now;
                timedOut.put(entry.getKey(), pr);
                Logger.info("Повторная отправка ответа {} (попытка {}/3)",
                        entry.getKey(), pr.retryCount);
            }
        }
        return timedOut;
    }

    /**
     * Периодическая очистка (вызывать из главного цикла сервера)
     */
    public void maintenance() {
        // Отправка таймаутных ответов
        getTimedOutResponses().forEach((id, pr) -> {
            // Логика повторной отправки через responseSender
        });

        // Очистка очень старых неподтверждённых
        long now = System.currentTimeMillis();
        pendingResponses.entrySet().removeIf(e ->
                now - e.getValue().sentTime > DUPLICATE_WINDOW_MS);
    }
}