import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# remove the inserted lines
bad_block = """            if (initError != null) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())) {
                    androidx.compose.material3.Text(initError!!, color = Color.Red)
                }
            } else {"""
content = content.replace(bad_block, "")

# fix the closing brace that was inserted
content = content.replace("            }\n                        com.example.ui.LumioAuthScreen(viewModel = viewModel)\n                    }", "                        com.example.ui.LumioAuthScreen(viewModel = viewModel)\n                    }")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
