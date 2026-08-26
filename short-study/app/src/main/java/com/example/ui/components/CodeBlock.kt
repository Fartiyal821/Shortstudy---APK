package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeComment
import com.example.ui.theme.CodeEditorBg
import com.example.ui.theme.CodeEditorBorder
import com.example.ui.theme.CodeFunction
import com.example.ui.theme.CodeKeyword
import com.example.ui.theme.CodeNumber
import com.example.ui.theme.CodePunctuation
import com.example.ui.theme.CodeString
import com.example.ui.theme.CodeType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CodeBlock(
    code: String,
    language: String = "python",
    showLineNumbers: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    val cleanCode = remember(code) {
        code.trimIndent().replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")
    }

    val lines = remember(cleanCode) {
        cleanCode.lines()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CodeEditorBorder, RoundedCornerShape(12.dp)),
        color = CodeEditorBg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar with Window Dots + Language Badge + Copy Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161B22))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Window indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF27C93F)))

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = language.uppercase(),
                        color = Color(0xFF8B949E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Copy Code Button
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Short-Study Code", cleanCode)
                        clipboard.setPrimaryClip(clip)
                        isCopied = true
                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("copy_code_button")
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = if (isCopied) Color(0xFF34D399) else Color(0xFF8B949E),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Code Content with Line Numbers & Scrollable Canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                if (showLineNumbers) {
                    // Line numbers column
                    Column(
                        modifier = Modifier.padding(end = 16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        lines.indices.forEach { index ->
                            Text(
                                text = "${index + 1}",
                                color = Color(0xFF484F58),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Highlighted Code lines
                Column {
                    lines.forEach { line ->
                        Text(
                            text = highlightSyntax(line, language),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Syntax token highlighter for Python, C, and Web Dev snippets.
 */
fun highlightSyntax(line: String, language: String): AnnotatedString {
    val lang = language.lowercase()
    val builder = buildAnnotatedString {
        append(line)

        // Comments
        val commentPrefix = when {
            lang.contains("python") -> "#"
            lang.contains("c") || lang.contains("js") || lang.contains("css") -> "//"
            else -> "#"
        }
        val commentIdx = line.indexOf(commentPrefix)
        if (commentIdx != -1) {
            addStyle(SpanStyle(color = CodeComment), commentIdx, line.length)
            return@buildAnnotatedString
        }

        // Keywords
        val keywords = when {
            lang.contains("python") -> listOf("def", "return", "import", "from", "class", "if", "elif", "else", "for", "in", "while", "try", "except", "lambda", "with", "as", "pass", "True", "False", "None", "async", "await")
            lang.contains("c") -> listOf("int", "char", "float", "double", "void", "return", "if", "else", "for", "while", "struct", "typedef", "sizeof", "include", "printf", "malloc", "free", "calloc", "realloc", "NULL")
            lang.contains("css") -> listOf("display", "justify-content", "align-items", "grid-template-columns", "gap", "padding", "margin", "color", "background", "flex", "grid", "repeat", "auto-fit", "minmax")
            else -> listOf("function", "const", "let", "var", "return", "if", "else", "for", "while", "class", "import", "export", "async", "await", "try", "catch")
        }

        for (kw in keywords) {
            val regex = Regex("\\b$kw\\b")
            regex.findAll(line).forEach { match ->
                addStyle(SpanStyle(color = CodeKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
            }
        }

        // String literals
        val stringRegex = Regex("(\"[^\"]*\"|'[^']*')")
        stringRegex.findAll(line).forEach { match ->
            addStyle(SpanStyle(color = CodeString), match.range.first, match.range.last + 1)
        }

        // Numbers
        val numberRegex = Regex("\\b\\d+(\\.\\d+)?\\b")
        numberRegex.findAll(line).forEach { match ->
            addStyle(SpanStyle(color = CodeNumber), match.range.first, match.range.last + 1)
        }

        // Function calls (e.g. name(...) )
        val funcRegex = Regex("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?=\\()")
        funcRegex.findAll(line).forEach { match ->
            addStyle(SpanStyle(color = CodeFunction), match.range.first, match.range.last + 1)
        }

        // Types in C
        if (lang.contains("c")) {
            val typeRegex = Regex("\\b(int|float|double|char|void|size_t)\\b")
            typeRegex.findAll(line).forEach { match ->
                addStyle(SpanStyle(color = CodeType), match.range.first, match.range.last + 1)
            }
        }
    }
    return builder
}
