package client;

import client.console.ConsoleReader;
import client.network.UdpClient;
import shared.Request;
import shared.Response;
import commands.CommandResult;
import org.tinylog.Logger;
import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

public class ClientApp {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 1337;
    private static final int REQUEST_TIMEOUT_MS = 10000;
    private static final int MAX_RETRIES = 3;

    public static void main(String[] args) {
        Config config = Config.parse(args);
        ClientLogger.configure(config.getLogLevel(), config.getLogFile());

        Logger.info("Запуск клиента, сервер: {}:{}", config.getHost(), config.getPort());

        try (UdpClient udpClient = new UdpClient(config.getHost(), config.getPort())) {
            ConsoleReader consoleReader = new ConsoleReader(udpClient.getClientId());

            System.out.println("=== Клиент коллекции продуктов ===");
            System.out.println("Введите 'help' для списка команд, 'exit' для выхода");

            boolean running = true;
            while (running) {
                try {
                    Request request = consoleReader.readCommand();
                    if (request == null) continue;

                    // Проверка на exit ДО отправки на сервер
                    if ("exit".equalsIgnoreCase(request.getCommand().getName())) {
                        System.out.println("Завершение работы клиента...");
                        break;
                    }

                    // Отправка с обработкой недоступности сервера
                    Response response = udpClient.sendWithRetry(
                            request, MAX_RETRIES, REQUEST_TIMEOUT_MS
                    );

                    // Обработка ответа
                    handleResponse(response);

                } catch (TimeoutException e) {
                    System.err.println("⚠ Сервер не отвечает. Проверьте соединение.");
                    Logger.warn("Таймаут соединения с сервером");
                } catch (RuntimeException e) {
                    System.err.println("❌ Ошибка: " + e.getMessage());
                    Logger.error(e, "Необработанная ошибка клиента");
                }
            }

        } catch (Exception e) {
            Logger.error(e, "Критическая ошибка клиента");
            System.err.println("Фатальная ошибка: " + e.getMessage());
            System.exit(1);
        }

        Logger.info("Клиент завершил работу");
    }

    private static void handleResponse(Response response) {
        // Вывод уникального ID запроса для трассировки
        System.out.println("[request:" + response.getRequestId() + "]");

        if (response.isSuccess()) {
            System.out.println("✓ " + response.getMessage());

            // Обработка данных ответа
            Object data = response.getData();
            if (data != null) {
                if (data instanceof Iterable<?> items) {
                    // Сортировка по умолчанию перед выводом
                    items.stream()
                            .sorted((a, b) -> {
                                if (a instanceof Comparable c1 && b instanceof Comparable c2) {
                                    return c1.compareTo(c2);
                                }
                                return 0;
                            })
                            .forEach(item -> System.out.println("  • " + item));
                } else {
                    System.out.println("  Результат: " + data);
                }
            }
        } else {
            System.err.println("✗ Ошибка: " + response.getMessage());
        }
        System.out.println();
    }
}