package com.murtukov.css_to_jss;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;

public class PasteAsJssAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var cpManager = CopyPasteManager.getInstance();
        var contents = cpManager.getContents();
        var project = event.getProject();
        var editor = event.getData(CommonDataKeys.EDITOR);
        var document = editor.getDocument();
        var primaryCaret = editor.getCaretModel().getPrimaryCaret();

        assert contents != null;

        var parser = new CssConverter();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                document.replaceString(
                    primaryCaret.getSelectionStart(),
                    primaryCaret.getSelectionEnd(),
                    parser.parse(contents.getTransferData(DataFlavor.stringFlavor).toString())
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        primaryCaret.removeSelection();
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
