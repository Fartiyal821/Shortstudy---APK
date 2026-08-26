package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ReaderFontFamily

sealed class ContentBlock {
    data class Heading(val text: String, val level: Int) : ContentBlock()
    data class Paragraph(val text: String) : ContentBlock()
    data class Code(val code: String, val language: String) : ContentBlock()
    data class ListItem(val text: String, val isOrdered: Boolean, val index: Int) : ContentBlock()
    data class BlockQuote(val text: String) : ContentBlock()
}

@Composable
fun HtmlContentRenderer(
    htmlContent: String,
    fontSizeMultiplier: Float = 1.0f,
    lineHeightMultiplier: Float = 1.2f,
    fontFamily: ReaderFontFamily = ReaderFontFamily.SANS_SERIF,
    showCodeLineNumbers: Boolean = true,
    modifier: Modifier = Modifier
) {
    val blocks = remember(htmlContent) { parseHtmlToBlocks(htmlContent) }

    val baseFontFamily = when (fontFamily) {
        ReaderFontFamily.SANS_SERIF -> FontFamily.SansSerif
        ReaderFontFamily.SERIF -> FontFamily.Serif
        ReaderFontFamily.MONOSPACE -> FontFamily.Monospace
    }

    val bodySize: TextUnit = (16 * fontSizeMultiplier).sp
    val bodyLineHeight: TextUnit = (26 * fontSizeMultiplier * lineHeightMultiplier).sp
    val h2Size: TextUnit = (22 * fontSizeMultiplier).sp
    val h3Size: TextUnit = (18 * fontSizeMultiplier).sp

    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEach { block ->
            when (block) {
                is ContentBlock.Heading -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = block.text,
                        fontSize = if (block.level <= 2) h2Size else h3Size,
                        fontWeight = FontWeight.Bold,
                        fontFamily = baseFontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = (if (block.level <= 2) 30 else 24).sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                is ContentBlock.Paragraph -> {
                    Text(
                        text = parseInlineFormatting(block.text),
                        fontSize = bodySize,
                        lineHeight = bodyLineHeight,
                        fontFamily = baseFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                is ContentBlock.Code -> {
                    CodeBlock(
                        code = block.code,
                        language = block.language,
                        showLineNumbers = showCodeLineNumbers
                    )
                }
                is ContentBlock.ListItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        if (block.isOrdered) {
                            Text(
                                text = "${block.index}.",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = bodySize,
                                modifier = Modifier.width(28.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp, end = 12.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Text(
                            text = parseInlineFormatting(block.text),
                            fontSize = bodySize,
                            lineHeight = bodyLineHeight,
                            fontFamily = baseFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is ContentBlock.BlockQuote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(36.dp)
                                .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parseInlineFormatting(block.text),
                            fontSize = bodySize,
                            fontStyle = FontStyle.Italic,
                            fontFamily = baseFontFamily,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Parses HTML string into structured ContentBlock list.
 */
fun parseHtmlToBlocks(html: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val prePattern = Regex("<pre(?:[^>]*)><code(?:[^>]*class=[\"'](?:language-)?([a-zA-Z0-9_-]+)[\"'])?>([\\s\\S]*?)</code></pre>|<pre(?:[^>]*)>([\\s\\S]*?)</pre>", RegexOption.IGNORE_CASE)

    var remaining = html
    var match = prePattern.find(remaining)

    while (match != null) {
        val before = remaining.substring(0, match.range.first)
        if (before.isNotBlank()) {
            parseStandardTags(before, blocks)
        }

        val lang = match.groups[1]?.value ?: "python"
        val codeBody = match.groups[2]?.value ?: match.groups[3]?.value ?: ""
        val cleanCode = codeBody
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .trim()

        blocks.add(ContentBlock.Code(code = cleanCode, language = lang))

        remaining = remaining.substring(match.range.last + 1)
        match = prePattern.find(remaining)
    }

    if (remaining.isNotBlank()) {
        parseStandardTags(remaining, blocks)
    }

    return blocks
}

private fun parseStandardTags(htmlSegment: String, output: MutableList<ContentBlock>) {
    // Process tags line-by-line / segment-by-segment
    val tagPattern = Regex("<(h[1-4]|p|li|blockquote)>(.*?)</\\1>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    val matches = tagPattern.findAll(htmlSegment).toList()

    if (matches.isEmpty()) {
        // Fallback split on paragraphs or newlines
        val rawLines = htmlSegment.split("\n\n", "<br/>", "<br>").map { it.replace(Regex("<[^>]*>"), "").trim() }.filter { it.isNotBlank() }
        rawLines.forEach { output.add(ContentBlock.Paragraph(it)) }
        return
    }

    var listCounter = 1
    for (m in matches) {
        val tag = m.groups[1]?.value?.lowercase() ?: "p"
        val inner = m.groups[2]?.value?.trim() ?: ""

        when {
            tag.startsWith("h") -> {
                val level = tag.substring(1).toIntOrNull() ?: 2
                output.add(ContentBlock.Heading(inner.replace(Regex("<[^>]*>"), ""), level))
                listCounter = 1
            }
            tag == "li" -> {
                output.add(ContentBlock.ListItem(inner, isOrdered = false, index = listCounter++))
            }
            tag == "blockquote" -> {
                output.add(ContentBlock.BlockQuote(inner))
            }
            else -> {
                if (inner.isNotBlank()) {
                    output.add(ContentBlock.Paragraph(inner))
                }
            }
        }
    }
}

/**
 * Handles inline <strong>, <em>, and <code> chips for smooth typography.
 */
fun parseInlineFormatting(rawText: String): AnnotatedString {
    val builder = buildAnnotatedString {
        var cursor = 0
        val pattern = Regex("<strong>(.*?)</strong>|<b>(.*?)</b>|<em>(.*?)</em>|<i>(.*?)</i>|<code>(.*?)</code>", RegexOption.IGNORE_CASE)
        val matches = pattern.findAll(rawText)

        for (m in matches) {
            val pre = rawText.substring(cursor, m.range.first)
            append(pre.replace(Regex("<[^>]*>"), ""))

            val strong = m.groups[1]?.value ?: m.groups[2]?.value
            val italic = m.groups[3]?.value ?: m.groups[4]?.value
            val code = m.groups[5]?.value

            when {
                strong != null -> {
                    val start = length
                    append(strong)
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, length)
                }
                italic != null -> {
                    val start = length
                    append(italic)
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, length)
                }
                code != null -> {
                    val start = length
                    append(" $code ")
                    addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2563EB),
                            background = Color(0xFFDBEAFE).copy(alpha = 0.6f)
                        ),
                        start,
                        length
                    )
                }
            }
            cursor = m.range.last + 1
        }

        if (cursor < rawText.length) {
            append(rawText.substring(cursor).replace(Regex("<[^>]*>"), ""))
        }
    }
    return builder
}
