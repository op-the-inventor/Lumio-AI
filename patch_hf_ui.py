with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    lines = f.readlines()

out_lines = []
in_openrouter = False
braces_count = 0
found = False

for i, line in enumerate(lines):
    out_lines.append(line)
    if "text = \"OpenRouter API Key\"" in line:
        in_openrouter = True
    
    if in_openrouter:
        if "OutlinedTextField(" in line:
            braces_count += 1
        elif "}" in line:
            # We are not counting accurately.
            pass

# Let's just find the "Reset Test" button or "Test API Key" button and insert before it
insert_idx = -1
for i, line in enumerate(lines):
    if "onClick = { viewModel.testApiKey() }" in line:
        # Search backwards for the Row
        for j in range(i, -1, -1):
            if "Row(" in lines[j] and "horizontalArrangement = Arrangement.spacedBy" in lines[j]:
                insert_idx = j
                break
        break

if insert_idx != -1:
    hf_ui = """
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Hugging Face API Key",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = hfApiKey,
                            onValueChange = { viewModel.saveHfApiKey(it) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            placeholder = { Text("hf_...", fontSize = 13.sp) },
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
    lines.insert(insert_idx, hf_ui)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.writelines(lines)
