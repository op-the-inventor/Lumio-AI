import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Update collectAsState
content = content.replace("val selectedModel by viewModel.selectedModel.collectAsState()", """val cloudModel by viewModel.cloudModel.collectAsState()
    val localModel by viewModel.localModel.collectAsState()
    val useLocalModel by viewModel.useLocalModel.collectAsState()""")

# Update LocalModelScreen callback
content = content.replace("viewModel.saveSelectedModel(modelPath)", "viewModel.saveLocalModel(modelPath)\n                        viewModel.setUseLocalModel(true)")

# Find Model selection block and replace it
model_section_start = content.find("// Model selection")
test_model_start = content.find("// Test Model")

new_model_section = """// Model selection
                        Text(
                            text = "Model Settings",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        // Cloud Model Selection
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { viewModel.setUseLocalModel(false) }) {
                            androidx.compose.material3.RadioButton(
                                selected = !useLocalModel,
                                onClick = { viewModel.setUseLocalModel(false) }
                            )
                            Text("OpenRouter Model ID", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        
                        if (!useLocalModel) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp, bottom = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                TextField(
                                    value = cloudModel,
                                    onValueChange = { viewModel.saveCloudModel(it) },
                                    placeholder = { Text("e.g. meta-llama/llama-3.3-70b-instruct:free", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                    ),
                                    modifier = Modifier.weight(1f).testTag("cloud_model_id_input")
                                )
                            }
                        }

                        // Local Model Selection
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { viewModel.setUseLocalModel(true) }) {
                            androidx.compose.material3.RadioButton(
                                selected = useLocalModel,
                                onClick = { viewModel.setUseLocalModel(true) }
                            )
                            Text("Local Model Path (.gguf)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        
                        if (useLocalModel) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp, bottom = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                TextField(
                                    value = localModel,
                                    onValueChange = { viewModel.saveLocalModel(it) },
                                    placeholder = { Text("e.g. /storage/.../model.gguf", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                    ),
                                    modifier = Modifier.weight(1f).testTag("local_model_id_input")
                                )
                                IconButton(onClick = { showLocalModels = true }) {
                                    Icon(Icons.Default.Folder, contentDescription = "Browse Local Models", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        """

content = content[:model_section_start] + new_model_section + content[test_model_start:]

# Fix test model to use useLocalModel
content = content.replace("val isLocal = selectedModel.endsWith(\".gguf\")", "val isLocal = useLocalModel")
content = content.replace("de.kherud.llama.ModelParameters().setModel(selectedModel)", "de.kherud.llama.ModelParameters().setModel(localModel)")
content = content.replace("repo.getAICompletion(apiKey, selectedModel, testModelInput, emptyList(), \"You are a test assistant.\").first", "repo.getAICompletion(apiKey, cloudModel, testModelInput, emptyList(), \"You are a test assistant.\").first")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
