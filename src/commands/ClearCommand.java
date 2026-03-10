
package commands;

import manager.CollectionManager;

/**
 * команда очистки коллекции
 */
public class ClearCommand implements Command {
    private final CollectionManager manager;

    public ClearCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean execute() {
        manager.clear();
        return true;
    }

    @Override
    public String getDescription() {
        return "очистить коллекцию";
    }

    @Override
    public String getName() {
        return "clear";
    }
}