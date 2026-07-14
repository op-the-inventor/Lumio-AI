sed -i '429,431c\
                            var userTypedInput by remember { mutableStateOf("") }\
                            val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }\
                            var isFirstLaunch by remember { mutableStateOf(true) }\
                            LaunchedEffect(Unit) {\
                                if (isFirstLaunch) {\
                                    delay(300)\
                                    try {\
                                        focusRequester.requestFocus()\
                                    } catch (e: Exception) {}\
                                    isFirstLaunch = false\
                                }\
                            }\
' app/src/main/java/com/example/MainActivity.kt

sed -i 's/modifier = Modifier/modifier = Modifier.focusRequester(focusRequester)/' app/src/main/java/com/example/MainActivity.kt
