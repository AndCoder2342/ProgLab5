
package commands;

import manager.CollectionManager;

/**
 * команда вывода информации о коллекции.
 */
public class InfoCommand implements Command {
    private final CollectionManager manager;

    public InfoCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean execute() {
        System.out.println(manager.getInfo());
        return true;
    }

    @Override
    public String getDescription() {
        return "вывести в стандартный поток вывода информацию о коллекции";
    }

    @Override
    public String getName() {
        return "info";
    }
}