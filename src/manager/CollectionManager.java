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
     * удаляет по ключу
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
     * очищает коллекцию
     */
    public void clear() {
        collection.clear();
        System.out.println("Очищено");
    }

    /**
     * информация о коллекции
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
     * удаляет продукты меньше заданного
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
     * фильтрует продукты по подстроке во ВСЕХ полях
     */
    public List<Product> filterContainsName(String substring) {
        return collection.values().stream()
                .filter(product -> containsInAnyField(product, substring))
                .collect(Collectors.toList());
    }

    /**
     * проверяет наличие подстроки в любом поле продукта
     */
    private boolean containsInAnyField(Product product, String substring) {
        if (substring == null || substring.isEmpty()) {
            return true;
        }

        String lowerSubstring = substring.toLowerCase();

        // id
        if (product.getId() != null && String.valueOf(product.getId()).contains(lowerSubstring)) {
            return true;
        }

        // name
        if (product.getName() != null && product.getName().toLowerCase().contains(lowerSubstring)) {
            return true;
        }

        // coordinates
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

        // price
        if (String.valueOf(product.getPrice()).contains(lowerSubstring)) {
            return true;
        }

        // creationDate
        if (product.getCreationDate() != null) {
            String dateString = product.getCreationDate().toString().toLowerCase();
            if (dateString.contains(lowerSubstring)) {
                return true;
            }
        }

        // unitOfMeasure
        if (product.getUnitOfMeasure() != null) {
            String unitString = product.getUnitOfMeasure().name().toLowerCase();
            if (unitString.contains(lowerSubstring)) {
                return true;
            }
        }

        // manufacturer (Organization)
        if (product.getManufacturer() != null) {
            Organization org = product.getManufacturer();

            // id организации
            if (org.getId() != null && String.valueOf(org.getId()).contains(lowerSubstring)) {
                return true;
            }

            // название организации
            if (org.getName() != null && org.getName().toLowerCase().contains(lowerSubstring)) {
                return true;
            }

            // полное название
            if (org.getFullName() != null && org.getFullName().toLowerCase().contains(lowerSubstring)) {
                return true;
            }

            // годовой оборот
            if (org.getAnnualTurnover() != null &&
                    String.valueOf(org.getAnnualTurnover()).contains(lowerSubstring)) {
                return true;
            }

            // количество сотрудников
            if (String.valueOf(org.getEmployeesCount()).contains(lowerSubstring)) {
                return true;
            }
        }

        return false;
    }

    public Hashtable<Long, Product> getCollection() {
        return collection;
    }
}