import re

with open('app/src/main/java/com/example/data/api/HuggingFaceApi.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'suspend fun searchModels(',
    'suspend fun searchModels(\n        @retrofit2.http.Header("Authorization") authHeader: String?,'
)
content = content.replace(
    'suspend fun getModelFiles(',
    'suspend fun getModelFiles(\n        @retrofit2.http.Header("Authorization") authHeader: String?,'
)

with open('app/src/main/java/com/example/data/api/HuggingFaceApi.kt', 'w') as f:
    f.write(content)
