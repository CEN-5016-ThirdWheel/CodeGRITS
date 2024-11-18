package actions;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import components.StatusIcon;

public class StatusIconAction {
    private static final Logger LOG = Logger.getInstance(StatusIconAction.class);
    private static StatusIcon statusIcon;

    // holding the state of the icon
    public enum IconState {
        RECORDING,
        PAUSED,
        NOT_RECORDING
    }

    // initalize the state to not recording
    private static IconState currentState = IconState.NOT_RECORDING;


    public static void setStatusIcon(StatusIcon icon) {
        LOG.info("Setting status icon reference");
        statusIcon = icon;
        // Update the icon to current state when initialized
        if (statusIcon != null && currentState != null) {
            statusIcon.updateState(currentState);
        }
    }

    // function to update the state 
    public static void updateIconState(IconState state) {
        LOG.info("Updating icon state to: " + state);
        currentState = state;
        if (statusIcon != null) { // only update if not null
            statusIcon.updateState(state);
        } else {
            LOG.warn("Status icon is null when trying to update state");
        }
    }
}