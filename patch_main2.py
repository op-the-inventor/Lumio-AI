import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("val selectedModel by viewModel.selectedModel.collectAsState(                                                )", "")
content = content.replace("val isSelected = selectedModel == modelId", "val isSelected = cloudModel == modelId")
content = content.replace("viewModel.saveSelectedModel(modelId)", "viewModel.saveCloudModel(modelId)\n                                                viewModel.setUseLocalModel(false)")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/CallViewModel.kt", "r") as f:
    content2 = f.read()

content2 = content2.replace("val currentModel = _selectedModel.value", "")
content2 = content2.replace("currentModel", "(_cloudModel.value)") # wait, testApiKey had currentModel

with open("app/src/main/java/com/example/ui/CallViewModel.kt", "w") as f:
    f.write(content2)
