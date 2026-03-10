//package commands;
//
//import java.util.Map;
//
//public class HelpCommand  implements Command {
//    private String name;
//    private Map commands;
//    public HelpCommand() {
//        this.name = name;
//        this.commands = commands;
//    }
//    @Override
//    public void execute() {}
//    @Override
//    public String toString() {
//        return "";
//    }
//}



package commands;

import java.util.Map;

/**
 * команда помощи
 */
public class HelpCommand implements Command {
    private final Map<String, Command> commands;

    public HelpCommand(Map<String, Command> commands) {
        this.commands = commands;
    }

    @Override
    public boolean execute() {
        System.out.println("Доступные команды:");
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue().getDescription());
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "вывести справку по доступным командам";
    }

    @Override
    public String getName() {
        return "help";
    }
}