import re

with open("app/src/main/java/com/example/ui/CallViewModel.kt", "r") as f:
    content = f.read()

repl = """                        try {
                            if (!java.io.File(model).exists()) {
                                throw Exception("Local model file not found: $model")
                            }
                            _isLocalModelLoading.value = true
                            val params = de.kherud.llama.ModelParameters().setModel(model)"""

content = content.replace("""                        try {
                            _isLocalModelLoading.value = true
                            val params = de.kherud.llama.ModelParameters().setModel(model)""", repl)

with open("app/src/main/java/com/example/ui/CallViewModel.kt", "w") as f:
    f.write(content)
