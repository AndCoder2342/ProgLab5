package manager;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import org.tinylog.Logger;


public class CollectionManager {

    private Hashtable<Long, Product> collection;
    private java.util.Date initializationDate;
    private final ProductRepository repository;
    private final UserManager userManager;
    private final ReadWriteLock lock;

    public CollectionManager(DatabaseManager dbManager, UserManager userManager) {
        this.collection = new Hashtable<>();
        this.initializationDate = new java.util.Date();
        this.repository = new ProductRepository(dbManager);
        this.userManager = userManager;
        this.lock = new ReentrantReadWriteLock();
    }


    public void loadFromDatabase() {
        lock.writeLock().lock();
        try {
            collection.clear();
            List<Product> products = repository.loadAll();
            for (Product p : products) {
                collection.put(p.getId(), p);
            }
            Logger.info("загружено {} продуктов из БД", collection.size());
        } finally {
            lock.writeLock().unlock();
        }
    }


    public boolean insert(Product product, int ownerId) {
        lock.writeLock().lock();
        try {
            long newId = repository.generateNextId();
            product.setId(newId);
            product.setCreationDate(new java.util.Date());
            product.setOwnerId(ownerId);

            if (!repository.insert(product, ownerId)) {
                return false;
            }

            collection.put(newId, product);
            Logger.info("добавлен продукт ID={} (владелец: {})", newId, ownerId);
            return true;

        } finally {
            lock.writeLock().unlock();
        }
    }


    public boolean update(Long id, Product newProduct, int ownerId) {
        lock.writeLock().lock();
        try {
            Product existing = collection.get(id);
            Logger.info("UPDATE: id={}, existing={}, ownerId={}", id, existing != null, ownerId);

            if (existing == null) {
                Logger.warn("продукт ID={} не найден", id);
                return false;
            }

            if (!isOwner(existing, ownerId)) {
                Logger.warn("пользователь {} не является владельцем продукта {}", ownerId, id);
                return false;
            }

            newProduct.setId(id);
            newProduct.setCreationDate(existing.getCreationDate());
            newProduct.setOwnerId(existing.getOwnerId());

            if (!repository.update(newProduct)) {
                return false;
            }

            collection.put(id, newProduct);
            Logger.info("обновлен продукт ID={}", id);
            return true;

        } finally {
            lock.writeLock().unlock();
        }
    }


    public boolean removeKey(Long key, int ownerId) {
        lock.writeLock().lock();
        try {
            Product product = collection.get(key);
            if (product == null) {
                Logger.warn("продукт ID={} не найден", key);
                return false;
            }

            if (!isOwner(product, ownerId)) {
                Logger.warn("пользователь {} не является владельцем продукта {}", ownerId, key);
                return false;
            }

            if (!repository.deleteByIdAndOwner(key, ownerId)) {
                return false;
            }

            collection.remove(key);
            Logger.info("удален продукт ID={}", key);
            return true;

        } finally {
            lock.writeLock().unlock();
        }
    }


    public int clear(int ownerId) {
        lock.writeLock().lock();
        try {
            int deletedCount = repository.deleteByOwner(ownerId);

            collection.keySet().removeIf(id -> {
                Product p = collection.get(id);
                return isOwner(p, ownerId);
            });

            Logger.info("очищено {} продуктов пользователя {}", deletedCount, ownerId);
            return deletedCount;

        } finally {
            lock.writeLock().unlock();
        }
    }


    public int removeGreater(Product product, int ownerId) {
        lock.writeLock().lock();
        try {
            List<Long> toRemove = collection.values().stream()
                    .filter(p -> isOwner(p, ownerId))
                    .filter(p -> p.compareTo(product) > 0)
                    .map(Product::getId)
                    .collect(Collectors.toList());

            if (toRemove.isEmpty()) return 0;

            int deleted = repository.deleteByIds(toRemove, ownerId);
            toRemove.forEach(id -> collection.remove(id));

            Logger.info("удалено {} продуктов (больше заданного)", deleted);
            return deleted;

        } finally {
            lock.writeLock().unlock();
        }
    }


    public int removeLower(Product product, int ownerId) {
        lock.writeLock().lock();
        try {
            List<Long> toRemove = collection.values().stream()
                    .filter(p -> isOwner(p, ownerId))
                    .filter(p -> p.compareTo(product) < 0)
                    .map(Product::getId)
                    .collect(Collectors.toList());

            if (toRemove.isEmpty()) return 0;

            int deleted = repository.deleteByIds(toRemove, ownerId);
            toRemove.forEach(id -> collection.remove(id));

            Logger.info("удалено {} продуктов (меньше заданного)", deleted);
            return deleted;

        } finally {
            lock.writeLock().unlock();
        }
    }


    public boolean replaceIfGreater(Long key, Product newProduct, int ownerId) {
        lock.writeLock().lock();
        try {
            Product oldProduct = collection.get(key);
            if (oldProduct == null) {
                return false;
            }

            if (!isOwner(oldProduct, ownerId)) {
                Logger.warn("пользователь {} не является владельцем", ownerId);
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


    private boolean isOwner(Product product, int userId) {
        if (product == null) return false;

        Integer productOwnerId = product.getOwnerId();

        if (productOwnerId == null) {
            Logger.warn("продукт ID={} не имеет ownerId. Доступ разрешён.", product.getId());
            return true;
        }

        boolean isOwner = (productOwnerId == userId);
        Logger.info("isOwner: productId={}, productOwnerId={}, userId={}, result={}",
                product.getId(), productOwnerId, userId, isOwner);

        return isOwner;
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