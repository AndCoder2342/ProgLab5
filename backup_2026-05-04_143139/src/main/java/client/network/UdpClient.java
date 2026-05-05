package client.network;

import shared.Request;
import shared.Response;
import org.tinylog.Logger;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UdpClient {
    private final DatagramChannel channel;
    private final Selector selector;
    private final InetSocketAddress serverAddress;
    private final UUID clientId;

    // Ожидание ответов по requestId
    private final Map<UUID, CompletableFuture<Response>> pendingResponses = new ConcurrentHashMap<>();

    private volatile boolean running = true;

    public UdpClient(String serverHost, int serverPort) throws IOException {
        this.serverAddress = new InetSocketAddress(serverHost, serverPort);
        this.clientId = UUID.randomUUID();

        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.channel.socket().bind(null); // Эфемерный порт

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        // Запуск фонового потока для чтения ответов
        startResponseReader();

        Logger.info("UDP-клиент запущен, сервер: {}", serverAddress);
    }

    private void startResponseReader() {
        Thread readerThread = new Thread(() -> {
            while (running) {
                try {
                    selector.select(500);
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (key.isReadable()) {
                            handleResponse();
                        }
                    }
                } catch (IOException e) {
                    Logger.error(e, "Ошибка чтения UDP-пакета");
                }
            }
        }, "UDP-Response-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void handleResponse() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(65535);
        InetSocketAddress sender = (InetSocketAddress) channel.receive(buffer);

        if (sender == null || !sender.equals(serverAddress)) return;

        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        Response response = SerializationUtil.deserialize(data, Response.class);
        if (response == null) {
            Logger.warn("Не удалось десериализовать ответ от сервера");
            return;
        }

        Logger.debug("Получен ответ: {} (успех: {})",
                response.getRequestId(), response.isSuccess());

        // Находим ожидающий запрос и завершаем CompletableFuture
        CompletableFuture<Response> future = pendingResponses.remove(response.getRequestId());
        if (future != null) {
            future.complete(response);
        } else {
            Logger.debug("Ответ для неизвестного запроса: {}", response.getRequestId());
        }
    }

    /**
     * Отправляет запрос и ожидает ответ с таймаутом
     */
    public Response sendRequest(Request request, long timeoutMs)
            throws IOException, TimeoutException, InterruptedException {

        byte[] data = SerializationUtil.serialize(request);
        channel.send(ByteBuffer.wrap(data), serverAddress);

        Logger.info("Отправлен запрос {} на сервер", request.getRequestId());

        // Создаём CompletableFuture для асинхронного ожидания
        CompletableFuture<Response> future = new CompletableFuture<>();
        pendingResponses.put(request.getRequestId(), future);

        // Ждём ответ с таймаутом
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new IOException("Ошибка получения ответа", e.getCause());
        } catch (TimeoutException e) {
            pendingResponses.remove(request.getRequestId());
            Logger.warn("Таймаут ожидания ответа для запроса {}", request.getRequestId());
            throw e;
        }
    }

    /**
     * Обработка временной недоступности сервера
     */
    public Response sendWithRetry(Request request, int maxRetries, long timeoutMs) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxRetries) {
            try {
                return sendRequest(request, timeoutMs);
            } catch (TimeoutException e) {
                lastException = e;
                attempts++;
                Logger.warn("Попытка {}/{} не удалась, таймаут", attempts, maxRetries);

                if (attempts < maxRetries) {
                    try {
                        Thread.sleep(1000 * attempts); // Экспоненциальная задержка
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (IOException e) {
                lastException = e;
                Logger.error(e, "Сетевая ошибка");
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        throw new RuntimeException("Не удалось получить ответ после " + maxRetries + " попыток", lastException);
    }

    public void close() {
        running = false;
        try {
            channel.close();
            selector.close();
        } catch (IOException e) {
            Logger.error(e, "Ошибка закрытия UDP-канала");
        }
    }

    public UUID getClientId() {
        return clientId;
    }
}