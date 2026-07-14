sed -i '2715,2765c\
fun MessageItem(msg: CallMessageEntity) {\
    val isUser = msg.sender == "user"\
    var isVisible by remember { mutableStateOf(false) }\
    LaunchedEffect(Unit) {\
        isVisible = true\
    }\
    AnimatedVisibility(\
        visible = isVisible,\
        enter = fadeIn(animationSpec = tween(350)) + slideInVertically(initialOffsetY = { 20 }, animationSpec = tween(350)),\
        modifier = Modifier.fillMaxWidth()\
    ) {\
        Row(\
            modifier = Modifier.fillMaxWidth().padding(vertical = if(isUser) 2.dp else 8.dp),\
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start\
        ) {\
            Box(\
                modifier = Modifier\
                    .let {\
                        if (isUser) {\
                            it.clip(RoundedCornerShape(\
                                topStart = 16.dp,\
                                topEnd = 16.dp,\
                                bottomStart = 16.dp,\
                                bottomEnd = 4.dp\
                            )).background(MaterialTheme.colorScheme.primaryContainer)\
                        } else {\
                            it.background(Color.Transparent)\
                        }\
                    }\
                    .padding(if(isUser) 12.dp else 4.dp)\
                    .widthIn(max = if(isUser) 280.dp else 340.dp)\
            ) {\
                Column {\
                    if (!isUser) {\
                        Row(verticalAlignment = Alignment.CenterVertically) {\
                            Text(\
                                text = "Lumio",\
                                fontSize = 12.sp,\
                                fontWeight = FontWeight.Bold,\
                                color = MaterialTheme.colorScheme.primary\
                            )\
                            if (msg.emotionTag != "NORMAL") {\
                                Spacer(modifier = Modifier.width(6.dp))\
                                Text(\
                                    text = msg.emotionTag,\
                                    fontSize = 10.sp,\
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)\
                                )\
                            }\
                        }\
                        Spacer(modifier = Modifier.height(4.dp))\
                    }\
                    MessageContent(text = msg.text, isUser = isUser)\
                }\
            }\
        }\
    }\
}\
' app/src/main/java/com/example/MainActivity.kt
