import re

with open('app/src/main/java/com/example/ui/LocalModelScreen.kt', 'r') as f:
    content = f.read()

# Add hfApiKey parameter
content = content.replace(
    'fun LocalModelScreen(\n    onBack: () -> Unit,\n    onModelSelected: (String) -> Unit,\n    viewModel: LocalModelViewModel = viewModel()\n)',
    'fun LocalModelScreen(\n    onBack: () -> Unit,\n    onModelSelected: (String) -> Unit,\n    hfApiKey: String,\n    viewModel: LocalModelViewModel = viewModel()\n)'
)

# Update viewModel calls
content = content.replace('viewModel.searchModels(searchQuery)', 'viewModel.searchModels(searchQuery, hfApiKey)')
content = content.replace('viewModel.loadModelFiles(modelId)', 'viewModel.loadModelFiles(modelId, hfApiKey)')
content = content.replace('viewModel.loadModelFiles(model.id)', 'viewModel.loadModelFiles(model.id, hfApiKey)')
content = content.replace('viewModel.downloadModel(context, selectedModel!!, file)', 'viewModel.downloadModel(context, selectedModel!!, file, hfApiKey)')

with open('app/src/main/java/com/example/ui/LocalModelScreen.kt', 'w') as f:
    f.write(content)
