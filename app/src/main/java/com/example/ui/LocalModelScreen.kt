package com.example.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalModelScreen(
    onBack: () -> Unit,
    onModelSelected: (String) -> Unit,
    hfApiKey: String,
    viewModel: LocalModelViewModel = viewModel()
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val modelFiles by viewModel.modelFiles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()

    var selectedModel by remember { mutableStateOf<String?>(null) }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { viewModel.importModelFromUri(context, it) }
        }
    )

    LaunchedEffect(Unit) {
        if (searchResults.isEmpty()) {
            viewModel.searchModels("", hfApiKey)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local GGUF Models") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Hugging Face (e.g. Llama-3-8B-GGUF)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { 
                        keyboardController?.hide()
                        viewModel.searchModels(searchQuery, hfApiKey) 
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { 
                    keyboardController?.hide()
                    viewModel.searchModels(searchQuery, hfApiKey) 
                }),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    val totalRam = viewModel.getTotalRAM(context)
                    val freeRam = viewModel.getFreeRAM(context)
                    Text("RAM: ${freeRam}MB Free / ${totalRam}MB Total", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = { filePickerLauncher.launch("*/*") }) {
                    Text("Import GGUF", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            val glassColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
            val glassBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

            if (selectedModel == null) {
                val dir = java.io.File(context.getExternalFilesDir(null), "models")
                val downloadedFiles = dir.listFiles()?.filter { it.name.endsWith(".gguf") } ?: emptyList()
                if (downloadedFiles.isNotEmpty()) {
                    Text("Downloaded Models (Local folder)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    LazyColumn(modifier = Modifier.weight(if (searchResults.isEmpty()) 1f else 0.5f)) {
                        items(downloadedFiles) { file ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = glassColors,
                                border = glassBorder,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = file.name, fontWeight = FontWeight.Bold)
                                        Text(text = "Size: ${file.length() / (1024 * 1024)} MB", fontSize = 12.sp)
                                    }
                                    Button(onClick = {
                                        onModelSelected(file.absolutePath)
                                        onBack()
                                        android.widget.Toast.makeText(context, "Loaded local model ${file.name}", android.widget.Toast.LENGTH_SHORT).show()
                                    }) {
                                        Text("Load")
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (searchResults.isNotEmpty()) {
                    Text(if (searchQuery.isBlank()) "Trending GGUF Models" else "Search Results", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(searchResults) { model ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { 
                                        selectedModel = model.id
                                        viewModel.loadModelFiles(model.id, hfApiKey)
                                    },
                                colors = glassColors,
                                border = glassBorder,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = model.id, fontWeight = FontWeight.Bold)
                                    Text(text = "Downloads: ${model.downloads}", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectedModel = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to search")
                    }
                    Text("Files for $selectedModel", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                }
                
                if (downloadStatus.isNotEmpty()) {
                    Text(text = downloadStatus, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    if (downloadProgress != null) {
                        LinearProgressIndicator(
                            progress = { downloadProgress ?: 0f },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(modelFiles) { file ->
                        val sizeMb = file.size / (1024 * 1024)
                        val estRate = viewModel.getEstimateTokenRate(context, sizeMb)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = glassColors,
                            border = glassBorder,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = file.path, fontWeight = FontWeight.Bold)
                                    Text(text = "Size: ${sizeMb} MB", fontSize = 12.sp)
                                    Text(text = "Est. Speed: $estRate", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = { viewModel.downloadModel(context, selectedModel!!, file, hfApiKey) }) {
                                    Icon(Icons.Default.Download, contentDescription = "Download")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
                                    val dir = java.io.File(context.getExternalFilesDir(null), "models")
                                    val outputFile = java.io.File(dir, file.path.substringAfterLast("/"))
                                    if (!outputFile.exists()) {
                                        Toast.makeText(context, "Please download the model first", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    onModelSelected(outputFile.absolutePath)
                                    onBack()
                                    Toast.makeText(context, "Loaded local model ${file.path}", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Load")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
