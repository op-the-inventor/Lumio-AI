#!/bin/bash
# First, let's restore MainActivity.kt from git to have a clean slate for this block?
# No, let's just fix it by matching the exact lines.
# Between the end of items block and "Spacer(modifier = Modifier.height(8.dp))"

sed -i '560,590c\
                                                                }\
                                                            }\
                                                        }\
                                                    }\
                                                }\
                                            }\
                                        }\
                                        // Generating/thinking loader item\
                                        if (isGenerating) {\
                                            item {\
                                                ThinkingIndicatorItem()\
                                            }\
                                        }\
                                    }\
                                }\
                            }' app/src/main/java/com/example/MainActivity.kt
