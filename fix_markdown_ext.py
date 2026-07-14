import re

with open("app/src/main/java/com/example/ui/MarkdownUI.kt", "r") as f:
    content = f.read()

# I want to add Links to formatMarkdownInline
links_replacement = """        // Find links
        val linkRegex = Regex("\\\\[(.*?)\\\\]\\\\((.*?)\\\\)")
        var textToProcess = text
        var offset = 0
        linkRegex.findAll(text).forEach { match ->
            // In a simple approach, we just style it blue
            addStyle(SpanStyle(color = Color(0xFF64B5F6), textDecoration = TextDecoration.Underline), match.range.first, match.range.last + 1)
        }
"""

if "// Find links" not in content:
    content = content.replace("// Find headers", links_replacement + "\n        // Find headers")

with open("app/src/main/java/com/example/ui/MarkdownUI.kt", "w") as f:
    f.write(content)
