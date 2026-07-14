import re

with open('app/src/main/java/com/example/ui/LocalModelViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('fun searchModels(query: String)', 'fun searchModels(query: String, apiKey: String)')
content = content.replace('val results = api.searchModels(query)', 'val auth = if (apiKey.isNotBlank()) "Bearer $apiKey" else null\n                val results = api.searchModels(auth, query)')

content = content.replace('fun loadModelFiles(modelId: String)', 'fun loadModelFiles(modelId: String, apiKey: String)')
content = content.replace('val files = api.getModelFiles(modelId)', 'val auth = if (apiKey.isNotBlank()) "Bearer $apiKey" else null\n                val files = api.getModelFiles(auth, modelId)')

content = content.replace('fun downloadModel(context: Context, modelId: String, file: HfFile)', 'fun downloadModel(context: Context, modelId: String, file: HfFile, apiKey: String)')
content = content.replace('val request = okhttp3.Request.Builder().url(url).build()', 'val requestBuilder = okhttp3.Request.Builder().url(url)\n                if (apiKey.isNotBlank()) requestBuilder.addHeader("Authorization", "Bearer $apiKey")\n                val request = requestBuilder.build()')

with open('app/src/main/java/com/example/ui/LocalModelViewModel.kt', 'w') as f:
    f.write(content)
