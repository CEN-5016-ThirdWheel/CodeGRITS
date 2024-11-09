package components;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import actions.StatusIconAction.IconState;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

public class StatusIcon implements StatusBarWidget, StatusBarWidget.IconPresentation {
    private static final Logger LOG = Logger.getInstance(StatusIcon.class);
    private final Project project;
    private IconState currentState = IconState.NOT_RECORDING;
    private boolean isVisible = true;
    private Timer flashTimer;

    // Load the icon
    private static final Icon RECORDING_ICON = IconLoader.getIcon("/recording-icon-tiny.svg", StatusIcon.class);

    public StatusIcon(Project project) {
        this.project = project;
        LOG.debug("StatusIcon initialized for project: " + project.getName());
    }

    @NotNull
    @Override
    public String ID() {
        return "com.codegrits.StatusIcon";
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        LOG.info("Installing StatusIcon widget");
    }

    @Override
    public void dispose() {
        if (flashTimer != null) {
            flashTimer.cancel();
        }
    }

    @Nullable
    @Override
    public Icon getIcon() {
        if (currentState == IconState.RECORDING && isVisible) {
            return RECORDING_ICON;
        }
        return null;
    }

    @Nullable
    @Override
    public String getTooltipText() {
        return currentState == IconState.RECORDING ?
                "CodeGRITS: Recording" : "CodeGRITS: Not Recording";
    }

    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    public void updateState(IconState state) {
        LOG.info("Updating icon state from " + currentState + " to " + state);
        this.currentState = state;

        if (flashTimer != null) {
            flashTimer.cancel();
        }

        if (state == IconState.RECORDING) {
            startFlashing();
        } else {
            isVisible = true;
        }

        updateWidget();
    }

    private void startFlashing() {
        flashTimer = new Timer(true);
        flashTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                isVisible = !isVisible;
                updateWidget();
            }
        }, 0, 500); // Flash every 500ms
    }

    private void updateWidget() {
        if (project != null && !project.isDisposed()) {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            if (statusBar != null) {
                LOG.debug("Updating widget in status bar");
                statusBar.updateWidget(ID());
            } else {
                LOG.warn("Status bar is null when trying to update widget");
            }
        } else {
            LOG.warn("Project is null or disposed when trying to update widget");
        }
    }

    public static StatusIcon createAndAdd(@NotNull Project project) {
        LOG.info("Creating and adding StatusIcon for project: " + project.getName());
        StatusIcon widget = new StatusIcon(project);
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            StatusBarWidget existingWidget = statusBar.getWidget(widget.ID());
            if (existingWidget != null) {
                LOG.info("Found existing widget, returning it");
                return (StatusIcon) existingWidget;
            }
            LOG.info("Adding new widget to status bar");
            statusBar.addWidget(widget);
        } else {
            LOG.warn("Status bar is null when trying to create widget");
        }
        return widget;
    }
}