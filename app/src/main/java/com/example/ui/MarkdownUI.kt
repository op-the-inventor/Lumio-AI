package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

sealed class MarkdownBlock {
    data class Text(val content: String) : MarkdownBlock()
    data class Code(val language: String, val code: String) : MarkdownBlock()
    data class Quote(val content: String) : MarkdownBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    data class ListBlock(val items: List<String>, val isOrdered: Boolean) : MarkdownBlock()
}

fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val codeRegex = Regex("```(.*?)?\n(.*?)(```|$)", RegexOption.DOT_MATCHES_ALL)
    var lastIndex = 0
    
    codeRegex.findAll(text).forEach { match ->
        val before = text.substring(lastIndex, match.range.first)
        if (before.isNotBlank()) {
            blocks.addAll(parseNonCodeBlocks(before.trim()))
        }
        val lang = match.groupValues[1].trim()
        val code = match.groupValues[2].trim()
        blocks.add(MarkdownBlock.Code(lang, code))
        lastIndex = match.range.last + 1
    }
    
    val after = text.substring(lastIndex)
    if (after.isNotBlank()) {
        blocks.addAll(parseNonCodeBlocks(after.trim()))
    }
    
    return blocks
}

fun parseNonCodeBlocks(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    var currentText = StringBuilder()
    
    val lines = text.split("\n")
    var inQuote = false
    var quoteContent = StringBuilder()
    
    var inList = false
    var isOrderedList = false
    var listItems = mutableListOf<String>()
    
    for (line in lines) {
        if (line.startsWith("> ")) {
            if (currentText.isNotEmpty()) {
                blocks.add(MarkdownBlock.Text(currentText.toString().trimEnd()))
                currentText.clear()
            }
            if (inList) {
                blocks.add(MarkdownBlock.ListBlock(listItems.toList(), isOrderedList))
                listItems.clear()
                inList = false
            }
            inQuote = true
            quoteContent.append(line.substring(2)).append("\n")
        } else if (line.matches(Regex("^\\|.*\\|$"))) {
            if (currentText.isNotEmpty()) {
                blocks.add(MarkdownBlock.Text(currentText.toString().trimEnd()))
                currentText.clear()
            }
            if (inQuote) {
                blocks.add(MarkdownBlock.Quote(quoteContent.toString().trimEnd()))
                quoteContent.clear()
                inQuote = false
            }
            if (inList) {
                blocks.add(MarkdownBlock.ListBlock(listItems.toList(), isOrderedList))
                listItems.clear()
                inList = false
            }
            // Parse table
            val parts = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.all { it.matches(Regex("^-+$")) }) {
                // divider row, skip
            } else {
                if (blocks.isNotEmpty() && blocks.last() is MarkdownBlock.Table) {
                    val lastTable = blocks.removeLast() as MarkdownBlock.Table
                    val newRows = lastTable.rows.toMutableList()
                    newRows.add(parts)
                    blocks.add(MarkdownBlock.Table(lastTable.headers, newRows))
                } else {
                    blocks.add(MarkdownBlock.Table(parts, emptyList()))
                }
            }
        } else if (line.matches(Regex("^[-*]\\s+.*"))) {
            if (currentText.isNotEmpty()) {
                blocks.add(MarkdownBlock.Text(currentText.toString().trimEnd()))
                currentText.clear()
            }
            if (inQuote) {
                blocks.add(MarkdownBlock.Quote(quoteContent.toString().trimEnd()))
                quoteContent.clear()
                inQuote = false
            }
            if (inList && isOrderedList) {
                blocks.add(MarkdownBlock.ListBlock(listItems.toList(), isOrderedList))
                listItems.clear()
            }
            inList = true
            isOrderedList = false
            listItems.add(line.replaceFirst(Regex("^[-*]\\s+"), ""))
        } else if (line.matches(Regex("^\\d+\\.\\s+.*"))) {
            if (currentText.isNotEmpty()) {
                blocks.add(MarkdownBlock.Text(currentText.toString().trimEnd()))
                currentText.clear()
            }
            if (inQuote) {
                blocks.add(MarkdownBlock.Quote(quoteContent.toString().trimEnd()))
                quoteContent.clear()
                inQuote = false
            }
            if (inList && !isOrderedList) {
                blocks.add(MarkdownBlock.ListBlock(listItems.toList(), isOrderedList))
                listItems.clear()
            }
            inList = true
            isOrderedList = true
            listItems.add(line.replaceFirst(Regex("^\\d+\\.\\s+"), ""))
        } else {
            if (inQuote) {
                blocks.add(MarkdownBlock.Quote(quoteContent.toString().trimEnd()))
                quoteContent.clear()
                inQuote = false
            }
            if (inList) {
                blocks.add(MarkdownBlock.ListBlock(listItems.toList(), isOrderedList))
                listItems.clear()
                inList = false
            }
            currentText.append(line).append("\n")
        }
    }
    
    if (quoteContent.isNotEmpty()) blocks.add(MarkdownBlock.Quote(quoteContent.toString().trimEnd()))
    if (listItems.isNotEmpty()) blocks.add(MarkdownBlock.ListBlock(listItems.toList(), isOrderedList))
    if (currentText.isNotEmpty()) blocks.add(MarkdownBlock.Text(currentText.toString().trimEnd()))
    
    return blocks
}

fun formatMarkdownInline(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(formatMarkdownInline(text.substring(i + 2, end)))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("*", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(formatMarkdownInline(text.substring(i + 1, end)))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x33888888), fontSize = 14.sp)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("### ", i) -> {
                    val end = text.indexOf("\n", i).let { if (it == -1) text.length else it }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                        append(formatMarkdownInline(text.substring(i + 4, end)))
                    }
                    i = end
                }
                text.startsWith("## ", i) -> {
                    val end = text.indexOf("\n", i).let { if (it == -1) text.length else it }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                        append(formatMarkdownInline(text.substring(i + 3, end)))
                    }
                    i = end
                }
                text.startsWith("# ", i) -> {
                    val end = text.indexOf("\n", i).let { if (it == -1) text.length else it }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)) {
                        append(formatMarkdownInline(text.substring(i + 2, end)))
                    }
                    i = end
                }
                text.startsWith("---", i) -> {
                    val end = text.indexOf("\n", i).let { if (it == -1) text.length else it }
                    append("\n──────────\n")
                    i = end
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

@Composable
fun MessageContent(text: String, isUser: Boolean) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.Text -> {
                    Text(
                        text = formatMarkdownInline(block.content),
                        fontSize = 15.sp,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
                is MarkdownBlock.Quote -> {
                    val quoteColor = MaterialTheme.colorScheme.primary
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Box(modifier = Modifier.width(4.dp).height(IntrinsicSize.Min).background(quoteColor, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatMarkdownInline(block.content),
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is MarkdownBlock.ListBlock -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        block.items.forEachIndexed { index, item ->
                            Row {
                                Text(
                                    text = if (block.isOrdered) "${index + 1}. " else "• ",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(24.dp)
                                )
                                Text(text = formatMarkdownInline(item))
                            }
                        }
                    }
                }
                is MarkdownBlock.Table -> {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                        androidx.compose.foundation.layout.Row(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                            block.headers.forEach { header ->
                                Text(
                                    text = formatMarkdownInline(header),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(8.dp).widthIn(min = 60.dp)
                                )
                            }
                        }
                        block.rows.forEachIndexed { idx, row ->
                            androidx.compose.foundation.layout.Row(modifier = Modifier.background(if (idx % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                                row.forEach { cell ->
                                    Text(
                                        text = formatMarkdownInline(cell),
                                        modifier = Modifier.padding(8.dp).widthIn(min = 60.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                is MarkdownBlock.Code -> {
                    CodeCard(language = block.language, code = block.code)
                }
            }
        }
    }
}

@Composable
fun CodeCard(language: String, code: String) {
    val context = LocalContext.current
    var showPreview by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically()
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2D2D2D))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = language.ifEmpty { "text" },
                        color = Color(0xFFCCCCCC),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val isRunnable = language.lowercase() in listOf("html", "css", "js", "javascript")
                        if (isRunnable) {
                            IconButton(onClick = { showPreview = true }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = "Run", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        
                        IconButton(onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Code", code))
                        }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, code)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Code"))
                        }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        
                        val createDoc = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
                            if (uri != null) {
                                context.contentResolver.openOutputStream(uri)?.use {
                                    it.write(code.toByteArray())
                                }
                            }
                        }
                        IconButton(onClick = {
                            val ext = when(language.lowercase()) {
                                "html" -> "html"
                                "css" -> "css"
                                "javascript", "js" -> "js"
                                "python", "py" -> "py"
                                "java" -> "java"
                                "kotlin", "kt" -> "kt"
                                "cpp", "c++" -> "cpp"
                                "json" -> "json"
                                "xml" -> "xml"
                                "markdown", "md" -> "md"
                                else -> "txt"
                            }
                            createDoc.launch("code.$ext")
                        }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                
                // Code block
                Box(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(12.dp)) {
                    Text(
                        text = code,
                        color = Color(0xFFD4D4D4),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    if (showPreview) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showPreview = false }) {
            Card(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Preview", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        IconButton(onClick = { showPreview = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = {
                            android.webkit.WebView(it).apply {
                                settings.javaScriptEnabled = true
                                val wrappedCode = if (language.lowercase() == "html") {
                                    code
                                } else if (language.lowercase() == "css") {
                                    "<html><head><style>$code</style></head><body><h1>CSS Applied</h1></body></html>"
                                } else {
                                    "<html><body><script>$code</script><h1>JS Executed</h1></body></html>"
                                }
                                loadDataWithBaseURL(null, wrappedCode, "text/html", "utf-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
    }
}
