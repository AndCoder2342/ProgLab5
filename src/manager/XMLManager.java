package manager;

import io.*;
import enums.UnitOfMeasure;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Hashtable;

/**
 * менеджер для работы с xml
 */
public class XMLManager {
    private String filepath;



    public XMLManager() {
        this.filepath = System.getenv("PRODUCTS_FILE_PATH");

        // Если переменная не установлена, используем путь по умолчанию
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
                System.out.println("Файл не найден. Создана новая коллекция.");
                return collection;
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
        } catch (Exception e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            e.printStackTrace();
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

            try (java.io.FileWriter writer = new java.io.FileWriter(filepath)) {
                java.io.StringWriter stringWriter = new java.io.StringWriter();
                StreamResult streamResult = new StreamResult(stringWriter);
                transformer.transform(source, streamResult);
                writer.write(stringWriter.toString());
            }

            System.out.println("Коллекция сохранена в файл: " + filepath);
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении файла: " + e.getMessage());
            e.printStackTrace();
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