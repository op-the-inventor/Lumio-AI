#!/bin/bash
sed -i '582,586c\
                            }\
                            Spacer(modifier = Modifier.height(8.dp))\
\
                            // --- BOTTOM INPUT AREA ---\
                            Column(\
                                modifier = Modifier\
                                    .fillMaxWidth()\
                                    .padding(vertical = 4.dp),\
                                verticalArrangement = Arrangement.spacedBy(4.dp)\
                            ) {' app/src/main/java/com/example/MainActivity.kt
