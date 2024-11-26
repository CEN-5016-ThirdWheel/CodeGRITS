package actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Action for adding labels in the application.
 * <p>
 * The `AddLabelAction` class provides functionality for adding descriptive labels to
 * various components or parts of the application. It dynamically updates its button
 * text and enabled state, and shows a notification to confirm that a label has been
 * successfully added.
 * </p>
 * 
 * <h2>Class Role</h2>
 * <ul>
 * <li><b>Dynamic Text Update:</b> Updates the action button's text to reflect the label being added.</li>
 * <li><b>Notification:</b> Displays a success notification when the label is added.</li>
 * <li><b>State Management:</b> Allows the button to be enabled or disabled based on the application's state.</li>
 * </ul>
 * 
 * <h2>Dependencies</h2>
 * <ul>
 * <li>Relies on IntelliJ's `Notification` API to display success messages.</li>
 * </ul>
 */
public class AddLabelAction extends AnAction {

    /**
     * Description of the label to be added. This value is reflected in the button text
     * and success notification.
     */
    private String description;

    /**
     * Indicates whether this action is currently enabled.
     * <p>
     * The enabled state determines if the action button can be clicked by the user.
     * This is managed externally through the `setIsEnabled` method.
     * </p>
     */
    private static boolean isEnabled = false;

    /**
     * Updates the button text and state based on the current configuration.
     * <p>
     * This method dynamically sets the button's text to the value of the `description`
     * property and enables/disables the button based on the `isEnabled` flag.
     * </p>
     *
     * @param e The action event providing context for the update.
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(description);
        e.getPresentation().setEnabled(isEnabled);
    }

    /**
     * Executes the action for adding a label.
     * <p>
     * When triggered, this method displays a notification to confirm that the label
     * specified by the `description` property has been successfully added.
     * </p>
     *
     * @param e The action event that triggered this action.
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Notification notification = new Notification("CodeGRITS Notification Group", "Add label",
                "Successfully add label \"" + description + "\"!", NotificationType.INFORMATION);
        notification.notify(e.getProject());
    }

    /**
     * Sets the description of the label to be added.
     * <p>
     * This description will appear as the action button's text and in the success
     * notification when the label is added.
     * </p>
     *
     * @param description A string representing the label's description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets whether this action is enabled.
     * <p>
     * When enabled, the action button can be clicked to add a label. This state can
     * be controlled dynamically based on application requirements.
     * </p>
     *
     * @param isEnabled A boolean indicating whether the action is enabled.
     */
    public static void setIsEnabled(boolean isEnabled) {
        AddLabelAction.isEnabled = isEnabled;
    }

    /**
     * Retrieves the template text for the action.
     * <p>
     * The template text corresponds to the `description` property, which defines the
     * label text used for the action button and success notification.
     * </p>
     *
     * @return The description text associated with this action.
     */
    @Override
    public @NotNull String getTemplateText() {
        return description;
    }
}