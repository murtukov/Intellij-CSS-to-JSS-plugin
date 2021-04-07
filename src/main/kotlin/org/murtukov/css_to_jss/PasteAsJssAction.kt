package com.murtukov.css_to_jss;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class PasteAsJssAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var cpManager = CopyPasteManager.getInstance();
        var contents = cpManager.getContents();
        var project = event.getProject();
        var editor = event.getData(CommonDataKeys.EDITOR);
        var psiFile = event.getData(CommonDataKeys.PSI_FILE);
        var document = editor.getDocument();
        var primaryCaret = editor.getCaretModel().getPrimaryCaret();
        var csManager = CodeStyleManager.getInstance(project);

        var psiDocManager = PsiDocumentManager.getInstance(project);

        // My custom converter
        CssConverter converter = new CssConverter();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            var start = primaryCaret.getSelectionStart();
            var end = primaryCaret.getSelectionEnd();

            try {
                final var string = converter.parse(contents.getTransferData(DataFlavor.stringFlavor).toString());
                document.replaceString(start, end, string);
                psiDocManager.commitDocument(document);
                csManager.reformatText(psiFile, start, start + string.length());
            } catch (Exception ignored) {}
        });


//        primaryCaret.removeSelection();
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
