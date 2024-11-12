package actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import components.ConfigDialog;
import entity.Config;
import org.jetbrains.annotations.NotNull;
import trackers.EyeTracker;
import trackers.IDETracker;
import trackers.ScreenRecorder;
import utils.AvailabilityChecker;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Objects;

/**
 * Action for starting or stopping tracking for development-related data collection.
 * <p>
 * The StartStopTrackingAction class manages the lifecycle of IDETracker, EyeTracker, and ScreenRecorder components,
 * which are used to capture data for analysis during a developer's coding session. The class toggles tracking
 * based on user interaction, configuring and initializing necessary resources and managing their states.
 * </p>
 * 
 * <h2>Class Role</h2>
 * <ul>
 * <li><b>Starting Tracking:</b> Initializes and starts IDETracker, EyeTracker, and ScreenRecorder based on
 * configuration settings.</li>
 * <li><b>Stopping Tracking:</b> Stops these trackers, releases resources, and resets tracking state.</li>
 * <li><b>State Management:</b> Provides methods to pause and resume tracking, and updates the action button text
 * based on whether tracking is active.</li>
 * </ul>
 * 
 * <h2>Prerequisites</h2>
 * <ul>
 * <li>Configuration setup is required before tracking can start, ensuring paths and dependencies are defined.</li>
 * <li>If eye tracking is enabled, Python must be installed and configured, along with any necessary hardware and 
 * Python environment checks.</li>
 * </ul>
 */
public class StartStopTrackingAction extends AnAction {

    /**
     * Indicates whether tracking is currently active.
     */
    private static boolean isTracking = false;
   /**
     * Instance of the IDE tracker responsible for tracking IDE-related data.
     */
    private static IDETracker iDETracker;
    /**
     * Instance of the eye tracker used for tracking eye movement data.
     */
    private static EyeTracker eyeTracker;
    /**
     * Singleton instance of the screen recorder used to capture screen recordings.
     */
    private final ScreenRecorder screenRecorder = ScreenRecorder.getInstance();
    /**
     * Configuration object containing settings for tracking.
     */
    Config config = new Config();

    /**
     * Updates the text displayed on the action button based on tracking state.
     *
     * @param e The action event providing context for the update.
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(isTracking ? "Stop Tracking" : "Start Tracking");
    }

    /**
     * Toggles tracking when the action is performed, starting or stopping IDETracker, EyeTracker, and ScreenRecorder.
     * <p>
     * This method checks the configuration and the tracking state (isTracking) to determine the appropriate action:
     * <ul>
     * <li><b>Start Tracking:</b> Loads configuration, verifies the environment, and starts tracking components.
     * Updates the UI to reflect tracking state and sets paths for data output.</li>
     * <li><b>Stop Tracking:</b> Stops all tracking components, releases resources, and updates the UI accordingly.</li>
     * </ul>
     * </p>
     * <h3>Exceptions</h3>
     * <ul>
     * <li>ParserConfigurationException: Thrown if an error occurs in XML parser configuration.</li>
     * <li>TransformerException: Thrown if an error occurs in transforming data output.</li>
     * <li>IOException: Thrown if an error occurs in I/O operations, such as reading configuration files.</li>
     * <li>InterruptedException: Thrown if the thread managing trackers is interrupted.</li>
     * </ul>
     *
     * @param e The action event that triggered this action.
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (config.configExists()) {
            config.loadFromJson();
        } else {
            Notification notification = new Notification("CodeGRITS Notification Group", "Configuration",
                    "Please configure the plugin first.", NotificationType.WARNING);
            notification.notify(e.getProject());
            return;
        }
        try {
            if (!isTracking) {
                if (config.getCheckBoxes().get(1)) {
                    if (!AvailabilityChecker.checkPythonEnvironment(config.getPythonInterpreter())) {
                        JOptionPane.showMessageDialog(null, "Python interpreter not found. Please configure the plugin first.");
                        return;
                    }
                    if (config.getEyeTrackerDevice() != 0 && !AvailabilityChecker.checkEyeTracker(config.getPythonInterpreter())) {
                        JOptionPane.showMessageDialog(null, "Eye tracker not found. Please configure the mouse simulation first.");
                        return;
                    }
                }

                isTracking = true;
                ConfigAction.setIsEnabled(false);
                AddLabelActionGroup.setIsEnabled(true);
                String projectPath = e.getProject() != null ? e.getProject().getBasePath() : "";
                String realDataOutputPath = Objects.equals(config.getDataOutputPath(), ConfigDialog.selectDataOutputPlaceHolder)
                        ? projectPath : config.getDataOutputPath();
                realDataOutputPath += "/" + System.currentTimeMillis() + "/";

                if (config.getCheckBoxes().get(2)) {
                    screenRecorder.setDataOutputPath(realDataOutputPath);
                    screenRecorder.startRecording();
                }

                iDETracker = IDETracker.getInstance();
                iDETracker.setProjectPath(projectPath);
                iDETracker.setDataOutputPath(realDataOutputPath);
                iDETracker.startTracking(e.getProject());

                if (config.getCheckBoxes().get(1)) {
                    eyeTracker = new EyeTracker();
                    eyeTracker.setProjectPath(projectPath);
                    eyeTracker.setDataOutputPath(realDataOutputPath);
                    eyeTracker.setPythonInterpreter(config.getPythonInterpreter());
                    eyeTracker.setSampleFrequency(config.getSampleFreq());
                    eyeTracker.setDeviceIndex(config.getEyeTrackerDevice());
                    eyeTracker.setPythonScriptTobii();
                    eyeTracker.setPythonScriptMouse();
                    eyeTracker.startTracking(e.getProject());
                }
                AddLabelAction.setIsEnabled(true);

            } else {
                isTracking = false;
                iDETracker.stopTracking();
                AddLabelAction.setIsEnabled(false);
                ConfigAction.setIsEnabled(true);
                if (config.getCheckBoxes().get(1) && eyeTracker != null) {
                    eyeTracker.stopTracking();
                }
                if (config.getCheckBoxes().get(2)) {
                    screenRecorder.stopRecording();
                }
                eyeTracker = null;
            }
        } catch (ParserConfigurationException | TransformerException | IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

        /**
     * Checks if tracking is currently active.
     *
     * @return True if tracking is active, false otherwise.
     */
    public static boolean isTracking() {
        return isTracking;
    }

    /**
     * Checks if tracking is currently paused. 
     * <p>
     * This method evaluates the state of the IDE tracker, which reflects whether tracking 
     * activities are temporarily halted but not completely stopped.
     * </p>
     *
     * @return True if tracking is paused, false otherwise.
     */
    public static boolean isPaused() {
        return !iDETracker.isTracking();
    }

    /**
     * Pauses tracking for the IDE and eye trackers if they are currently active.
     * <p>
     * This method is intended for situations where tracking should be temporarily halted,
     * but can be resumed later without reinitializing resources. It affects both the IDE
     * tracker and, if available, the eye tracker.
     * </p>
     */
    public static void pauseTracking() {
        iDETracker.pauseTracking();
        if (eyeTracker != null) {
            eyeTracker.pauseTracking();
        }
    }

    /**
     * Resumes tracking for the IDE and eye trackers if they were paused.
     * <p>
     * This method is used to continue tracking from a paused state, restoring both
     * the IDE and eye trackers to active tracking mode without requiring a full start.
     * </p>
     */
    public static void resumeTracking() {
        iDETracker.resumeTracking();
        if (eyeTracker != null) {
            eyeTracker.resumeTracking();
        }
    }

}