package server;

import manager.CollectionManager;
import server.network.UdpServer;
import server.logging.ServerLogger;
import org.tinylog.Logger;
import java.net.InetSocketAddress;

public class ServerApp {
    private static final int DEFAULT_PORT = 1337;
    private static final String DEFAULT_LOG_LEVEL = "info";

    public static void main(String[] args) {
        // Парсинг аргументов: --port 1337 --log-level debug --log-file server.log
        Config config = Config.parse(args);

        // Инициализация логгера
        ServerLogger.configure(config.getLogLevel(), config.getLogFile());
        Logger.info("Запуск сервера на порту {}", config.getPort());

        try {
            CollectionManager collectionManager = new CollectionManager();
            collectionManager.initialize(); // Загрузка из XML

            UdpServer udpServer = new UdpServer(
                    new InetSocketAddress(config.getPort()),
                    collectionManager
            );

            // Hook для сохранения при завершении
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                Logger.info("Получен сигнал завершения, сохраняю коллекцию...");
                collectionManager.save();
                Logger.info("Коллекция сохранена, сервер остановлен");
            }));

            // Запуск в однопоточном режиме (блокирующий цикл)
            udpServer.start();

        } catch (Exception e) {
            Logger.error(e, "Критическая ошибка сервера");
            System.exit(1);
        }
    }
}