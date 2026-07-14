import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add hfApiKey to MainActivity
if "hfApiKey by viewModel.hfApiKey.collectAsState()" not in content:
    content = re.sub(r'val apiKey by viewModel.apiKey.collectAsState\(\s*\)', 'val apiKey by viewModel.apiKey.collectAsState()\n    val hfApiKey by viewModel.hfApiKey.collectAsState()', content)

# Add field to Settings
settings_field = """
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Hugging Face API Key", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = hfApiKey,
                            onValueChange = {
                                viewModel.saveHfApiKey(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("hf_...") },
                            visualTransformation = if (isApiKeyVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                    Icon(
                                        imageVector = if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
"""
if "Hugging Face API Key" not in content:
    content = re.sub(r'Text\("OpenRouter API Key".*?\(isApiKeyVisible = !isApiKeyVisible \}\) \{\s*Icon\(\s*imageVector = if \(isApiKeyVisible\) Icons\.Default\.Visibility else Icons\.Default\.VisibilityOff,\s*contentDescription = null\s*\)\s*\}\s*\}\s*\)', lambda m: m.group(0) + settings_field, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
