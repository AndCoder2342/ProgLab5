
package manager;

import java.io.Serializable;
import java.util.Objects;

/**
 * класс представляющий организацию
 */
public class Organization implements Serializable {
    private Integer id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private String fullName; //Поле может быть null
    private Long annualTurnover; //Поле не может быть null, Значение поля должно быть больше 0
    private int employeesCount; //Значение поля должно быть больше 0

    /**
     * валидация и устанавка id
     */
    public void setId(Integer id) {
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
     * валидация и устанавка годовоого оборота
     */
    public void setAnnualTurnover(Long annualTurnover) {
        if (annualTurnover == null) {
            throw new IllegalArgumentException("Годовой оборот не может быть null");
        }
        if (annualTurnover <= 0) {
            throw new IllegalArgumentException("Годовой оборот должен быть больше 0");
        }
        this.annualTurnover = annualTurnover;
    }

    /**
     * валидация и устанавка кол-во сотрудников
     */
    public void setEmployeesCount(int employeesCount) {
        if (employeesCount <= 0) {
            throw new IllegalArgumentException("Количество сотрудников должно быть больше 0");
        }
        this.employeesCount = employeesCount;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getFullName() { return fullName; }
    public Long getAnnualTurnover() { return annualTurnover; }
    public int getEmployeesCount() { return employeesCount; }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organization that = (Organization) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Organization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", annualTurnover=" + annualTurnover +
                ", employeesCount=" + employeesCount +
                '}';
    }
}