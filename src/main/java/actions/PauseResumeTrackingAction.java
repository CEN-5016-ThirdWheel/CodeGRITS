package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import trackers.ScreenRecorder;

import java.io.IOException;

/**
 * Action for pausing or resuming tracking.
 * <p>
 * This class provides functionality to toggle the tracking state for the screen recorder 
 * and any other active tracking components in the application. It interacts with 
 * `ScreenRecorder` and `StartStopTrackingAction` to handle the pausing or resuming 
 * of tracking based on user input.
 * </p>
 * 
 * <h2>Class Role</h2>
 * <ul>
 * <li><b>Pause Tracking:</b> When tracking is active, pauses the screen recording and 
 * other tracking components by disabling associated UI actions.</li>
 * <li><b>Resume Tracking:</b> When tracking is paused, resumes the screen recording 
 * and other tracking components, re-enabling UI actions.</li>
 * </ul>
 * 
 * <h2>Dependencies</h2>
 * <ul>
 * <li>Relies on `StartStopTrackingAction` to check tracking state and handle overall tracking lifecycle.</li>
 * <li>Uses `ScreenRecorder` to manage the recording of screen activities, which can be paused and resumed.</li>
 * </ul>
 */
public class PauseResumeTrackingAction extends AnAction {
    private final ScreenRecorder screenRecorder = ScreenRecorder.getInstance();

    /**
     * Updates the text and state of the action button based on current tracking status.
     * <p>
     * This method enables the action button and sets the text to "Pause Tracking" if 
     * tracking is active, or "Resume Tracking" if tracking is paused. If tracking has not 
     * started, the button is disabled.
     * </p>
     *
     * @param e The action event, providing context for updating the button state.
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        if (StartStopTrackingAction.isTracking()) {
            e.getPresentation().setEnabled(true);
            if (StartStopTrackingAction.isPaused()) {
                e.getPresentation().setText("Resume Tracking");
            } else {
                e.getPresentation().setText("Pause Tracking");
            }
        } else {
            e.getPresentation().setText("Pause Tracking");
            e.getPresentation().setEnabled(false);
        }
    }

    /**
     * Toggles between pausing and resuming tracking when the action is performed.
     * <p>
     * If tracking is currently paused, this method resumes tracking by enabling 
     * associated UI actions and resuming screen recording. If tracking is active, it pauses 
     * tracking, disables certain UI actions, and pauses screen recording.
     * </p>
     * 
     * <h3>Exceptions</h3>
     * <ul>
     * <li>IOException: Thrown if an error occurs while pausing the screen recording.</li>
     * </ul>
     *
     * @param e The action event that triggered this toggle action.
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (StartStopTrackingAction.isPaused()) {
            screenRecorder.resumeRecording();
            StartStopTrackingAction.resumeTracking();
            AddLabelAction.setIsEnabled(true);
            ConfigAction.setIsEnabled(false);

        } else {
            StartStopTrackingAction.pauseTracking();
            ConfigAction.setIsEnabled(false);
            AddLabelAction.setIsEnabled(false);
            try {
                screenRecorder.pauseRecording();
            } catch (IOException ex) {
                throw new RuntimeException("Error pausing screen recording", ex);
            }
        }
    }
}