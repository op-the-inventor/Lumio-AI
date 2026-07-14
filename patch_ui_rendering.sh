sed -i '480,560c\
                                            ) {\
                                            val isUser = msg.sender == "user"\
                                            val hasAttachment = msg.text.contains("[Attached File:")\
                                            val cleanText = if (hasAttachment) {\
                                                msg.text.substringBefore("[Attached File:").trim()\
                                            } else {\
                                                msg.text\
                                            }\
                                            val attachmentInfo = if (hasAttachment) {\
                                                msg.text.substringAfter("[Attached File:").substringBefore("]").trim()\
                                            } else {\
                                                null\
                                            }\
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
                                                        com.example.ui.MessageContent(text = cleanText, isUser = isUser)\
                                                        if (attachmentInfo != null) {\
                                                            Spacer(modifier = Modifier.height(8.dp))\
                                                            Row(\
                                                                verticalAlignment = Alignment.CenterVertically,\
                                                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.1f)).padding(8.dp)\
                                                            ) {\
                                                                Icon(Icons.Rounded.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)\
                                                                Spacer(modifier = Modifier.width(4.dp))\
                                                                Text(\
                                                                    text = attachmentInfo,\
                                                                    fontSize = 10.sp,\
                                                                    color = MaterialTheme.colorScheme.primary\
                                                                )\
                                                            }\
                                                        }\
                                                    }\
                                                }\
                                            }\
                                            }\
' app/src/main/java/com/example/MainActivity.kt
