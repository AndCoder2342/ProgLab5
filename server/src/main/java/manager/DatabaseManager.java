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
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Файл конфигурации не найден: " + CONFIG_FILE);
            }

            Properties props = new Properties();
            props.load(input);

            String host = props.getProperty("db.host", "localhost");
            String port = props.getProperty("db.port", "5432");
            String dbname = props.getProperty("db.name", "prog_lab5");

            this.url = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbname);
            this.username = props.getProperty("db.username", "postgres");
            this.password = props.getProperty("db.password", "");

            Logger.info("Загружены настройки БД: {}", url);

        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки конфигурации БД", e);
        }
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

            // Читаем SQL скрипт из ресурсов
            String schemaSql = readSchemaFile();

            // Выполняем все команды (разделенные ;)
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