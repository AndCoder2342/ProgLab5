package server;

import manager.CollectionManager;
import manager.DatabaseManager;
import manager.UserManager;
import org.tinylog.Logger;
import server.network.UdpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

public class ServerApp {
    public static void main(String[] args) {
        // === Инициализация БД ===
        DatabaseManager db = DatabaseManager.getInstance();

        if (!db.testConnection()) {
            Logger.error("Не удалось подключиться к базе данных");
            System.exit(1);
        }

        Logger.info("Подключение к БД успешно!");
        db.initializeSchema();
        Logger.info("Схема инициализирована!");

        // === Инициализация менеджеров ===
        UserManager userManager = new UserManager(db);
        CollectionManager collectionManager = new CollectionManager(db, userManager);

        // Загружаем коллекцию из БД
        collectionManager.loadFromDatabase();

        // === Настройки сервера ===
        int port = 1337;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-p") || args[i].equals("--port")) {
                if (i + 1 < args.length) {
                    try {
                        port = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException e) {
                        Logger.warn("Неверный порт: {}, используем 1337", args[i + 1]);
                    }
                }
            }
        }

        Logger.info("Запуск Сервера");
        Logger.info("Порт: {}, Логирование: info", port);

        // === AtomicReference для безопасного использования в lambda ===
        AtomicReference<UdpServer> udpServerRef = new AtomicReference<>();

        // === Shutdown hook для корректной остановки ===
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.info("Получен сигнал завершения...");
            UdpServer server = udpServerRef.get();
            if (server != null) {
                server.stop();
            }
        }));

        try {
            // Создаём сервер
            UdpServer udpServer = new UdpServer(
                    new InetSocketAddress(port),
                    collectionManager
            );

            // Сохраняем ссылку для shutdown hook (AtomicReference позволяет это)
            udpServerRef.set(udpServer);

            Logger.info("Сервер готов к приему подключений...");

            // Запускаем (блокирующий вызов)
            udpServer.start();

        } catch (Exception e) {
            Logger.error(e, "Критическая ошибка при запуске сервера");
            UdpServer server = udpServerRef.get();
            if (server != null) {
                server.stop();
            }
            System.exit(1);
        }
    }
}