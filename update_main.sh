#!/bin/bash
awk '
/MyApplicationTheme/ {
    print "            if (initError != null) {"
    print "                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())) {"
    print "                    androidx.compose.material3.Text(initError!!, color = Color.Red)"
    print "                }"
    print "            } else {"
    print $0
    skip = 1
    next
}
/        }/ && skip {
    print $0
    print "            }"
    skip = 0
    next
}
{ print $0 }
' app/src/main/java/com/example/MainActivity.kt > app/src/main/java/com/example/MainActivity.tmp && mv app/src/main/java/com/example/MainActivity.tmp app/src/main/java/com/example/MainActivity.kt
