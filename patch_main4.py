import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

repl = """                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                    if (localModel.isBlank() || !java.io.File(localModel).exists()) {
                                                        throw Exception("Local model file not found: $localModel")
                                                    }
                                                    val params = de.kherud.llama.ModelParameters().setModel(localModel)"""

content = content.replace("                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {\n                                                    val params = de.kherud.llama.ModelParameters().setModel(localModel)", repl)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
