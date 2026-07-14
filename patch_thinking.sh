cat << 'INNER_EOF' >> app/src/main/java/com/example/MainActivity.kt

@Composable
fun ThinkingIndicatorItem() {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Thinking...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
INNER_EOF
