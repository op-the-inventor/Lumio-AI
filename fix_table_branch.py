with open("app/src/main/java/com/example/ui/MarkdownUI.kt", "r") as f:
    content = f.read()

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
