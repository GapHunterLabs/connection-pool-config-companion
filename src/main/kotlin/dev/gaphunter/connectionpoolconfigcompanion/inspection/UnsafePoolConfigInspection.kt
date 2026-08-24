package dev.gaphunter.connectionpoolconfigcompanion.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.gaphunter.connectionpoolconfigcompanion.detect.HikariConfigScanner
import dev.gaphunter.connectionpoolconfigcompanion.model.PoolHit
import dev.gaphunter.connectionpoolconfigcompanion.model.PoolProblem
import dev.gaphunter.connectionpoolconfigcompanion.review.ReviewPrompt

/**
 * Flags a HikariCP connection-pool setting outside its own documented
 * safe range in a `.properties`/`.yml`/`.yaml` config file. Runs via
 * [checkFile] (whole-file text scan), same reasoning as
 * `config-secrets-file-companion`'s `HardcodedConfigSecretInspection`.
 */
class UnsafePoolConfigInspection : LocalInspectionTool() {

    companion object {
        const val MAX_FILE_LENGTH = 500_000
        private val CONFIG_FILE_NAME = Regex("""^[^.]+\.(properties|ya?ml)$""", RegexOption.IGNORE_CASE)
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val virtualFile = file.virtualFile ?: return null
        if (!CONFIG_FILE_NAME.matches(virtualFile.name)) return null

        val text = file.text
        if (text.length > MAX_FILE_LENGTH) return null

        val hits = HikariConfigScanner.scan(text)
        if (hits.isEmpty()) return null

        val document = file.viewProvider.document ?: return null
        val problems = mutableListOf<ProblemDescriptor>()

        for (hit in hits) {
            if (hit.lineNumber - 1 !in 0 until document.lineCount) continue
            val lineStartOffset = document.getLineStartOffset(hit.lineNumber - 1)
            val lineEndOffset = document.getLineEndOffset(hit.lineNumber - 1)
            val anchor = leafElementAt(file, lineStartOffset) ?: continue
            val anchorStart = anchor.textRange.startOffset
            val relativeRange = TextRange(
                (lineStartOffset - anchorStart).coerceAtLeast(0),
                (lineEndOffset - anchorStart).coerceAtMost(anchor.textLength),
            )
            if (relativeRange.startOffset >= relativeRange.endOffset) continue

            problems += manager.createProblemDescriptor(
                anchor,
                relativeRange,
                messageFor(hit),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
            )

            ReviewPrompt.recordHit(file.project, "${virtualFile.path}:${hit.lineNumber}:${hit.problem}")
        }

        return if (problems.isEmpty()) null else problems.toTypedArray()
    }

    private fun messageFor(hit: PoolHit): String = when (hit.problem) {
        PoolProblem.CONNECTION_TIMEOUT_TOO_LOW ->
            "${hit.key}=${hit.value} is below HikariCP's documented minimum of 250ms"
        PoolProblem.MAX_LIFETIME_TOO_LOW ->
            "${hit.key}=${hit.value} is below HikariCP's documented minimum of 30000ms (30s)"
        PoolProblem.IDLE_TIMEOUT_TOO_LOW ->
            "${hit.key}=${hit.value} is below HikariCP's documented minimum of 10000ms (10s)"
        PoolProblem.IDLE_TIMEOUT_NOT_LESS_THAN_MAX_LIFETIME ->
            "idleTimeout=${hit.value} is not less than maxLifetime -- HikariCP silently ignores idleTimeout in this case"
    }

    private fun leafElementAt(file: PsiFile, startOffset: Int): PsiElement? {
        if (startOffset < 0 || startOffset >= file.textLength) return null
        var element = file.findElementAt(startOffset) ?: return file
        while (element.firstChild != null) {
            element = element.firstChild
        }
        return element
    }
}
