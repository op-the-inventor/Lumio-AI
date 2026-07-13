#!/bin/bash
awk '
/items\(messages\) \{ msg ->/ {
    print "                                        items(messages, key = { it.id }) { msg ->"
    print "                                            var isVisible by remember { mutableStateOf(false) }"
    print "                                            LaunchedEffect(msg.id) { isVisible = true }"
    print "                                            androidx.compose.animation.AnimatedVisibility("
    print "                                                visible = isVisible,"
    print "                                                enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(350)) +"
    print "                                                        androidx.compose.animation.slideInVertically(initialOffsetY = { 20 }, animationSpec = androidx.compose.animation.core.tween(350)),"
    print "                                                modifier = Modifier.fillMaxWidth()"
    print "                                            ) {"
    skip = 1
    next
}
/if \(isGenerating\) \{/ && skip {
    print "                                            }"
    print "                                        }"
    print "                                        // Generating/thinking loader item"
    print "                                        if (isGenerating) {"
    skip = 0
    next
}
skip { print $0; next }
{ print $0 }
' app/src/main/java/com/example/MainActivity.kt > app/src/main/java/com/example/MainActivity.tmp && mv app/src/main/java/com/example/MainActivity.tmp app/src/main/java/com/example/MainActivity.kt
