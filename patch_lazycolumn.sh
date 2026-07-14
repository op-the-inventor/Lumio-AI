sed -i '555,561c\
                                        if (streamingMessage != null) {\
                                            item {\
                                                Row(\
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),\
                                                    horizontalArrangement = Arrangement.Start\
                                                ) {\
                                                    Box(\
                                                        modifier = Modifier\
                                                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp))\
                                                            .background(Color.Transparent)\
                                                            .padding(4.dp)\
                                                            .widthIn(max = 340.dp)\
                                                    ) {\
                                                        Column {\
                                                            Row(verticalAlignment = Alignment.CenterVertically) {\
                                                                Text(\
                                                                    text = "Lumio",\
                                                                    fontSize = 12.sp,\
                                                                    fontWeight = FontWeight.Bold,\
                                                                    color = MaterialTheme.colorScheme.primary\
                                                                )\
                                                                if (streamingEmotion != "NORMAL") {\
                                                                    Spacer(modifier = Modifier.width(6.dp))\
                                                                    Text(\
                                                                        text = streamingEmotion,\
                                                                        fontSize = 10.sp,\
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)\
                                                                    )\
                                                                }\
                                                            }\
                                                            Spacer(modifier = Modifier.height(4.dp))\
                                                            if (streamingMessage!!.isEmpty()) {\
                                                                ThinkingIndicatorItem()\
                                                            } else {\
                                                                com.example.ui.MessageContent(text = streamingMessage!!, isUser = false)\
                                                            }\
                                                        }\
                                                    }\
                                                }\
                                            }\
                                        } else if (isGenerating) {\
                                            item {\
                                                ThinkingIndicatorItem()\
                                            }\
                                        }\
' app/src/main/java/com/example/MainActivity.kt
