import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RawText;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.awt.datatransfer.DataFlavor;

public class ContexMenuAction implements CopyPastePreProcessor {
    @Override
    public @Nullable String preprocessOnCopy(PsiFile file, int[] startOffsets, int[] endOffsets, String text) {
        return null;
    }

    @Override
    public @NotNull String preprocessOnPaste(Project project, PsiFile file, Editor editor, String text, RawText rawText) {
        var cpManager = CopyPasteManager.getInstance();
        var contents = cpManager.getContents();

        assert contents != null;

        var parser = new CssConverter();

        try {
            return parser.parse(contents.getTransferData(DataFlavor.stringFlavor).toString());
        } catch (Exception e) {
            return text;
        }
    }
}
