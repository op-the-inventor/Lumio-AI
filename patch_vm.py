import re

with open("app/src/main/java/com/example/ui/CallViewModel.kt", "r") as f:
    content = f.read()

# Add new StateFlows
new_states = """    private val _cloudModel = MutableStateFlow("meta-llama/llama-3.3-70b-instruct:free")
    val cloudModel: StateFlow<String> = _cloudModel.asStateFlow()

    private val _localModel = MutableStateFlow("")
    val localModel: StateFlow<String> = _localModel.asStateFlow()

    private val _useLocalModel = MutableStateFlow(false)
    val useLocalModel: StateFlow<Boolean> = _useLocalModel.asStateFlow()
"""

content = content.replace("    private val _selectedModel = MutableStateFlow(\"meta-llama/llama-3.3-70b-instruct:free\")\n    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()", new_states)

# Replace in init
init_repl = """                _cloudModel.value = settings["cloud_model"] ?: "meta-llama/llama-3.3-70b-instruct:free"
                _localModel.value = settings["local_model"] ?: ""
                _useLocalModel.value = settings["use_local_model"]?.toBoolean() ?: false"""

content = content.replace("                _selectedModel.value = settings[\"selected_model\"] ?: \"meta-llama/llama-3.3-70b-instruct:free\"", init_repl)

# Replace save function
save_repl = """    fun saveCloudModel(model: String) {
        viewModelScope.launch {
            repository.saveSetting("cloud_model", model)
            _cloudModel.value = model
        }
    }

    fun saveLocalModel(model: String) {
        viewModelScope.launch {
            repository.saveSetting("local_model", model)
            _localModel.value = model
        }
    }

    fun setUseLocalModel(useLocal: Boolean) {
        viewModelScope.launch {
            repository.saveSetting("use_local_model", useLocal.toString())
            _useLocalModel.value = useLocal
        }
    }"""

content = content.replace("    fun saveSelectedModel(model: String) {\n        viewModelScope.launch {\n            repository.saveSetting(\"selected_model\", model)\n            _selectedModel.value = model\n        }\n    }", save_repl)

# Update testApiKey
test_api_repl = """    fun testApiKey() {
        val currentKey = _apiKey.value.trim()
        val isLocalModel = _useLocalModel.value
        
        if (isLocalModel) {
            val file = java.io.File(_localModel.value)
            if (file.exists()) {
                _apiKeyTestState.value = ApiKeyTestState.SUCCESS("Local model found and ready. No API key needed.")
            } else {
                _apiKeyTestState.value = ApiKeyTestState.ERROR("Local model file not found.")
            }
            return
        }"""
content = re.sub(r'    fun testApiKey\(\) \{.*?if \(isLocalModel\) \{.*?return\n        \}', test_api_repl, content, flags=re.DOTALL)

# Update sendUserMessage and startCall
content = content.replace("val isLocalModel = _selectedModel.value.endsWith(\".gguf\")", "val isLocalModel = _useLocalModel.value")
content = content.replace("val model = _selectedModel.value", "val model = if (_useLocalModel.value) _localModel.value else _cloudModel.value")


with open("app/src/main/java/com/example/ui/CallViewModel.kt", "w") as f:
    f.write(content)
