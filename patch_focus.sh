sed -i '/var userTypedInput by remember { mutableStateOf("") }/a \
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }\
    var isFirstLaunch by remember { mutableStateOf(true) }\
    LaunchedEffect(Unit) {\
        if (isFirstLaunch) {\
            kotlinx.coroutines.delay(300)\
            try { focusRequester.requestFocus() } catch(e: Exception) {}\
            isFirstLaunch = false\
        }\
    }\
' app/src/main/java/com/example/MainActivity.kt
