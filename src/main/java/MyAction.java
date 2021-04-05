import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class MyAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // Using the event, create and show a dialog
        var currentProject = event.getProject();
        var dlgMsg = new StringBuilder(event.getPresentation().getText() + " Selected!");
        var dlgTitle = event.getPresentation().getDescription();

        // If an element is selected in the editor, add info about it.
        var nav = event.getData(CommonDataKeys.NAVIGATABLE);

        if (nav != null) {
            dlgMsg.append(String.format("\nSelected Element: %s", nav.toString()));
        }

        Messages.showMessageDialog(currentProject, dlgMsg.toString(), dlgTitle, Messages.getInformationIcon());
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
