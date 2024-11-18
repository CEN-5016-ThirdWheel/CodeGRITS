package components;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import actions.StatusIconAction;

public class StatusIconFactory implements StatusBarWidgetFactory {
    @Override
    @NotNull
    public String getId() {
        return "com.codegrits.StatusIcon";
    }

    @Override
    @Nls
    @NotNull
    public String getDisplayName() {
        return "CodeGRITS Recording Status";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        StatusIcon icon = new StatusIcon(project);
        // Initialize the reference in StatusIconAction
        StatusIconAction.setStatusIcon(icon);
        return icon;
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}