//package manager;
//
//public class Coordinates {
//    private Integer x; //Максимальное значение поля: 875, Поле не может быть null
//    private Float y; //Поле не может быть null
//}




package manager;

import java.io.Serializable;

/**
 * класс представляющий координаты
 */
public class Coordinates implements Serializable {
    private Integer x; //Максимальное значение поля: 875, Поле не может быть null
    private Float y; //Поле не может быть null

    /**
     * валидация с установкой х
     */
    public void setX(Integer x) {
        if (x == null) {
            throw new IllegalArgumentException("X не может быть null");
        }
        if (x > 875) {
            throw new IllegalArgumentException("X не может быть больше 875");
        }
        this.x = x;
    }

    /**
     * валидация с установкой y
     */
    public void setY(Float y) {
        if (y == null) {
            throw new IllegalArgumentException("Y не может быть null");
        }
        this.y = y;
    }

    public Integer getX() { return x; }
    public Float getY() { return y; }

    @Override
    public String toString() {
        return "{" + x + ", " + y + "}";
    }
}