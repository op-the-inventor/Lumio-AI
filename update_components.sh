#!/bin/bash
# First, insert Button3D
cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/AuthScreens.kt

@Composable
fun Button3D(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val yOffset by animateFloatAsState(targetValue = if (isPressed) 4f else 0f, label = "button_press")
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .offset(y = 4.dp)
                .background(Color(0xFF6D28D9), RoundedCornerShape(28.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .offset(y = yOffset.dp)
                .background(PurplePrimary, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
INNER_EOF
