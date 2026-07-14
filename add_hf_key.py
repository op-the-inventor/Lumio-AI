import re

with open('app/src/main/java/com/example/ui/CallViewModel.kt', 'r') as f:
    content = f.read()

# Add _hfApiKey to CallViewModel
if "_hfApiKey" not in content:
    hf_prop = """
    private val _hfApiKey = MutableStateFlow("")
    val hfApiKey: StateFlow<String> = _hfApiKey.asStateFlow()
"""
    content = re.sub(r'val apiKey: StateFlow<String> = _apiKey.asStateFlow\(\)', 'val apiKey: StateFlow<String> = _apiKey.asStateFlow()\n' + hf_prop, content)
    
    # Load from settings
    content = re.sub(r'_apiKey\.value = settings\["api_key"\] \?: ""', '_apiKey.value = settings["api_key"] ?: ""\n                _hfApiKey.value = settings["hf_api_key"] ?: ""', content)

    # Save function
    save_func = """
    fun saveHfApiKey(key: String) {
        viewModelScope.launch {
            repository.saveSetting("hf_api_key", key)
            _hfApiKey.value = key
        }
    }
"""
    content = re.sub(r'fun saveApiKey\(key: String\) \{', save_func + '\n    fun saveApiKey(key: String) {', content)

with open('app/src/main/java/com/example/ui/CallViewModel.kt', 'w') as f:
    f.write(content)
