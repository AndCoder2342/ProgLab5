package manager;

import java.sql.*;
import java.util.*;
import org.tinylog.Logger;


public class ProductRepository {

    private final DatabaseManager dbManager;

    public ProductRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }


    public List<Product> loadAll() {
        List<Product> result = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(mapResultSetToProduct(rs));
            }

            Logger.info("загружено {} продуктов из БД", result.size());

        } catch (SQLException e) {
            Logger.error(e, "ошибка загрузки коллекции из БД");
        }

        return result;
    }


    public long generateNextId() {
        String sql = "SELECT nextval('products_id_seq')";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            rs.next();
            return rs.getLong(1);

        } catch (SQLException e) {
            Logger.error(e, "ошибка получения ID из sequence");
            throw new RuntimeException("не удалось сгенерировать ID", e);
        }
    }


    public boolean insert(Product product, int ownerId) {
        String sql = """
            INSERT INTO products (
                id, name, coordinates_x, coordinates_y, price, unit_of_measure,
                owner_id,
                manufacturer_name, manufacturer_full_name,
                manufacturer_annual_turnover, manufacturer_employees
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            prepareProductStatementForInsert(stmt, product, ownerId);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            Logger.error(e, "ошибка добавления продукта в БД");
            return false;
        }
    }


    public boolean update(Product product) {
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

            prepareProductStatementForUpdate(stmt, product);
            stmt.setLong(10, product.getId());
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            Logger.error(e, "ошибка обновления продукта ID={}", product.getId());
            return false;
        }
    }


    public boolean deleteByIdAndOwner(Long id, int ownerId) {
        String sql = "DELETE FROM products WHERE id=? AND owner_id=?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.setInt(2, ownerId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.error(e, "ошибка удаления продукта ID={}", id);
            return false;
        }
    }


    public int deleteByOwner(int ownerId) {
        String sql = "DELETE FROM products WHERE owner_id=?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ownerId);
            return stmt.executeUpdate();

        } catch (SQLException e) {
            Logger.error(e, "ошибка очистки коллекции");
            return 0;
        }
    }


    public int deleteByIds(List<Long> ids, int ownerId) {
        if (ids.isEmpty()) return 0;

        String sql = "DELETE FROM products WHERE id=ANY(?) AND owner_id=?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            Array idArray = conn.createArrayOf("bigint", ids.toArray(new Long[0]));
            stmt.setArray(1, idArray);
            stmt.setInt(2, ownerId);
            return stmt.executeUpdate();

        } catch (SQLException e) {
            Logger.error(e, "ошибка удаления продуктов");
            return 0;
        }
    }


    private void prepareProductStatementForInsert(PreparedStatement stmt, Product p, int ownerId) throws SQLException {
        int idx = 1;

        stmt.setLong(idx++, p.getId());
        stmt.setString(idx++, p.getName());
        stmt.setInt(idx++, p.getCoordinates().getX());
        stmt.setDouble(idx++, p.getCoordinates().getY());
        stmt.setInt(idx++, p.getPrice());
        stmt.setString(idx++, p.getUnitOfMeasure() != null ? p.getUnitOfMeasure().name() : null);
        stmt.setInt(idx++, ownerId);

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
    }


    private void prepareProductStatementForUpdate(PreparedStatement stmt, Product p) throws SQLException {
        int idx = 1;

        stmt.setString(idx++, p.getName());
        stmt.setInt(idx++, p.getCoordinates().getX());
        stmt.setDouble(idx++, p.getCoordinates().getY());
        stmt.setInt(idx++, p.getPrice());
        stmt.setString(idx++, p.getUnitOfMeasure() != null ? p.getUnitOfMeasure().name() : null);

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

        int ownerIdDb = rs.getInt("owner_id");
        if (!rs.wasNull()) {
            product.setOwnerId(ownerIdDb);
        }

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
}