import re

with open("app/src/main/java/com/example/ui/MarkdownUI.kt", "r") as f:
    content = f.read()

bad_format_regex = re.compile(r"fun formatMarkdownInline\(.*?\)\s*:\s*AnnotatedString\s*\{.*?\n\}\n(?=@Composable)", re.DOTALL)

replacement = """fun formatMarkdownInline(text: String): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        
        // Find and style `inline code`
        val codeRegex = Regex("`(.*?)`")
        codeRegex.findAll(text).forEach { match ->
            addStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x33888888)), match.range.first, match.range.last + 1)
        }
        
        // Find and style **bold**
        val boldRegex = Regex("\\\\*\\\\*(.*?)\\\\*\\\\*")
        boldRegex.findAll(text).forEach { match ->
            addStyle(SpanStyle(fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
        
        // Find and style *italic*
        val italicRegex = Regex("\\\\*(.*?)\\\\*")
        italicRegex.findAll(text).forEach { match ->
            if (match.value.startsWith("**")) return@forEach
            addStyle(SpanStyle(fontStyle = FontStyle.Italic), match.range.first, match.range.last + 1)
        }
        
        // Find headers
        val h3 = Regex("^###\\\\s+(.*)", RegexOption.MULTILINE)
        h3.findAll(text).forEach { match ->
            addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp), match.range.first, match.range.last + 1)
        }
        
        val h2 = Regex("^##\\\\s+(.*)", RegexOption.MULTILINE)
        h2.findAll(text).forEach { match ->
            addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp), match.range.first, match.range.last + 1)
        }
        
        val h1 = Regex("^#\\\\s+(.*)", RegexOption.MULTILINE)
        h1.findAll(text).forEach { match ->
            addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp), match.range.first, match.range.last + 1)
        }
        
        // Find hr
        val hr = Regex("^---", RegexOption.MULTILINE)
        hr.findAll(text).forEach { match ->
            addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), match.range.first, match.range.last + 1)
        }
    }
}
"""

content = bad_format_regex.sub(replacement, content)

with open("app/src/main/java/com/example/ui/MarkdownUI.kt", "w") as f:
    f.write(content)
