#!/bin/bash
sed -i '/overflow = TextOverflow.Ellipsis/a \
                                                                    )\
                                                                    Text(\
                                                                        text = fileType.uppercase(),\
                                                                        fontSize = 9.sp,\
                                                                        color = (if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f)\
                                                                    )' app/src/main/java/com/example/MainActivity.kt
# We also lost "Spacer(modifier = Modifier.height(8.dp))" before "Column(" (BOTTOM INPUT AREA)
sed -i '/verticalArrangement = Arrangement.spacedBy(4.dp)/i \
                            Spacer(modifier = Modifier.height(8.dp))\
\
                            // --- BOTTOM INPUT AREA ---' app/src/main/java/com/example/MainActivity.kt
