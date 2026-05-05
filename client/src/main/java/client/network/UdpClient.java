package client.network;

import shared.Request;
import shared.Response;
import shared.SerializationUtil;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.UUID;

public class UdpClient {
    private final DatagramChannel channel;
    private final InetSocketAddress serverAddress;
    private final UUID clientId;

    public UdpClient(String host, int port) throws IOException {
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false); // Неблокирующий режим
        this.serverAddress = new InetSocketAddress(host, port);
        this.clientId = UUID.randomUUID();
        Logger.info("Клиент запущен. ID: {}", clientId);
    }

    /**
     * Отправляет запрос и ждет ответ (блокирует поток, пока не придет ответ)
     */
    public Response sendRequest(Request request) throws IOException, ClassNotFoundException {
        byte[] data = SerializationUtil.serialize(request);
        ByteBuffer buffer = ByteBuffer.wrap(data);

        // Отправляем пакет
        channel.send(buffer, serverAddress);
        Logger.debug("Запрос {} отправлен", request.getRequestId());

        // Ждем ответ
        ByteBuffer receiveBuffer = ByteBuffer.allocate(65535);
        // Блокируем канал только на время чтения ответа
        channel.configureBlocking(true);
        channel.receive(receiveBuffer);
        channel.configureBlocking(false);

        receiveBuffer.flip();
        byte[] responseData = new byte[receiveBuffer.remaining()];
        receiveBuffer.get(responseData);

        return SerializationUtil.deserialize(responseData, Response.class);
    }

    public void close() throws IOException {
        channel.close();
    }

    /**
     * Возвращает ID клиента
     */
    public UUID getClientId() {
        return clientId;
    }
}