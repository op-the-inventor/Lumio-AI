import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

new_content = """            MyApplicationTheme(darkTheme = darkTheme, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (initError != null) {
                        Text(initError!!, color = Color.Red, modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()))
                    } else if (isLoggedIn) {"""

content = content.replace("""            MyApplicationTheme(darkTheme = darkTheme, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLoggedIn) {""", new_content)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
