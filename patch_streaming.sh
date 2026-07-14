sed -i '/val request = OpenRouterRequest(/i \
        // Streaming implemented directly in ViewModel instead of AppRepository for OpenRouter\
' app/src/main/java/com/example/data/repository/AppRepository.kt
