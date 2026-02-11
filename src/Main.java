import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/// help : вывести справку по доступным командам
/// info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)
/// show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении
/// insert null {element} : добавить новый элемент с заданным ключом
/// update id {element} : обновить значение элемента коллекции, id которого равен заданному
/// remove_key null : удалить элемент из коллекции по его ключу
/// clear : очистить коллекцию
/// save : сохранить коллекцию в файл
/// execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.
/// exit : завершить программу (без сохранения в файл)
/// remove_greater {element} : удалить из коллекции все элементы, превышающие заданный
/// remove_lower {element} : удалить из коллекции все элементы, меньшие, чем заданный
/// replace_if_greater null {element} : заменить значение по ключу, если новое значение больше старого
/// group_counting_by_manufacturer : сгруппировать элементы коллекции по значению поля manufacturer, вывести количество элементов в каждой группе
/// count_greater_than_manufacturer manufacturer : вывести количество элементов, значение поля manufacturer которых больше заданного
/// filter_contains_name name : вывести элементы, значение поля name которых содержит заданную подстроку

public class Main {
    private Map history = new HashMap();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Main main = new Main();

        while (true) {
            String input = sc.nextLine();
            if (input.equals("help")) {
                System.out.println("help info show");
            }

            main.history.put("help", new HelpCommand());

            if (input.equals("info")) {
                System.out.println("...");
            }
            if (input.equals("show")) {
                Command t = new HelpCommand();
                Object[] command = main.history.values().stream().toArray();

//                System.out.println(main.history.values().stream().toArray(String[]::new));
                for (int i = 0; i<command.length; i++) {
                    System.out.println(command[i]);
                }
            }
        }
    }
}