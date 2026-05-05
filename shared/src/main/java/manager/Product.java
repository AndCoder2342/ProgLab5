package manager;

import enums.UnitOfMeasure;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * класс представляющий продукт
 * реализует Comparable для сортировки по умолчанию (по id)
 */
public class Product implements Comparable<Product>, Serializable {
    private Long id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private Date creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private int price; //Значение поля должно быть больше 0
    private UnitOfMeasure unitOfMeasure; //Поле может быть null
    private Organization manufacturer; //Поле может быть null

    public Product() {
        this.creationDate = new Date();
    }

    /**
     * валидация и устанавка id
     */
    public void setId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("ID должно быть больше 0");
        }
        this.id = id;
    }

    /**
     * валидация и устанавка имени
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть null или пустым");
        }
        this.name = name.trim();
    }

    /**
     * валидация и устанавка координат
     */
    public void setCoordinates(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Координаты не могут быть null");
        }
        this.coordinates = coordinates;
    }

    /**
     * валидация и устанавка цены
     */
    public void setPrice(int price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Цена должна быть больше 0");
        }
        this.price = price;
    }

    // гетеры
    public Long getId() { return id; }
    public String getName() { return name; }
    public Coordinates getCoordinates() { return coordinates; }
    public Date getCreationDate() { return creationDate; }
    public int getPrice() { return price; }
    public UnitOfMeasure getUnitOfMeasure() { return unitOfMeasure; }
    public Organization getManufacturer() { return manufacturer; }

    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public void setManufacturer(Organization manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public int compareTo(Product other) {
        if (other == null) return 1;
        return this.id.compareTo(other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", price=" + price +
                ", unitOfMeasure=" + unitOfMeasure +
                ", manufacturer=" + (manufacturer != null ? manufacturer.getName() : "null") +
                '}';
    }
}