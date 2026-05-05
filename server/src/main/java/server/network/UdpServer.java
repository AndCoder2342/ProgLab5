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

import server.commands.CommandExecutor;
import shared.RequestContext;
import shared.CommandResult;


/**
 * UDP Сервер на базе Java NIO (DatagramChannel).
 * Работает в однопоточном режиме, неблокирующе обрабатывает запросы.
 */
public class UdpServer {

    private final DatagramChannel channel;
    private final Selector selector;
    private final CollectionManager collectionManager;
    private volatile boolean running = true;

    public UdpServer(InetSocketAddress address, CollectionManager collectionManager) throws IOException {
        this.collectionManager = collectionManager;

        // открываем канал и переводим в неблокирующий режим
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.channel.socket().bind(address);

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        Logger.info("UDP Socket привязан к адресу: {}", address);
    }

    /**
     * главный цикл обработки событий (Event Loop)
     */
    public void start() {
        Logger.info("Вход в главный цикл обработки запросов...");

        while (running) {
            try {
                // таймаут 1 сек, чтобы можно было проверить running
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

    private void handleRead(SelectionKey key) {
        DatagramChannel ch = null;
        InetSocketAddress clientAddr = null;

        try {
            ch = (DatagramChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(65535);

            clientAddr = (InetSocketAddress) ch.receive(buffer);
            if (clientAddr == null) return;

            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            Logger.debug("Получен пакет от {} (размер: {} байт)", clientAddr, data.length);

            // десериализация запроса
            Request request = SerializationUtil.deserialize(data, Request.class);
            if (request == null) {
                sendError(ch, clientAddr, "Ошибка десериализации запроса");
                return;
            }

            Logger.info("Обработка команды '{}' от {}", request.getCommand().getName(), clientAddr);

            // обработка

            // создаём контекст запроса
            RequestContext context = new RequestContext(
                    request.getRequestId(),
                    request.getClientId(),
                    clientAddr,
                    collectionManager
            );

            // выполняем команду через CommandExecutor
            CommandExecutor executor = new CommandExecutor();
            CommandResult result = executor.execute(request.getCommand(), context);

            // формируем ответ
            Response response;
            if (result.isSuccess()) {
                response = Response.ok(request.getRequestId(), result.getMessage(), result.getData());
            } else {
                response = Response.error(request.getRequestId(), result.getMessage());
            }

            // отправляем ответ
            byte[] responseData = SerializationUtil.serialize(response);
            ByteBuffer responseBuffer = ByteBuffer.wrap(responseData);
            ch.send(responseBuffer, clientAddr);

            Logger.debug("Ответ отправлен клиенту {}", clientAddr);

        } catch (ClassNotFoundException e) {
            Logger.error(e, "Класс не найден при десериализации");
            if (ch != null && clientAddr != null) {
                sendError(ch, clientAddr, "Ошибка десериализации");
            }
        } catch (IOException e) {
            Logger.error(e, "Ошибка ввода-вывода при чтении пакета");
            if (ch != null && clientAddr != null) {
                sendError(ch, clientAddr, "Ошибка ввода-вывода");
            }
        } catch (Exception e) {
            Logger.error(e, "Непредвиденная ошибка при обработке запроса");
            if (ch != null && clientAddr != null) {
                sendError(ch, clientAddr, "Внутренняя ошибка сервера: " + e.getMessage());
            }
        }
    }

    private void sendError(DatagramChannel ch, InetSocketAddress addr, String msg) {
        try {
            Response error = Response.error(java.util.UUID.randomUUID(), msg);
            ch.send(ByteBuffer.wrap(SerializationUtil.serialize(error)), addr);
        } catch (IOException e) {
            Logger.error(e, "Не удалось отправить сообщение об ошибке");
        }
    }

    public void stop() {
        running = false;
        try {
            channel.close();
            selector.close();
        } catch (IOException e) {
            Logger.error(e, "Ошибка при закрытии ресурсов сервера");
        }
    }
}