import re

with open("app/src/main/java/com/example/ui/MarkdownUI.kt", "r") as f:
    content = f.read()

# Add MarkdownBlock.Table
if "data class Table(" not in content:
    content = content.replace("data class Quote(val content: String) : MarkdownBlock()", "data class Quote(val content: String) : MarkdownBlock()\n    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()")

# parse tables
table_parser = """        } else if (line.matches(Regex("^\\\\|.*\\\\|$"))) {
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
"""

if "Parse table" not in content:
    content = content.replace("        } else if (line.matches(Regex(\"^[-*]\\\\s+.*\"))) {", table_parser + "        } else if (line.matches(Regex(\"^[-*]\\\\s+.*\"))) {")

# Render tables
table_renderer = """                is MarkdownBlock.Table -> {
                    Column(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                        Row(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                            block.headers.forEach { header ->
                                Text(
                                    text = formatMarkdownInline(header),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(8.dp).widthIn(min = 60.dp)
                                )
                            }
                        }
                        block.rows.forEachIndexed { idx, row ->
                            Row(modifier = Modifier.background(if (idx % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
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
"""

if "is MarkdownBlock.Table" not in content:
    content = content.replace("                is MarkdownBlock.Code -> {", table_renderer + "                is MarkdownBlock.Code -> {")

with open("app/src/main/java/com/example/ui/MarkdownUI.kt", "w") as f:
    f.write(content)
