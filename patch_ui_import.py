import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

import_launcher = """
    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            viewModel.importChatHistory(context, uri)
        }
    }
"""

content = content.replace('val selectedPersonaGender by viewModel.selectedPersonaGender.collectAsState(                                                )', 'val selectedPersonaGender by viewModel.selectedPersonaGender.collectAsState(                                                )\n' + import_launcher)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
