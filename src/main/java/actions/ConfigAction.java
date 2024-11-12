package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import components.ConfigDialog;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Action for configuring the application settings.
 * <p>
 * The `ConfigAction` class provides an interface for users to open and interact with the
 * configuration dialog, allowing them to set up necessary settings for the application.
 * This action can be enabled or disabled based on application state.
 * </p>
 * 
 * <h2>Class Role</h2>
 * <ul>
 * <li><b>Configuration Dialog:</b> Opens the configuration dialog where users can adjust application settings.</li>
 * <li><b>State Management:</b> Allows control over whether this action is enabled, so it can be activated or
 * deactivated by other parts of the application.</li>
 * </ul>
 */
public class ConfigAction extends AnAction {

    /**
     * Indicates whether the configuration action is enabled. This can be toggled to allow
     * or restrict access to configuration based on the current state of the application.
     */
    private static boolean isEnabled = true;

    /**
     * Updates the action button's enabled state based on the `isEnabled` variable.
     * <p>
     * This method is called automatically by the framework to refresh the state of the action button.
     * If `isEnabled` is set to `false`, the button will be disabled.
     * </p>
     *
     * @param e The action event providing context for updating the button state.
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(isEnabled);
    }

    /**
     * Displays the configuration dialog when the action is performed.
     * <p>
     * This method initializes the `ConfigDialog` for the current project. If an exception 
     * occurs during initialization (such as I/O or interruption), it will be wrapped 
     * in a `RuntimeException`.
     * </p>
     *
     * @param e The action event that triggered this configuration action.
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        ConfigDialog configDialog;
        try {
            configDialog = new ConfigDialog(project);
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException("Error initializing configuration dialog", ex);
        }
        configDialog.show();
    }

    /**
     * Sets whether the configuration action is enabled.
     * <p>
     * When enabled, the action can be triggered by the user to open the configuration dialog.
     * Other parts of the application may toggle this setting based on the application's state.
     * </p>
     *
     * @param isEnabled A boolean indicating if the configuration action should be enabled.
     */
    public static void setIsEnabled(boolean isEnabled) {
        ConfigAction.isEnabled = isEnabled;
    }
}