package server.network;

import shared.Request;
import shared.Response;
import manager.CollectionManager;
import org.tinylog.Logger;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class UdpServer {
    private final DatagramChannel channel;
    private final Selector selector;
    private final CollectionManager collectionManager;
    private final PacketHandler packetHandler;
    private final ResponseSender responseSender;

    private volatile boolean running = true;

    public UdpServer(InetSocketAddress address, CollectionManager cm) throws IOException {
        this.collectionManager = cm;

        // Неблокирующий режим
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.channel.socket().bind(address);

        this.selector = Selector.open();
        this.channel.register(selector, SelectionKey.OP_READ);

        this.packetHandler = new PacketHandler(cm);
        this.responseSender = new ResponseSender(channel);

        Logger.info("UDP-сервер запущен на {}", address);
    }

    public void start() {
        Logger.info("Начало цикла обработки запросов");

        while (running) {
            try {
                selector.select(1000); // Таймаут 1 сек для проверки running
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            } catch (IOException e) {
                Logger.error(e, "Ошибка в цикле обработки UDP");
            }
        }

        Logger.info("Цикл обработки завершён");
    }

    private void handleRead(SelectionKey key) {
        try {
            DatagramChannel ch = (DatagramChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(65535);
            InetSocketAddress clientAddr = (InetSocketAddress) ch.receive(buffer);

            if (clientAddr == null) return;

            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            Logger.debug("Получен пакет от {} ({} байт)", clientAddr, data.length);

            // Десериализация запроса
            Request request = SerializationUtil.deserialize(data, Request.class);
            if (request == null) {
                sendError(clientAddr, "Ошибка десериализации запроса");
                return;
            }

            Logger.info("Запрос {}: {} от {}",
                    request.getRequestId(),
                    request.getCommand().getName(),
                    clientAddr);

            // Обработка команды
            CommandResult result = packetHandler.handle(request, clientAddr, collectionManager);

            // Формирование и отправка ответа
            Response response = new Response(
                    request.getRequestId(),
                    result.isSuccess(),
                    result.getMessage(),
                    result.getData(),
                    Response.AckType.ACK
            );

            responseSender.send(response, clientAddr);
            Logger.debug("Ответ отправлен клиенту {}", clientAddr);

        } catch (IOException | ClassNotFoundException e) {
            Logger.error(e, "Ошибка обработки пакета");
        }
    }

    private void sendError(InetSocketAddress addr, String message) {
        Response error = Response.error(UUID.randomUUID(), message);
        responseSender.send(error, addr);
    }

    public void stop() {
        running = false;
        try {
            channel.close();
            selector.close();
        } catch (IOException e) {
            Logger.error(e, "Ошибка закрытия каналов");
        }
    }
}