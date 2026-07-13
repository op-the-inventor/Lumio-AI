#!/bin/bash
sed -i '/Spacer(modifier = Modifier.height(16.dp)/i \
                        OutlinedButton(\
                            onClick = {\
                                viewModel.logout()\
                                showSettings = false\
                            },\
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),\
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),\
                            shape = RoundedCornerShape(12.dp),\
                            modifier = Modifier\
                                .fillMaxWidth()\
                                .height(48.dp)\
                                .testTag("logout_button")\
                        ) {\
                            Text("Logout", fontSize = 13.sp, fontWeight = FontWeight.Bold)\
                        }\
' app/src/main/java/com/example/MainActivity.kt
