sed -i '209i \
    if (showPreview) {\
        androidx.compose.ui.window.Dialog(onDismissRequest = { showPreview = false }) {\
            Card(modifier = Modifier.fillMaxSize().padding(16.dp)) {\
                Column(modifier = Modifier.fillMaxSize()) {\
                    Row(\
                        modifier = Modifier.fillMaxWidth().padding(8.dp),\
                        horizontalArrangement = Arrangement.SpaceBetween,\
                        verticalAlignment = Alignment.CenterVertically\
                    ) {\
                        Text("Preview", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)\
                        IconButton(onClick = { showPreview = false }) {\
                            Icon(Icons.Rounded.Close, contentDescription = "Close")\
                        }\
                    }\
                    androidx.compose.ui.viewinterop.AndroidView(\
                        factory = {\
                            android.webkit.WebView(it).apply {\
                                settings.javaScriptEnabled = true\
                                val wrappedCode = if (language.lowercase() == "html") {\
                                    code\
                                } else if (language.lowercase() == "css") {\
                                    "<html><head><style>$code</style></head><body><h1>CSS Applied</h1></body></html>"\
                                } else {\
                                    "<html><body><script>$code</script><h1>JS Executed</h1></body></html>"\
                                }\
                                loadDataWithBaseURL(null, wrappedCode, "text/html", "utf-8", null)\
                            }\
                        },\
                        modifier = Modifier.fillMaxSize()\
                    )\
                }\
            }\
        }\
    }\
' app/src/main/java/com/example/ui/MarkdownUI.kt
