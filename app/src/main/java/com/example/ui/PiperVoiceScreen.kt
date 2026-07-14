package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast

@Composable
fun PiperVoiceScreen(
    onBack: () -> Unit,
    viewModel: PiperVoiceViewModel = viewModel()
) {
    val context = LocalContext.current
    val voices by viewModel.voices.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadVoices(context)
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Piper Voice Models",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Device Performance Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Performance Estimation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(viewModel.getDevicePerformanceEstimate(context), fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (voices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(voices) { voice ->
                    VoiceCard(
                        voice = voice,
                        onDownload = { viewModel.downloadVoice(context, voice) },
                        onDelete = { viewModel.deleteVoice(context, voice) }
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceCard(
    voice: EnhancedVoiceInfo,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${voice.info.name} (${voice.info.language.code})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (voice.isRecommended) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Rounded.Star, contentDescription = "Recommended", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        Text("Recommended", fontSize = 10.sp, color = Color(0xFFFFB300), modifier = Modifier.padding(start = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quality: ${voice.info.quality.replaceFirstChar { it.uppercase() }} • Size: ${voice.sizeMb} MB",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (voice.isDownloading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { voice.downloadProgress }, modifier = Modifier.fillMaxWidth().height(4.dp))
                }
            }
            
            if (voice.isInstalled) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
                Icon(Icons.Rounded.CheckCircle, contentDescription = "Installed", tint = Color(0xFF4CAF50), modifier = Modifier.padding(start = 8.dp))
            } else if (!voice.isDownloading) {
                IconButton(onClick = onDownload) {
                    Icon(Icons.Rounded.Download, contentDescription = "Download", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
