//package manager;
//
//public class CollectionManager {
//
//    public void addItem(String input) {
//    }
//}



package manager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * менеджер коллекции
 * использует Hashtable
 */
public class CollectionManager {
    private Hashtable<Long, Product> collection;
    private Date initializationDate;
    private XMLManager xmlManager;
    private long nextId = 1;

    public CollectionManager() {
        this.collection = new Hashtable<>();
        this.initializationDate = new Date();
        this.xmlManager = new XMLManager();
    }

    /**
     * инициализирует коллекцию из файла
     */
    public void initialize() {
        collection = xmlManager.readCollection();
        if (!collection.isEmpty()) {
            nextId = collection.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
        }
    }

    /**
     * сохраняет в файл
     */
    public void save() {
        xmlManager.saveCollection(collection);
    }

    /**
     * добавляет элемент
     */
    public void insert(Product product) {
        product.setId(nextId++);
        product.setCreationDate(new Date());
        collection.put(product.getId(), product);
        System.out.println("добавлен " + product.getId());
    }

    /**
     * обновляет продукт по id
     */
    public boolean update(Long id, Product newProduct) {
        if (collection.containsKey(id)) {
            newProduct.setId(id);
            newProduct.setCreationDate(collection.get(id).getCreationDate());
            collection.put(id, newProduct);
            System.out.println(id + " обновлен");
            return true;
        }
        System.out.println(id + " не найден");
        return false;
    }

    /**
     * удаляет ключу
     */
    public boolean removeKey(Long key) {
        if (collection.remove(key) != null) {
            System.out.println(key + " удален");
            return true;
        }
        System.out.println(key + " не найден");
        return false;
    }

    /**
     * очищает кололекцию
     */
    public void clear() {
        collection.clear();
        System.out.println("Очищено");
    }

    /**
     * информация о коллекции.
     */
    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Тип коллекции: ").append(collection.getClass().getName()).append("\n");
        sb.append("Дата инициализации: ").append(initializationDate).append("\n");
        sb.append("Количество элементов: ").append(collection.size()).append("\n");
        return sb.toString();
    }

    /**
     * возвращает всё
     */
    public Collection<Product> getAll() {
        return new ArrayList<>(collection.values());
    }

    /**
     * удаляет продукты превышающие заданный
     */
    public int removeGreater(Product product) {
        List<Long> toRemove = collection.values().stream()
                .filter(p -> p.compareTo(product) > 0)
                .map(Product::getId)
                .collect(Collectors.toList());

        toRemove.forEach(id -> collection.remove(id));
        System.out.println("Удалено элементов: " + toRemove.size());
        return toRemove.size();
    }

    /**
     * удаляет продукты меньшие заданного
     */
    public int removeLower(Product product) {
        List<Long> toRemove = collection.values().stream()
                .filter(p -> p.compareTo(product) < 0)
                .map(Product::getId)
                .collect(Collectors.toList());

        toRemove.forEach(id -> collection.remove(id));
        System.out.println("Удалено элементов: " + toRemove.size());
        return toRemove.size();
    }

    /**
     * заменяет продукт если новый больше старого
     */
    public boolean replaceIfGreater(Long key, Product newProduct) {
        Product oldProduct = collection.get(key);
        if (oldProduct != null && newProduct.compareTo(oldProduct) > 0) {
            newProduct.setId(key);
            newProduct.setCreationDate(oldProduct.getCreationDate());
            collection.put(key, newProduct);
            System.out.println("Продукт заменен");
            return true;
        }
        System.out.println("новый продукт не больше старого");
        return false;
    }

    /**
     * группирует по производителю
     */
    public Map<String, Long> groupCountingByManufacturer() {
        return collection.values().stream()
                .collect(Collectors.groupingBy(
                        p -> p.getManufacturer() != null ? p.getManufacturer().getName() : "null",
                        Collectors.counting()
                ));
    }

    /**
     * считает продукты с производителем больше заданного
     */
    public long countGreaterThanManufacturer(Organization manufacturer) {
        return collection.values().stream()
                .filter(p -> p.getManufacturer() != null)
                .filter(p -> p.getManufacturer().getName().compareTo(manufacturer.getName()) > 0)
                .count();
    }

    /**
     * фильтрует
     */
    public List<Product> filterContainsName(String name) {
        return collection.values().stream()
                .filter(p -> p.getName().contains(name))
                .collect(Collectors.toList());
    }

    public Hashtable<Long, Product> getCollection() {
        return collection;
    }
}