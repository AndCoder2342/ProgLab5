package shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

/**
 * Утилита для сериализации и десериализации объектов (Request/Response)
 * Используется и клиентом, и сервером.
 */
public class SerializationUtil {

    /**
     * Преобразует объект в массив байт для отправки по сети
     */
    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }

    /**
     * Преобразует массив байт обратно в объект
     */

    public static <T> T deserialize(byte[] data, Class<T> clazz) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return clazz.cast(ois.readObject());
        }
    }

    /**
     * Вспомогательный метод для отладки (вывод в Base64)
     */
    public static String toBase64(Object obj) {
        try {
            return Base64.getEncoder().encodeToString(serialize(obj));
        } catch (IOException e) {
            return "Ошибка сериализации";
        }
    }
}