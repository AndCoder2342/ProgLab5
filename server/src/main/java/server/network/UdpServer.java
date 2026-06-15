package server.network;

import manager.CollectionManager;
import org.tinylog.Logger;
import shared.Request;
import shared.Response;
import shared.SerializationUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

/**
 * UDP сервер использующий многопоточность и ForkJoinPool
 */
public class UdpServer {

    private final DatagramChannel channel;
    private final Selector selector;
    private final CollectionManager collectionManager;

    // пулы
    private final ForkJoinPool forkJoinPool;           // для команд
    private final ExecutorService responseExecutor;    // для ответов

    private volatile boolean running = true;

    public UdpServer(InetSocketAddress address, CollectionManager collectionManager) throws IOException {
        this.collectionManager = collectionManager;

        // канал в неблокирующем режиме
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.channel.socket().bind(address);

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        // инициализация пулов
        int cores = Runtime.getRuntime().availableProcessors();
        this.forkJoinPool = new ForkJoinPool(cores);
        this.responseExecutor = Executors.newFixedThreadPool(cores);

        Logger.info("UDP Socket привязан к адресу: {}", address);
        Logger.info("ForkJoinPool: {} потоков", cores);
        Logger.info("Response Executor: {} потоков", cores);
    }


    public void start() {
        Logger.info("Вход в главный цикл обработки запросов...");

        while (running) {
            try {

                int readyChannels = selector.select(1000);
                if (readyChannels == 0) continue;

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isReadable()) {
                        handleRead(key);
                    }

                    keyIterator.remove();
                }
            } catch (IOException e) {
                Logger.error(e, "Ошибка в цикле селектора");
            }
        }

        Logger.info("Сервер остановлен.");
    }

    /**
     * обработка входящего запроса
     * отправляем в ForkJoinPool для параллельной обработки
     */
    private void handleRead(SelectionKey key) {
        try {
            DatagramChannel ch = (DatagramChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(65535);

            InetSocketAddress clientAddr = (InetSocketAddress) ch.receive(buffer);
            if (clientAddr == null) return;

            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            Logger.debug("Получен пакет от {} (размер: {} байт)", clientAddr, data.length);


            final DatagramChannel finalChannel = ch;
            final InetSocketAddress finalClientAddr = clientAddr;
            final byte[] finalData = data;


            forkJoinPool.submit(() -> {
                try {
                    processRequest(finalChannel, finalClientAddr, finalData);
                } catch (Exception e) {
                    Logger.error(e, "Ошибка обработки запроса в ForkJoinPool от {}", finalClientAddr);
                }
            });

        } catch (IOException e) {
            Logger.error(e, "Ошибка чтения пакета");
        }
    }

    /**
     * Обработка запроса в отдельном потоке ForkJoinPool
     */
    private void processRequest(DatagramChannel ch, InetSocketAddress clientAddr, byte[] data) {
        try {

            Request request = SerializationUtil.deserialize(data, Request.class);
            if (request == null) {
                sendError(ch, clientAddr, "Ошибка десериализации запроса");
                return;
            }

            Logger.info("Обработка команды '{}' от {}", request.getCommand().getName(), clientAddr);


            server.commands.CommandExecutor executor = new server.commands.CommandExecutor();


            shared.RequestContext context = new shared.RequestContext(
                    request.getRequestId(),
                    request.getUsername() != null ? request.getUsername() : "anonymous",
                    clientAddr
            );


            shared.CommandResult result = executor.execute(request, context, collectionManager);


            Response response;
            if (result.isSuccess()) {
                response = Response.ok(request.getRequestId(), result.getMessage(), result.getData());
            } else {
                response = Response.error(request.getRequestId(), result.getMessage());
            }



            responseExecutor.submit(() -> {
                try {
                    sendResponse(ch, clientAddr, response);
                } catch (IOException e) {
                    Logger.error(e, "Ошибка отправки ответа");
                }
            });

        } catch (ClassNotFoundException e) {
            Logger.error(e, "Класс не найден при десериализации");
            sendError(ch, clientAddr, "Ошибка десериализации");
        } catch (Exception e) {
            Logger.error(e, "Непредвиденная ошибка при обработке запроса");
            sendError(ch, clientAddr, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    /**
     * Отправка ответа клиенту
     */
    private void sendResponse(DatagramChannel ch, InetSocketAddress clientAddr, Response response) throws IOException {
        byte[] responseData = SerializationUtil.serialize(response);
        ByteBuffer responseBuffer = ByteBuffer.wrap(responseData);
        ch.send(responseBuffer, clientAddr);
        Logger.debug("Ответ отправлен клиенту {}", clientAddr);
    }

    /**
     * Отправка сообщения об ошибке
     */
    private void sendError(DatagramChannel ch, InetSocketAddress addr, String msg) {
        try {
            Response error = Response.error(java.util.UUID.randomUUID(), msg);
            sendResponse(ch, addr, error);
        } catch (IOException e) {
            Logger.error(e, "Не удалось отправить сообщение об ошибке");
        }
    }

    /**
     * Корректная остановка сервера
     */
    public void stop() {
        Logger.info("Остановка сервера...");
        running = false;

        // останока пулов
        forkJoinPool.shutdown();
        responseExecutor.shutdown();

        try {
            if (!forkJoinPool.awaitTermination(5, TimeUnit.SECONDS)) {
                forkJoinPool.shutdownNow();
            }
            if (!responseExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                responseExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            forkJoinPool.shutdownNow();
            responseExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }


        try {
            channel.close();
            selector.close();
            Logger.info("Сервер остановлен корректно");
        } catch (IOException e) {
            Logger.error(e, "Ошибка при закрытии ресурсов сервера");
        }
    }
}