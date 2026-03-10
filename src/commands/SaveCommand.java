
package commands;

import manager.CollectionManager;

/**
 * команда сохранения коллекции в файл.
 */
public class SaveCommand implements Command {
    private final CollectionManager manager;

    public SaveCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean execute() {
        manager.save();
        return true;
    }

    @Override
    public String getDescription() {
        return "сохранить коллекцию в файл";
    }

    @Override
    public String getName() {
        return "save";
    }
}