import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

buttons = """
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.exportChatHistory(context) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Export JSON", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        OutlinedButton(
                            onClick = { importLauncher.launch("application/json") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Import JSON", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
"""

content = content.replace('// Bottom settings buttons', buttons + '\n                    // Bottom settings buttons')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
