import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add the states back inside MainActivity's main composable (around line 170)
# We will search for 'val isMuted by viewModel.isMuted.collectAsState('
states_repl = """val isMuted by viewModel.isMuted.collectAsState(                                                )
    val cloudModel by viewModel.cloudModel.collectAsState()
    val localModel by viewModel.localModel.collectAsState()
    val useLocalModel by viewModel.useLocalModel.collectAsState()"""

content = content.replace("val isMuted by viewModel.isMuted.collectAsState(                                                )", states_repl)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
