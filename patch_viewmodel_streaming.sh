sed -i '/val messages: StateFlow<List<CallMessageEntity>> = _messages.asStateFlow()/a \
    private val _streamingMessage = MutableStateFlow<String?>("null")\
    val streamingMessage: StateFlow<String?> = _streamingMessage.asStateFlow()\
    private val _streamingEmotion = MutableStateFlow("NORMAL")\
    val streamingEmotion: StateFlow<String> = _streamingEmotion.asStateFlow()\
' app/src/main/java/com/example/ui/CallViewModel.kt
sed -i 's/MutableStateFlow<String?>("null")/MutableStateFlow<String?>(null)/' app/src/main/java/com/example/ui/CallViewModel.kt
