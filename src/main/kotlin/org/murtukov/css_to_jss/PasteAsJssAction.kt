package org.murtukov.css_to_jss

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.openapi.command.WriteCommandAction
import java.awt.datatransfer.DataFlavor
import java.lang.Exception

class PasteAsJssAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val cpManager     = CopyPasteManager.getInstance()
        val contents      = cpManager.contents
        val project       = event.project
        val editor        = event.getData(CommonDataKeys.EDITOR)
        val psiFile       = event.getData(CommonDataKeys.PSI_FILE)
        val document      = editor!!.document
        val primaryCaret  = editor.caretModel.primaryCaret
        val csManager     = CodeStyleManager.getInstance(project!!)
        val psiDocManager = PsiDocumentManager.getInstance(project)
        val converter     = CssConverter()

        WriteCommandAction.runWriteCommandAction(project) {
            val start = primaryCaret.selectionStart
            val end = primaryCaret.selectionEnd
            try {
                val string = converter.convert(contents!!.getTransferData(DataFlavor.stringFlavor).toString())
                document.replaceString(start, end, string)
                psiDocManager.commitDocument(document)
                csManager.reformatText(psiFile!!, start, start + string.length)
            } catch (ignored: Exception) {}
        }
    }

    override fun isDumbAware(): Boolean {
        return true
    }
}