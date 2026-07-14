import re

with open("app/src/main/java/com/example/data/repository/AppRepository.kt", "r") as f:
    content = f.read()

repl = """                if (!File(modelPath).exists()) {
                    throw Exception("Local model file not found: $modelPath")
                }
                val params = ModelParameters().setModel(modelPath)"""

content = content.replace("val params = ModelParameters().setModel(modelPath)", repl)

with open("app/src/main/java/com/example/data/repository/AppRepository.kt", "w") as f:
    f.write(content)
