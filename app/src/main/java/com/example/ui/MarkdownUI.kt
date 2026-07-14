package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

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
                is MarkdownBlock.Code -> {
                    CodeCard(language = block.language, code = block.code)
                }
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Text(val content: String) : MarkdownBlock()
    data class Code(val language: String, val code: String) : MarkdownBlock()
}

fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val regex = Regex("```(.*?)?\n(.*?)(```|$)", RegexOption.DOT_MATCHES_ALL)
    var lastIndex = 0
    
    regex.findAll(text).forEach { match ->
        val before = text.substring(lastIndex, match.range.first)
        if (before.isNotBlank()) {
            blocks.add(MarkdownBlock.Text(before.trim()))
        }
        val lang = match.groupValues[1].trim()
        val code = match.groupValues[2].trim()
        blocks.add(MarkdownBlock.Code(lang, code))
        lastIndex = match.range.last + 1
    }
    
    val after = text.substring(lastIndex)
    if (after.isNotBlank()) {
        blocks.add(MarkdownBlock.Text(after.trim()))
    }
    
    return blocks
}

fun formatMarkdownInline(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        // Simple regex for bold, italic, inline code
        val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
        val italicRegex = Regex("\\*(.*?)\\*")
        val inlineCodeRegex = Regex("`(.*?)`")
        
        // This is a naive implementation; a real one would use a proper lexer
        // We'll just append text for now, but we can do simple substitutions
        
        // Actually, let's just do a basic formatting
        // (Replacing with simple styled spans is complex with overlapping regex)
        append(text)
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

