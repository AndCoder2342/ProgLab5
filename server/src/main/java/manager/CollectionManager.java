package manager;

import enums.UnitOfMeasure;
import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import org.tinylog.Logger;

/**
 * Менеджер коллекции с поддержкой PostgreSQL и ReadWriteLock
 */
public class CollectionManager {

    private Hashtable<Long, Product> collection;
    private java.util.Date initializationDate;
    private final DatabaseManager dbManager;
    private final UserManager userManager;
    private final ReadWriteLock lock;

    public CollectionManager(DatabaseManager dbManager, UserManager userManager) {
        this.collection = new Hashtable<>();
        this.initializationDate = new java.util.Date();
        this.dbManager = dbManager;
        this.userManager = userManager;
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Загружает коллекцию из БД при старте сервера
     */
    public void loadFromDatabase() {
        lock.writeLock().lock();
        try {
            collection.clear();
            String sql = "SELECT * FROM products ORDER BY id";

            try (Connection conn = dbManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Product product = mapResultSetToProduct(rs);
                    collection.put(product.getId(), product);
                }

                Logger.info("Загружено {} продуктов из БД", collection.size());

            } catch (SQLException e) {
                Logger.error(e, "Ошибка загрузки коллекции из БД");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Добавляет продукт в БД и кэш
     */
    public boolean insert(Product product, int ownerId) {
        lock.writeLock().lock();
        try {
            // Генерируем новый ID через sequence
            String idSql = "SELECT nextval('products_id_seq')";
            long newId;

            try (Connection conn = dbManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(idSql)) {

                rs.next();
                newId = rs.getLong(1);

            } catch (SQLException e) {
                Logger.error(e, "Ошибка получения ID из sequence");
                return false;
            }

            product.setId(newId);
            product.setCreationDate(new java.util.Date());
            product.setOwnerId(ownerId);

            // Вставляем в БД (11 параметров)
            String insertSql = """
            INSERT INTO products (
                id, name, coordinates_x, coordinates_y, price, unit_of_measure,
                owner_id,
                manufacturer_name, manufacturer_full_name,
                manufacturer_annual_turnover, manufacturer_employees
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (Connection conn = dbManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(insertSql)) {

                prepareProductStatementForInsert(stmt, product, ownerId);
                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    collection.put(newId, product);
                    Logger.info("Добавлен продукт ID={} (владелец: {})", newId, ownerId);
                    return true;
                }

            } catch (SQLException e) {
                Logger.error(e, "Ошибка добавления продукта в БД");
                return false;
            }

            return false;

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Обновляет продукт (только если владелец)
     */
    public boolean update(Long id, Product newProduct, int ownerId) {
        lock.writeLock().lock();
        try {
            Product existing = collection.get(id);
            Logger.info("UPDATE: id={}, existing={}, ownerId={}", id, existing != null, ownerId);

            if (existing == null) {
                Logger.warn("Продукт ID={} не найден", id);
                return false;
            }

            // Проверяем владельца
            if (!isOwner(existing, ownerId)) {
                Logger.warn("Пользователь {} не является владельцем продукта {}", ownerId, id);
                return false;
            }

            // Сохраняем старые данные которые нельзя менять
            newProduct.setId(id);
            newProduct.setCreationDate(existing.getCreationDate());
            newProduct.setOwnerId(existing.getOwnerId());

            // Правильный SQL - 10 параметров (9 полей + WHERE id=?)
            String sql = """
            UPDATE products SET
                name=?, coordinates_x=?, coordinates_y=?, price=?, unit_of_measure=?,
                manufacturer_name=?, manufacturer_full_name=?,
                manufacturer_annual_turnover=?, manufacturer_employees=?,
                updated_at=CURRENT_TIMESTAMP
            WHERE id=?
            """;

            try (Connection conn = dbManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                prepareProductStatementForUpdate(stmt, newProduct);
                stmt.setLong(10, id);  //  WHERE id=? (10-й параметр, не 11-й!)

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    collection.put(id, newProduct);
                    Logger.info("Обновлен продукт ID={}", id);
                    return true;
                }

            } catch (SQLException e) {
                Logger.error(e, "Ошибка обновления продукта ID={}", id);
            }

            return false;

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Подготовка параметров для INSERT (11 параметров, С owner_id)
     */
    private void prepareProductStatementForInsert(PreparedStatement stmt, Product p, int ownerId) throws SQLException {
        int idx = 1;

        stmt.setLong(idx++, p.getId());              // 1. id
        stmt.setString(idx++, p.getName());          // 2. name
        stmt.setInt(idx++, p.getCoordinates().getX()); // 3. coordinates_x
        stmt.setDouble(idx++, p.getCoordinates().getY()); // 4. coordinates_y
        stmt.setInt(idx++, p.getPrice());            // 5. price
        stmt.setString(idx++, p.getUnitOfMeasure() != null ? p.getUnitOfMeasure().name() : null); // 6. unit_of_measure
        stmt.setInt(idx++, ownerId);                 // 7. owner_id

        // Manufacturer (4 поля: 8, 9, 10, 11)
        if (p.getManufacturer() != null) {
            Organization org = p.getManufacturer();
            stmt.setString(idx++, org.getName());
            stmt.setString(idx++, org.getFullName() != null ? org.getFullName() : "");
            if (org.getAnnualTurnover() != null) {
                stmt.setDouble(idx++, org.getAnnualTurnover());
            } else {
                stmt.setNull(idx++, Types.DOUBLE);
            }
            stmt.setInt(idx++, org.getEmployeesCount());
        } else {
            stmt.setNull(idx++, Types.VARCHAR);
            stmt.setString(idx++, "");
            stmt.setNull(idx++, Types.DOUBLE);
            stmt.setInt(idx++, 0);
        }
        // Итого 11 параметров
    }

    /**
     * Подготовка параметров для UPDATE (9 параметров, БЕЗ owner_id)
     */
    private void prepareProductStatementForUpdate(PreparedStatement stmt, Product p) throws SQLException {
        int idx = 1;

        stmt.setString(idx++, p.getName());              // 1. name
        stmt.setInt(idx++, p.getCoordinates().getX());   // 2. coordinates_x
        stmt.setDouble(idx++, p.getCoordinates().getY()); // 3. coordinates_y
        stmt.setInt(idx++, p.getPrice());                // 4. price
        stmt.setString(idx++, p.getUnitOfMeasure() != null ? p.getUnitOfMeasure().name() : null); // 5. unit_of_measure

        // Manufacturer (4 поля: 6, 7, 8, 9)
        if (p.getManufacturer() != null) {
            Organization org = p.getManufacturer();
            stmt.setString(idx++, org.getName());
            stmt.setString(idx++, org.getFullName() != null ? org.getFullName() : "");
            if (org.getAnnualTurnover() != null) {
                stmt.setDouble(idx++, org.getAnnualTurnover());
            } else {
                stmt.setNull(idx++, Types.DOUBLE);
            }
            stmt.setInt(idx++, org.getEmployeesCount());
        } else {
            stmt.setNull(idx++, Types.VARCHAR);
            stmt.setString(idx++, "");
            stmt.setNull(idx++, Types.DOUBLE);
            stmt.setInt(idx++, 0);
        }
        // Итого 9 параметров + WHERE id=? (10-й параметр)
    }

    /**
     * Удаляет продукт по ID (только владелец)
     */
    public boolean removeKey(Long key, int ownerId) {
        lock.writeLock().lock();
        try {
            Product product = collection.get(key);
            if (product == null) {
                Logger.warn("Продукт ID={} не найден", key);
                return false;
            }

            // Проверяем владельца
            if (!isOwner(product, ownerId)) {
                Logger.warn("Пользователь {} не является владельцем продукта {}", ownerId, key);
                return false;
            }

            String sql = "DELETE FROM products WHERE id=? AND owner_id=?";

            try (Connection conn = dbManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, key);
                stmt.setInt(2, ownerId);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    collection.remove(key);
                    Logger.info("Удален продукт ID={}", key);
                    return true;
                }

            } catch (SQLException e) {
                Logger.error(e, "Ошибка удаления продукта ID={}", key);
            }

            return false;

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Очищает коллекцию (только свои продукты)
     */
    public int clear(int ownerId) {
        lock.writeLock().lock();
        try {
            String sql = "DELETE FROM products WHERE owner_id=?";

            try (Connection conn = dbManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, ownerId);
                int deletedCount = stmt.executeUpdate();

                // Очищаем только свои продукты из кэша
                collection.keySet().removeIf(id -> {
                    Product p = collection.get(id);
                    return isOwner(p, ownerId);
                });

                Logger.info("Очищено {} продуктов пользователя {}", deletedCount, ownerId);
                return deletedCount;

            } catch (SQLException e) {
                Logger.error(e, "Ошибка очистки коллекции");
                return 0;
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Удаляет продукты больше заданного
     */
    public int removeGreater(Product product, int ownerId) {
        lock.writeLock().lock();
        try {
            List<Long> toRemove = collection.values().stream()
                    .filter(p -> isOwner(p, ownerId))
                    .filter(p -> p.compareTo(product) > 0)
                    .map(Product::getId)
                    .collect(Collectors.toList());

            if (toRemove.isEmpty()) return 0;

            String sql = "DELETE FROM products WHERE id=ANY(?) AND owner_id=?";

            try (Connection conn = dbManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                Array idArray = conn.createArrayOf("bigint", toRemove.toArray(new Long[0]));
                stmt.setArray(1, idArray);
                stmt.setInt(2, ownerId);

                int deleted = stmt.executeUpdate();
                toRemove.forEach(id -> collection.remove(id));

                Logger.info("Удалено {} продуктов (больше заданного)", deleted);
                return deleted;

            } catch (SQLException e) {
                Logger.error(e, "Ошибка удаления продуктов (greater)");
                return 0;
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Удаляет продукты меньше заданного
     */
    public int removeLower(Product product, int ownerId) {
        lock.writeLock().lock();
        try {
            List<Long> toRemove = collection.values().stream()
                    .filter(p -> isOwner(p, ownerId))
                    .filter(p -> p.compareTo(product) < 0)
                    .map(Product::getId)
                    .collect(Collectors.toList());

            if (toRemove.isEmpty()) return 0;

            String sql = "DELETE FROM products WHERE id=ANY(?) AND owner_id=?";

            try (Connection conn = dbManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                Array idArray = conn.createArrayOf("bigint", toRemove.toArray(new Long[0]));
                stmt.setArray(1, idArray);
                stmt.setInt(2, ownerId);

                int deleted = stmt.executeUpdate();
                toRemove.forEach(id -> collection.remove(id));

                Logger.info("Удалено {} продуктов (меньше заданного)", deleted);
                return deleted;

            } catch (SQLException e) {
                Logger.error(e, "Ошибка удаления продуктов (lower)");
                return 0;
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Заменяет продукт если новый больше (только владелец)
     */
    public boolean replaceIfGreater(Long key, Product newProduct, int ownerId) {
        lock.writeLock().lock();
        try {
            Product oldProduct = collection.get(key);
            if (oldProduct == null) {
                return false;
            }

            if (!isOwner(oldProduct, ownerId)) {
                Logger.warn("Пользователь {} не является владельцем", ownerId);
                return false;
            }

            if (newProduct.compareTo(oldProduct) > 0) {
                return update(key, newProduct, ownerId);
            }

            return false;

        } finally {
            lock.writeLock().unlock();
        }
    }



    public String getInfo() {
        lock.readLock().lock();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Тип коллекции: Product Collection (на основе Hashtable)\n");
            sb.append("Дата инициализации: ").append(initializationDate).append("\n");
            sb.append("Количество элементов: ").append(collection.size()).append("\n");
            sb.append("Источник данных: PostgreSQL\n");
            sb.append("Поддерживаемые операции: CRUD, фильтрация, группировка");

            return sb.toString();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Collection<Product> getAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(collection.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<String, Long> groupCountingByManufacturer() {
        lock.readLock().lock();
        try {
            return collection.values().stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getManufacturer() != null ? p.getManufacturer().getName() : "null",
                            Collectors.counting()
                    ));
        } finally {
            lock.readLock().unlock();
        }
    }

    public long countGreaterThanManufacturer(Organization manufacturer) {
        lock.readLock().lock();
        try {
            return collection.values().stream()
                    .filter(p -> p.getManufacturer() != null)
                    .filter(p -> p.getManufacturer().getName().compareTo(manufacturer.getName()) > 0)
                    .count();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Product> filterContainsName(String substring) {
        lock.readLock().lock();
        try {
            return collection.values().stream()
                    .filter(product -> containsInAnyField(product, substring))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }



    /**
     * Проверяет, является ли пользователь владельцем продукта
     */
    private boolean isOwner(Product product, int userId) {
        if (product == null) return false;

        Integer productOwnerId = product.getOwnerId();

        if (productOwnerId == null) {
            Logger.warn("Продукт ID={} не имеет ownerId. Доступ разрешён.", product.getId());
            return true;
        }

        boolean isOwner = (productOwnerId == userId);
        Logger.info("isOwner: productId={}, productOwnerId={}, userId={}, result={}",
                product.getId(), productOwnerId, userId, isOwner);

        return isOwner;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));

        Coordinates coords = new Coordinates();
        coords.setX(rs.getInt("coordinates_x"));
        coords.setY((float) rs.getDouble("coordinates_y"));
        product.setCoordinates(coords);

        product.setPrice(rs.getInt("price"));

        String unitStr = rs.getString("unit_of_measure");
        if (unitStr != null && !unitStr.isEmpty()) {
            product.setUnitOfMeasure(enums.UnitOfMeasure.valueOf(unitStr));
        }

        // ЧИТАЕМ ownerId из БД
        int ownerIdDb = rs.getInt("owner_id");
        if (!rs.wasNull()) {
            product.setOwnerId(ownerIdDb);
        }

        // Manufacturer
        String orgName = rs.getString("manufacturer_name");
        if (orgName != null && !orgName.isEmpty()) {
            Organization org = new Organization();
            org.setName(orgName);
            org.setFullName(rs.getString("manufacturer_full_name"));

            double turnover = rs.getDouble("manufacturer_annual_turnover");
            if (!rs.wasNull()) {
                org.setAnnualTurnover((long) turnover);
            }

            org.setEmployeesCount(rs.getInt("manufacturer_employees"));
            product.setManufacturer(org);
        }

        return product;
    }

    private boolean containsInAnyField(Product product, String substring) {
        if (substring == null || substring.isEmpty()) {
            return true;
        }

        String lowerSubstring = substring.toLowerCase();

        if (product.getId() != null && String.valueOf(product.getId()).contains(lowerSubstring)) {
            return true;
        }

        if (product.getName() != null && product.getName().toLowerCase().contains(lowerSubstring)) {
            return true;
        }

        if (product.getCoordinates() != null) {
            if (product.getCoordinates().getX() != null &&
                    String.valueOf(product.getCoordinates().getX()).contains(lowerSubstring)) {
                return true;
            }
            if (product.getCoordinates().getY() != null &&
                    String.valueOf(product.getCoordinates().getY()).contains(lowerSubstring)) {
                return true;
            }
        }

        if (String.valueOf(product.getPrice()).contains(lowerSubstring)) {
            return true;
        }

        if (product.getCreationDate() != null) {
            String dateString = product.getCreationDate().toString().toLowerCase();
            if (dateString.contains(lowerSubstring)) {
                return true;
            }
        }

        if (product.getUnitOfMeasure() != null) {
            String unitString = product.getUnitOfMeasure().name().toLowerCase();
            if (unitString.contains(lowerSubstring)) {
                return true;
            }
        }

        if (product.getManufacturer() != null) {
            Organization org = product.getManufacturer();

            if (org.getId() != null && String.valueOf(org.getId()).contains(lowerSubstring)) {
                return true;
            }

            if (org.getName() != null && org.getName().toLowerCase().contains(lowerSubstring)) {
                return true;
            }

            if (org.getFullName() != null && org.getFullName().toLowerCase().contains(lowerSubstring)) {
                return true;
            }

            if (org.getAnnualTurnover() != null &&
                    String.valueOf(org.getAnnualTurnover()).contains(lowerSubstring)) {
                return true;
            }

            if (String.valueOf(org.getEmployeesCount()).contains(lowerSubstring)) {
                return true;
            }
        }

        return false;
    }

    public Hashtable<Long, Product> getCollection() {
        lock.readLock().lock();
        try {
            return new Hashtable<>(collection);
        } finally {
            lock.readLock().unlock();
        }
    }

    public UserManager getUserManager() {
        return userManager;
    }
}