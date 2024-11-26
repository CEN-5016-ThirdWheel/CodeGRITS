package actions;

import com.intellij.openapi.actionSystem.*;
import entity.Config;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Action group for dynamically adding label actions.
 * <p>
 * The `AddLabelActionGroup` class manages a group of label-adding actions based on the 
 * application's configuration. It dynamically loads labels from the configuration file 
 * and registers corresponding {@link AddLabelAction} instances into the group.
 * </p>
 * 
 * <h2>Class Role</h2>
 * <ul>
 * <li><b>Dynamic Action Group:</b> Creates and registers label-adding actions at runtime based on configuration data.</li>
 * <li><b>State Management:</b> Maintains an enabled/disabled state for the group, allowing control over its availability.</li>
 * <li><b>Configuration Integration:</b> Integrates with the {@link Config} entity to load label definitions.</li>
 * </ul>
 */
public class AddLabelActionGroup extends DefaultActionGroup {

    /**
     * Indicates whether this action group is enabled. This state determines if the group 
     * and its associated actions are available to the user.
     */
    private static boolean isEnabled = true;

    /**
     * Tracks whether default labels have been loaded from the configuration file.
     * <p>
     * This flag ensures that the labels are only loaded once during the application's lifecycle.
     * </p>
     */
    private boolean defaultLabelsLoaded = false;

    /**
     * Updates the action group by dynamically loading and registering label actions.
     * <p>
     * This method is called to refresh the action group. If the configuration file exists and
     * labels have not already been loaded, it will:
     * <ol>
     * <li>Load label definitions from the configuration file.</li>
     * <li>Clear any existing actions in the group.</li>
     * <li>Register new {@link AddLabelAction} instances for each label.</li>
     * </ol>
     * </p>
     *
     * @param e The action event providing context for the update.
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        Config config = new Config();
        if (!defaultLabelsLoaded && config.configExists()) {
            config.loadFromJson();
            ActionManager actionManager = ActionManager.getInstance();
            DefaultActionGroup actionGroup = (DefaultActionGroup) actionManager.getAction("CodeGRITS.AddLabelActionGroup");
            actionGroup.removeAll(); // Clear existing actions to avoid duplicates.
            List<String> labels = config.getLabels(); // Retrieve label definitions from configuration.
            for (String label : labels) {
                AddLabelAction addLabelAction = new AddLabelAction();
                addLabelAction.setDescription(label);
                // Register the action with a unique identifier.
                actionManager.registerAction("CodeGRITS.AddLabel.[" + label + "]", addLabelAction);
                actionGroup.add(addLabelAction); // Add the action to the group.
            }
            defaultLabelsLoaded = true; // Mark labels as loaded to prevent reloading.
        }
    }

    /**
     * Sets whether this action group is enabled.
     * <p>
     * The enabled state determines if the group and its actions are available to the user.
     * This can be controlled dynamically based on application requirements.
     * </p>
     *
     * @param isEnabled A boolean indicating whether the action group should be enabled.
     */
    public static void setIsEnabled(boolean isEnabled) {
        AddLabelActionGroup.isEnabled = isEnabled;
    }
}