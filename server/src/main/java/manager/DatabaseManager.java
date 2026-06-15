package manager;

import org.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Менеджер подключения к базе данных PostgreSQL
 */
public class DatabaseManager {

    private static final String CONFIG_FILE = "database.properties";
    private static final String SCHEMA_FILE = "schema.sql";

    private String url;
    private String username;
    private String password;

    private static DatabaseManager instance;

    private DatabaseManager() {
        loadConfig();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Загружает настройки из database.properties
     */
    private void loadConfig() {
        String host = System.getenv("DB_HOST");
        if (host == null || host.isEmpty()) {
            host = "localhost";
        }

        String portStr = System.getenv("DB_PORT");
        int port = 5432;
        if (portStr != null && !portStr.isEmpty()) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                Logger.warn("Неверный порт БД: {}, используем 5432", portStr);
            }
        }

        String database = System.getenv("DB_NAME");
        if (database == null || database.isEmpty()) {
            database = "prog_lab5";
        }

        String user = System.getenv("DB_USER");
        if (user == null || user.isEmpty()) {
            user = "postgres";
        }

        String password = System.getenv("DB_PASSWORD");
        if (password == null || password.isEmpty()) {
            password = "postgres";
        }

        this.url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        this.username = user;
        this.password = password;

        Logger.info("Загружены настройки БД: jdbc:postgresql://{}:{}/{}", host, port, database);
    }

    /**
     * Возвращает новое соединение с БД
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Инициализирует схему БД (выполняет schema.sql)
     * Вызывать один раз при старте сервера
     */
    public void initializeSchema() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {


            String schemaSql = readSchemaFile();


            String[] statements = schemaSql.split(";");
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }

            Logger.info("Схема базы данных инициализирована успешно");

        } catch (SQLException e) {
            Logger.error(e, "Ошибка при инициализации схемы БД");
            throw new RuntimeException("Не удалось инициализировать схему БД", e);
        } catch (IOException e) {
            Logger.error(e, "Не удалось прочитать файл схемы");
            throw new RuntimeException("Файл схемы не найден", e);
        }
    }

    /**
     * Читает содержимое schema.sql из ресурсов
     */
    private String readSchemaFile() throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(SCHEMA_FILE)) {
            if (input == null) {
                throw new IOException("Файл схемы не найден: " + SCHEMA_FILE);
            }
            return new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    /**
     * Проверяет подключение к БД
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            Logger.warn(e, "Не удалось подключиться к базе данных");
            return false;
        }
    }
}