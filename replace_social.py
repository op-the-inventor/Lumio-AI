import re

with open("app/src/main/java/com/example/ui/AuthScreens.kt", "r") as f:
    content = f.read()

# Replace SocialButtons composable definition
new_social = """@Composable
fun SocialButtons(onProviderSelected: (String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val yOffset by animateFloatAsState(targetValue = if (isPressed) 4f else 0f, label = "button_press")
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = { onProviderSelected("google") })
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .offset(y = 4.dp)
                .background(Color(0xFFE5E7EB), RoundedCornerShape(28.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .offset(y = yOffset.dp)
                .background(Color.White, RoundedCornerShape(28.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Continue with Google", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}"""

pattern = r"@Composable\s*fun SocialButtons.*?^}\s*$"
content = re.sub(pattern, new_social, content, flags=re.DOTALL | re.MULTILINE)

with open("app/src/main/java/com/example/ui/AuthScreens.kt", "w") as f:
    f.write(content)
