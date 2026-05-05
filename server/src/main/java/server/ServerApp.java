package server;

import manager.CollectionManager;
import server.Config; // или shared.Config, если Config лежит в shared
import org.tinylog.Logger;
import server.network.UdpServer;
import java.net.InetSocketAddress;

/**
 * Главное серверное приложение.
 * Отвечает за:
 * 1. Инициализацию коллекции из файла.
 * 2. Запуск UDP-сервера в однопоточном режиме.
 * 3. Сохранение данных при завершении.
 */
public class ServerApp {

    public static void main(String[] args) {
        // парсим конфигурацию
        Config config = Config.parse(args);
        System.setProperty("log-level", config.getLogLevel());
        System.setProperty("log-file", config.getLogFile());

        Logger.info("Запуск Сервера");
        Logger.info("Порт: {}, Логирование: {}", config.getPort(), config.getLogLevel());


        try {
            // инициализация менеджера коллекции (чтение из XML)

            CollectionManager collectionManager = new CollectionManager();
            collectionManager.initialize();
            Logger.info("Коллекция загружена. Элементов: {}", collectionManager.getCollection().size());


            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                Logger.info("Получен сигнал завершения...");
                if (collectionManager != null) {
                    collectionManager.save();
                    Logger.info("Коллекция успешно сохранена в файл.");
                }
            }));

            // запуск сетевого модуля
            UdpServer udpServer = new UdpServer(
                    new InetSocketAddress(config.getPort()),
                    collectionManager
            );

            Logger.info("Сервер готов к приему подключений...");
            udpServer.start(); // блокирующий метод

        } catch (Exception e) {
            Logger.error(e, "Критическая ошибка при запуске сервера");
            System.exit(1);
        }
    }
}