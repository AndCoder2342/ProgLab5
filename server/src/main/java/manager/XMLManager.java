package manager;

import enums.UnitOfMeasure;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
/**
 * менеджер для работы с xml
 */
public class XMLManager {
    private String filepath;



    public XMLManager() {
        this.filepath = System.getenv("PRODUCTS_FILE_PATH");

        // если переменная не установлена, используем путь по умолчанию
        if (filepath == null || filepath.isEmpty()) {
            this.filepath = "products.xml";
            System.out.println("Предупреждение: PRODUCTS_FILE_PATH не установлен, используем: " + filepath);
        }
    }

    /**
     * читает коллекцию продуктов из XML файла
     * @return Hashtable с продуктами
     */
    public Hashtable<Long, Product> readCollection() {
        Hashtable<Long, Product> collection = new Hashtable<>();

        try {
            File file = new File(filepath);

            if (!file.exists()) {
                System.out.println("Файл не найден. Будет создан новый файл при первом сохранении.");
                return collection;
            }

            if (!file.canRead()) {
                System.err.println("ОШИБКА: Недостаточно прав для чтения файла: " + filepath);
                System.err.println("Проверьте права доступа к файлу (chmod) или запустите программу с соответствующими правами");
                throw new RuntimeException("Нет прав на чтение файла");
            }

            if (!file.isFile()) {
                System.err.println("ОШИБКА: Указанный путь не является файлом: " + filepath);
                throw new RuntimeException("Путь указывает не на файл");
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList productList = doc.getElementsByTagName("product");

            for (int i = 0; i < productList.getLength(); i++) {
                Element productElement = (Element) productList.item(i);
                Product product = parseProduct(productElement);
                if (product != null) {
                    collection.put(product.getId(), product);
                }
            }

            System.out.println("Загружено " + collection.size() + " продуктов.");

        } catch (java.nio.file.AccessDeniedException e) {
            System.err.println("ОШИБКА: Недостаточно прав для доступа к файлу: " + filepath);
            System.err.println("Причина: " + e.getMessage());
            System.err.println("Решение: Проверьте права доступа (chmod) или запустите с нужными правами.");
            throw new RuntimeException("Нет прав доступа к файлу");

        } catch (java.io.FileNotFoundException e) {
            System.err.println("ОШИБКА: Файл не найден: " + filepath);
            System.err.println("Решение: Проверьте правильность пути к файлу.");
            throw new RuntimeException("Файл не найден");

        } catch (javax.xml.parsers.ParserConfigurationException e) {
            System.err.println("ОШИБКА: Ошибка конфигурации XML парсера");
            System.err.println("Причина: " + e.getMessage());
            throw new RuntimeException("Ошибка XML парсера");

        } catch (org.xml.sax.SAXException e) {
            System.err.println("ОШИБКА: Ошибка разбора XML файла");
            System.err.println("Причина: " + e.getMessage());
            System.err.println("Решение: Проверьте корректность XML структуры в файле.");
            throw new RuntimeException("Ошибка разбора XML");

        } catch (java.io.IOException e) {
            System.err.println("ОШИБКА: Ошибка ввода-вывода при чтении файла");
            System.err.println("Причина: " + e.getMessage());
            System.err.println("Решение: Проверьте права доступа и целостность файла.");
            throw new RuntimeException("Ошибка чтения файла");
        }

        return collection;
    }

    private Product parseProduct(Element element) {
        try {
            Product product = new Product();
            product.setId(Long.parseLong(getElementValue(element, "id")));
            product.setName(getElementValue(element, "name"));
            product.setPrice(Integer.parseInt(getElementValue(element, "price")));
            product.setCreationDate(new java.util.Date(Long.parseLong(getElementValue(element, "creationDate"))));


            Element coordsElement = (Element) element.getElementsByTagName("coordinates").item(0);
            if (coordsElement != null) {
                Coordinates coords = new Coordinates();
                coords.setX(Integer.parseInt(getElementValue(coordsElement, "x")));
                coords.setY(Float.parseFloat(getElementValue(coordsElement, "y")));
                product.setCoordinates(coords);
            }


            String unitStr = getElementValue(element, "unitOfMeasure");
            if (unitStr != null && !unitStr.isEmpty() && !unitStr.equals("null")) {
                product.setUnitOfMeasure(UnitOfMeasure.valueOf(unitStr));
            }

            Element orgElement = (Element) element.getElementsByTagName("manufacturer").item(0);
            if (orgElement != null) {
                Organization org = parseOrganization(orgElement);
                product.setManufacturer(org);
            }

            return product;
        } catch (Exception e) {
            System.err.println("Ошибка при разборе продукта: " + e.getMessage());
            return null;
        }
    }

    private Organization parseOrganization(Element element) {
        try {
            Organization org = new Organization();
            org.setId(Integer.parseInt(getElementValue(element, "id")));
            org.setName(getElementValue(element, "name"));
            org.setFullName(getElementValue(element, "fullName"));
            org.setAnnualTurnover(Long.parseLong(getElementValue(element, "annualTurnover")));
            org.setEmployeesCount(Integer.parseInt(getElementValue(element, "employeesCount")));
            return org;
        } catch (Exception e) {
            System.err.println("Ошибка при разборе организации: " + e.getMessage());
            return null;
        }
    }

    private String getElementValue(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            return list.item(0).getTextContent();
        }
        return "";
    }

    /**
     * сохраняет коллекцию в xml файл
     * @param collection коллекция для сохранения
     */
    public void saveCollection(Hashtable<Long, Product> collection) {
        try {
            File file = new File(filepath);

            // проверка прав на запись в директорию
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                System.err.println("ОШИБКА: Директория не существует: " + parentDir.getAbsolutePath());
                System.err.println("Решение: Создайте директорию перед сохранением.");
                throw new RuntimeException("Директория не существует");
            }

            if (parentDir != null && !parentDir.canWrite()) {
                System.err.println("ОШИБКА: Недостаточно прав для записи в директорию: " + parentDir.getAbsolutePath());
                System.err.println("Решение: Проверьте права доступа к директории (chmod).");
                throw new RuntimeException("Нет прав на запись в директорию");
            }

            // если файл существует, проверяем права на запись в него
            if (file.exists() && !file.canWrite()) {
                System.err.println("ОШИБКА: Недостаточно прав для записи в файл: " + filepath);
                System.err.println("Решение: Проверьте права доступа к файлу (chmod +w) или запустите с нужными правами.");
                throw new RuntimeException("Нет прав на запись в файл");
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("products");
            doc.appendChild(root);

            for (Product product : collection.values()) {
                Element productElement = createProductElement(doc, product);
                root.appendChild(productElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);

            // используем FileWriter с явной кодировкой
            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(filepath),
                    java.nio.charset.StandardCharsets.UTF_8)) {

                StreamResult streamResult = new StreamResult(writer);
                transformer.transform(source, streamResult);
            }

            System.out.println("Коллекция успешно сохранена в файл: " + filepath);

        } catch (java.nio.file.AccessDeniedException e) {
            System.err.println("ОШИБКА: Недостаточно прав для записи в файл: " + filepath);
            System.err.println("Причина: " + e.getMessage());
            System.err.println("Решение: Проверьте права доступа (chmod +w) или запустите с нужными правами.");
            throw new RuntimeException("Нет прав на запись в файл");

        } catch (java.io.FileNotFoundException e) {
            System.err.println("ОШИБКА: Не удалось создать файл: " + filepath);
            System.err.println("Причина: " + e.getMessage());
            System.err.println("Решение: Проверьте права доступа к директории.");
            throw new RuntimeException("Не удалось создать файл");

        } catch (TransformerException e) {
            System.err.println("ОШИБКА: Ошибка при преобразовании XML");
            System.err.println("Причина: " + e.getMessage());
            throw new RuntimeException("Ошибка сохранения XML");

        } catch (java.io.IOException e) {
            System.err.println("ОШИБКА: Ошибка ввода-вывода при сохранении файла");
            System.err.println("Причина: " + e.getMessage());
            System.err.println("Решение: Проверьте права доступа и наличие свободного места.");
            throw new RuntimeException("Ошибка записи файла");
        } catch (ParserConfigurationException e) {

        }
    }

    private Element createProductElement(Document doc, Product product) {
        Element productElement = doc.createElement("product");

        productElement.appendChild(createElement(doc, "id", String.valueOf(product.getId())));
        productElement.appendChild(createElement(doc, "name", product.getName()));
        productElement.appendChild(createElement(doc, "price", String.valueOf(product.getPrice())));
        productElement.appendChild(createElement(doc, "creationDate", String.valueOf(product.getCreationDate().getTime())));


        if (product.getCoordinates() != null) {
            Element coordsElement = doc.createElement("coordinates");
            coordsElement.appendChild(createElement(doc, "x", String.valueOf(product.getCoordinates().getX())));
            coordsElement.appendChild(createElement(doc, "y", String.valueOf(product.getCoordinates().getY())));
            productElement.appendChild(coordsElement);
        }


        if (product.getUnitOfMeasure() != null) {
            productElement.appendChild(createElement(doc, "unitOfMeasure", product.getUnitOfMeasure().name()));
        } else {
            productElement.appendChild(createElement(doc, "unitOfMeasure", "null"));
        }


        if (product.getManufacturer() != null) {
            Element orgElement = createOrganizationElement(doc, product.getManufacturer());
            productElement.appendChild(orgElement);
        }

        return productElement;
    }

    private Element createOrganizationElement(Document doc, Organization org) {
        Element orgElement = doc.createElement("manufacturer");
        orgElement.appendChild(createElement(doc, "id", String.valueOf(org.getId())));
        orgElement.appendChild(createElement(doc, "name", org.getName()));
        orgElement.appendChild(createElement(doc, "fullName", org.getFullName() != null ? org.getFullName() : "null"));
        orgElement.appendChild(createElement(doc, "annualTurnover", String.valueOf(org.getAnnualTurnover())));
        orgElement.appendChild(createElement(doc, "employeesCount", String.valueOf(org.getEmployeesCount())));
        return orgElement;
    }

    private Element createElement(Document doc, String tag, String value) {
        Element element = doc.createElement(tag);
        element.setTextContent(value);
        return element;
    }
}