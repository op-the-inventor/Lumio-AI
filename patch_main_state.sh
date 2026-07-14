sed -i '/val isGenerating by viewModel.isGenerating.collectAsState()/a \
    val streamingMessage by viewModel.streamingMessage.collectAsState()\
    val streamingEmotion by viewModel.streamingEmotion.collectAsState()\
' app/src/main/java/com/example/MainActivity.kt
