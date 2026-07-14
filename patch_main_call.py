import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'com.example.ui.LocalModelScreen(\n                    onBack = { showLocalModels = false },\n                    onModelSelected = { modelPath ->\n                        viewModel.saveSelectedModel(modelPath)\n                        showLocalModels = false\n                    }\n                )',
    'com.example.ui.LocalModelScreen(\n                    onBack = { showLocalModels = false },\n                    onModelSelected = { modelPath ->\n                        viewModel.saveSelectedModel(modelPath)\n                        showLocalModels = false\n                    },\n                    hfApiKey = hfApiKey\n                )'
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
