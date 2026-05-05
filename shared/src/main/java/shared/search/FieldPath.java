package shared.search;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FieldPath {
    private final List<String> path; // ["manufacturer", "name"]

    public FieldPath(String... fields) {
        this.path = Arrays.asList(fields);
    }

    public FieldPath(String dotNotation) {
        this.path = Arrays.asList(dotNotation.split("\\."));
    }

    /**
     * Рекурсивно извлекает значение поля любой вложенности
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(Object obj) throws ReflectiveOperationException {
        Object current = obj;

        for (String fieldName : path) {
            if (current == null) return null;

            Field field = findField(current.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldException(
                        "Поле '" + fieldName + "' не найдено в " + current.getClass());
            }

            field.setAccessible(true);
            current = field.get(current);
        }

        return (T) current;
    }

    private Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Проверяет, содержит ли значение подстроку (регистронезависимо)
     */
    public boolean containsIgnoreCase(Object obj, String substring) {
        try {
            Object value = getValue(obj);
            if (value == null || substring == null) return false;

            String strValue = value.toString().toLowerCase();
            return strValue.contains(substring.toLowerCase());
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}