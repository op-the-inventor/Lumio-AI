sed -i '110i \    var showPreview by remember { mutableStateOf(false) }' app/src/main/java/com/example/ui/MarkdownUI.kt
sed -i 's/onClick = { \/\* Open HTML preview \*\/ }/onClick = { showPreview = true }/g' app/src/main/java/com/example/ui/MarkdownUI.kt
