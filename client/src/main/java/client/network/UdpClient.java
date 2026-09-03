package client.network;

import shared.Request;
import shared.Response;
import shared.SerializationUtil;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.UUID;

public class UdpClient {
    private final DatagramChannel channel;
    private final InetSocketAddress serverAddress;
    private final UUID clientId;

    public UdpClient(String host, int port) throws IOException {
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.serverAddress = new InetSocketAddress(host, port);
        this.clientId = UUID.randomUUID();
        Logger.info("Клиент запущен. ID: {}", clientId);
    }

        //блокирует поток, пока не придет ответ

    public Response sendRequest(Request request) throws IOException, ClassNotFoundException {
        byte[] data = SerializationUtil.serialize(request);
        ByteBuffer buffer = ByteBuffer.wrap(data);


        channel.send(buffer, serverAddress);
        Logger.debug("Запрос {} отправлен", request.getRequestId());

        Selector selector = Selector.open();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);


        int readyChannels = selector.select(3000);

        if (readyChannels == 0) {

            Logger.warn("Таймаут ожидания ответа от сервера");
            selector.close();
            return null;
        }


        ByteBuffer receiveBuffer = ByteBuffer.allocate(65535);
        channel.receive(receiveBuffer);
        receiveBuffer.flip();

        byte[] responseData = new byte[receiveBuffer.remaining()];
        receiveBuffer.get(responseData);

        selector.close();

        return SerializationUtil.deserialize(responseData, Response.class);
    }

    public void close() throws IOException {
        channel.close();
    }


    public UUID getClientId() {
        return clientId;
    }
}