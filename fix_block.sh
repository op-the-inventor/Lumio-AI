#!/bin/bash
sed -i '560,570c\
                                                                        overflow = TextOverflow.Ellipsis\
                                                                    )\
                                                                    Text(\
                                                                        text = fileType.uppercase(),\
                                                                        fontSize = 9.sp,\
                                                                        color = (if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f)\
                                                                    )\
                                                                }\
                                                            }' app/src/main/java/com/example/MainActivity.kt
