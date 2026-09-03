package manager;


import org.tinylog.Logger;

import java.sql.*;
import java.util.Optional;


public class UserManager {

    private final DatabaseManager dbManager;

    public UserManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }



    public String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD2");
            byte[] hashBytes = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // байты в hex
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (java.security.NoSuchAlgorithmException e) {
            Logger.error(e, "Алгоритм MD2 не найден");
            throw new RuntimeException("MD2 не поддерживается", e);
        }
    }


    public int register(String username, String password) {

        if (username == null || username.trim().isEmpty() || username.length() < 3) {
            Logger.warn("Попытка регистрации с пустым или коротким логином");
            return -3;
        }
        if (password == null || password.length() < 4) {
            Logger.warn("Попытка регистрации с коротким паролем");
            return -3;
        }

        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?) RETURNING id";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {


            String hashedPassword = hashPassword(password); // хэширование

            stmt.setString(1, username.trim());
            stmt.setString(2, hashedPassword);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");
                Logger.info("Пользователь '{}' зарегистрирован с ID={}", username, userId);
                return userId;
            }

        } catch (SQLException e) {

            String msg = e.getMessage();
            if (msg.contains("duplicate key") || msg.contains("уникальный") || msg.contains("already exists")) {
                Logger.warn("Пользователь '{}' уже существует", username);
                return -2;
            } else {
                Logger.error(e, "Ошибка БД при регистрации '{}'", username);
                return -1;
            }
        }

        return -1;
    }


    public Optional<User> login(String username, String password) {
        String sql = "SELECT id, username, password_hash, created_at FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = hashPassword(password);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                Logger.info("Пользователь {} авторизован", username);
                return Optional.of(user);
            }

        } catch (SQLException e) {
            Logger.error(e, "Ошибка при авторизации пользователя {}", username);
        }

        return Optional.empty();
    }


    public boolean validateUser(String username, String password) {
        return login(username, password).isPresent();
    }


    public Optional<User> getUserById(int userId) {
        String sql = "SELECT id, username, created_at FROM users WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return Optional.of(user);
            }

        } catch (SQLException e) {
            Logger.error(e, "Ошибка получения пользователя с ID={}", userId);
        }

        return Optional.empty();
    }
}
