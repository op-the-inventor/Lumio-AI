import re

with open('app/src/main/java/com/example/ui/LocalModelScreen.kt', 'r') as f:
    content = f.read()

downloaded_ui = """
                    val dir = java.io.File(context.filesDir, "models")
                    val downloadedFiles = dir.listFiles()?.filter { it.name.endsWith(".gguf") } ?: emptyList()
                    if (downloadedFiles.isNotEmpty()) {
                        Text("Downloaded Models", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                        LazyColumn(modifier = Modifier.weight(if (searchResults.isEmpty()) 1f else 0.5f)) {
                            items(downloadedFiles) { file ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = file.name, fontWeight = FontWeight.Bold)
                                            Text(text = "Size: ${file.length() / (1024 * 1024)} MB", fontSize = 12.sp)
                                        }
                                        Button(onClick = {
                                            onModelSelected(file.absolutePath)
                                            onBack()
                                            android.widget.Toast.makeText(context, "Loaded local model ${file.name}", android.widget.Toast.LENGTH_SHORT).show()
                                        }) {
                                            Text("Load")
                                        }
                                    }
                                }
                            }
                        }
                    }
"""

# Insert this before "Text("Recommended for Mobile""
content = content.replace('Text("Recommended for Mobile", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))', downloaded_ui.strip() + '\n                    Text("Recommended for Mobile", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))')

with open('app/src/main/java/com/example/ui/LocalModelScreen.kt', 'w') as f:
    f.write(content)
