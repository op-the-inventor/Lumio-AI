package com.example

import androidx.compose.ui.focus.focusRequester

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import io.github.jan.supabase.auth.handleDeeplinks
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.ui.ApiKeyTestState
import com.example.ui.CallState
import com.example.ui.CallViewModel
import com.example.ui.EmotionalPreset
import com.example.ui.SplashScreen
import com.example.ui.theme.*
import com.example.data.database.CallMessageEntity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        var initError: String? = null
        try { com.example.data.api.supabase.handleDeeplinks(intent) } catch (e: Throwable) { initError = e.stackTraceToString() }
        enableEdgeToEdge()
        setContent {
            val viewModel: CallViewModel = viewModel()
            val darkTheme by viewModel.darkTheme.collectAsState()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()
            var showSplash by remember { mutableStateOf(true) }

            MyApplicationTheme(darkTheme = darkTheme, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showSplash) {
                        SplashScreen { showSplash = false }
                    } else {
                        androidx.compose.animation.Crossfade(targetState = isLoggedIn, animationSpec = androidx.compose.animation.core.tween(500), label = "main_crossfade") { loggedIn ->
                            if (initError != null) {
                                Text(initError!!, color = Color.Red, modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()))
                            } else if (loggedIn) {
                                CallScreen(viewModel = viewModel)
                            } else {
                                com.example.ui.LumioAuthScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun MinimalistMenuIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(18.dp)) {
        val strokeWidth = 2.dp.toPx(                                                )
        drawLine(
            color = color,
            start = Offset(0f, size.height * 0.35f),
            end = Offset(size.width, size.height * 0.35f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
                                                        )
        drawLine(
            color = color,
            start = Offset(0f, size.height * 0.65f),
            end = Offset(size.width, size.height * 0.65f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
                                                        )
    }
}

@Composable
fun WaveformIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Row(
        modifier = modifier.width(18.dp).height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val heights = listOf(0.4f, 0.9f, 0.6f, 0.8f, 0.3f                                                )
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .weight(1f                                                )
                    .fillMaxHeight(h                                                )
                    .background(color, RoundedCornerShape(1.dp)                                                )
                                                            )
        }
    }
}

enum class ScreenTab {
    CALL,
    CHAT
}

@OptIn(ExperimentalMaterial3Api::class                                                )
@Composable
fun CallScreen(viewModel: CallViewModel = viewModel()) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Gather states
    val darkTheme by viewModel.darkTheme.collectAsState(                                                )
    val apiKey by viewModel.apiKey.collectAsState()
    val hfApiKey by viewModel.hfApiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState(                                                )
    val callState by viewModel.callState.collectAsState(                                                )
    val isMuted by viewModel.isMuted.collectAsState(                                                )
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsState(                                                )
    val currentAIEmotion by viewModel.currentAIEmotion.collectAsState(                                                )
    val isTTSPlaying by viewModel.isTTSPlaying.collectAsState(                                                )
    val isListening by viewModel.isListening.collectAsState(                                                )
    val speechTranscript by viewModel.speechTranscript.collectAsState(                                                )
    val error by viewModel.error.collectAsState(                                                )
    val messages by viewModel.messages.collectAsState(                                                )
    val streamingMessage by viewModel.streamingMessage.collectAsState()
    val streamingEmotion by viewModel.streamingEmotion.collectAsState()
    val sessions by viewModel.sessions.collectAsState(                                                )
    val isGenerating by viewModel.isGenerating.collectAsState(                                                )
    val isLocalModelLoading by viewModel.isLocalModelLoading.collectAsState()

    val selectedLanguage by viewModel.selectedLanguage.collectAsState(                                                )
    val selectedEmotionalPresetId by viewModel.selectedEmotionalPresetId.collectAsState(                                                )
    val selectedPersonaGender by viewModel.selectedPersonaGender.collectAsState(                                                )

    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            viewModel.importChatHistory(context, uri)
        }
    }

    val voiceEmotionSettings by viewModel.voiceEmotionSettings.collectAsState(                                                )

    // Screen navigation overlays
    var showSettings by remember { mutableStateOf(false) }
    var showLocalModels by remember { mutableStateOf(false) }
    var showPiperVoices by remember { mutableStateOf(false) }
    var isSidebarOpen by remember { mutableStateOf(false) }
    var userTypedInput by remember { mutableStateOf("") }
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    var isFirstLaunch by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        if (isFirstLaunch) {
            kotlinx.coroutines.delay(300)
            try { focusRequester.requestFocus(); keyboardController?.show() } catch(e: Exception) {}
            isFirstLaunch = false
        }
    }

    var isApiKeyVisible by remember { mutableStateOf(false) }

    // Settings collapsible section states
    var isAppearanceExpanded by remember { mutableStateOf(false) }
    var isAccentExpanded by remember { mutableStateOf(false) }
    var isGeneralExpanded by remember { mutableStateOf(false) }
    var isVoiceExpanded by remember { mutableStateOf(false) }
    var isSafetyExpanded by remember { mutableStateOf(false) }
    var isDataExpanded by remember { mutableStateOf(false) }
    var isAboutExpanded by remember { mutableStateOf(false) }
    var isTributeExpanded by remember { mutableStateOf(false) }
    var isTermsExpanded by remember { mutableStateOf(false) }
    var isPrivacyExpanded by remember { mutableStateOf(false) }

    var selectedFileUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileType by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(                                                )
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            var name: String? = null
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null                                                )
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        val colIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME                                                )
                        if (colIndex != -1) {
                            name = cursor.getString(colIndex                                                )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace(                                                )
                } finally {
                    cursor?.close(                                                )
                }
            }
            if (name == null) {
                name = uri.path
                val cut = name?.lastIndexOf('/') ?: -1
                if (cut != -1) {
                    name = name?.substring(cut + 1                                                )
                }
            }
            selectedFileName = name ?: "file"
            selectedFileType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            Toast.makeText(context, "Attached: $selectedFileName", Toast.LENGTH_SHORT).show(                                                )
        }
    }

    // Floating scroll controller for transcript
    val listState = rememberLazyListState()
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(messages.size, isGenerating, isImeVisible, streamingMessage) {
        if (messages.isNotEmpty() || isGenerating) {
            val lastIndex = if (isGenerating) messages.size else messages.size - 1
            if (lastIndex >= 0) {
                // Scroll to the end of the item by adding a large positive offset or just normal scroll
                listState.animateScrollToItem(lastIndex, scrollOffset = 10000)
            }
        }
    }

    // Audio Permission Launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(                                                )
    ) { isGranted ->
        if (isGranted) {
            if (callState == CallState.ACTIVE) {
                viewModel.startListening(                                                )
            } else {
                viewModel.startCall(                                                )
            }
        } else {
            Toast.makeText(context, "Microphone permission is required for voice calls.", Toast.LENGTH_LONG).show(                                                )
        }
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        // Main layout container (Safe areas respected using statusBarsPadding / navigationBarsPadding                                                )
        val bgGradient = if (darkTheme) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0C0C0D),
                    Color(0xFF141416),
                    Color(0xFF0C0C0D                                                )
                                                                )
                                                            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF8F9FB),
                    Color(0xFFEFF1F4),
                    Color(0xFFF8F9FB                                                )
                                                                )
                                                            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(bottom = innerPadding.calculateBottomPadding())
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // --- FLOATING TRANSPARENT TOP BAR ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Top Left Menu Button to toggle Sidebar
                        IconButton(
                            onClick = { isSidebarOpen = true },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("menu_sidebar_button")
                        ) {
                            MinimalistMenuIcon(color = MaterialTheme.colorScheme.onSurface)
                        }
                        
                        // Temp Chat Button moved to top left
                        Button(
                            onClick = {
                                viewModel.clearHistory()
                                Toast.makeText(context, "Temporary chat started (History cleared)", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("temp_chat_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = "Temp Chat",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Temp Chat",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Top Right Action Icons: Call AI
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Call AI Button
                        IconButton(
                            onClick = {
                                if (callState == CallState.ACTIVE) {
                                    viewModel.endCall()
                                    Toast.makeText(context, "Call ended", Toast.LENGTH_SHORT).show()
                                } else {
                                    val hasAudioPermission = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasAudioPermission) {
                                        viewModel.startCall()
                                    } else {
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                .border(
                                    width = 1.dp,
                                    color = if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA),
                                    shape = CircleShape
                                )
                                .testTag("top_right_call_button")
                        ) {
                            Icon(
                                imageVector = if (callState == CallState.ACTIVE) Icons.Rounded.PhoneInTalk else Icons.Rounded.Phone,
                                contentDescription = "Call AI",
                                tint = if (callState == CallState.ACTIVE) NeonGreen else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // --- MAIN CONTENT BODY ---
                Box(
                    modifier = Modifier
                        .weight(1f                                                )
                        .fillMaxWidth(                                                )
                ) {
                        // --- CHAT PANEL ---
                        Column(
                            modifier = Modifier
                                .fillMaxSize(                                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // --- MESSAGE STREAM (SCROLLABLE LIST) ---
                            Box(
                                modifier = Modifier
                                    .weight(1f)


                                    .fillMaxWidth(                                                )
                                    .padding(vertical = 4.dp                                                )
                            ) {
                                if (messages.isEmpty() && !isGenerating) {
                                    // Elegant empty state
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize(                                                )
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Forum,
                                            contentDescription = "Empty chat",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(64.dp                                                )
                                                                                        )
                                        Spacer(modifier = Modifier.height(12.dp)                                                )
                                        Text(
                                            text = "No messages yet",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                                                                        )
                                        Spacer(modifier = Modifier.height(4.dp)                                                )
                                        Text(
                                            text = "Type an uncensored prompt below or start a voice call to begin chatting with Lumio!",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 24.dp                                                )
                                                                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp                                                )
                                    ) {
                                        items(messages, key = { it.id }) { msg ->
                                            var isVisible by remember { mutableStateOf(false) }
                                            LaunchedEffect(msg.id) { isVisible = true }
                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = isVisible,
                                                enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(350)) +
                                                        androidx.compose.animation.slideInVertically(initialOffsetY = { 20 }, animationSpec = androidx.compose.animation.core.tween(350)),
                                                modifier = Modifier.fillMaxWidth(                                                )
                                            ) {
                                            val isUser = msg.sender == "user"
                                            val hasAttachment = msg.text.contains("[Attached File:")
                                            val cleanText = if (hasAttachment) {
                                                msg.text.substringBefore("[Attached File:").trim()
                                            } else {
                                                msg.text
                                            }
                                            val attachmentInfo = if (hasAttachment) {
                                                msg.text.substringAfter("[Attached File:").substringBefore("]").trim()
                                            } else {
                                                null
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = if(isUser) 2.dp else 8.dp),
                                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .let {
                                                            if (isUser) {
                                                                it.clip(RoundedCornerShape(
                                                                    topStart = 16.dp,
                                                                    topEnd = 16.dp,
                                                                    bottomStart = 16.dp,
                                                                    bottomEnd = 4.dp
                                                                )).background(MaterialTheme.colorScheme.primaryContainer)
                                                            } else {
                                                                it.background(Color.Transparent)
                                                            }
                                                        }
                                                        .padding(if(isUser) 12.dp else 4.dp)
                                                        .widthIn(max = if(isUser) 280.dp else 340.dp)
                                                ) {
                                                    Column {
                                                        if (!isUser) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = "Lumio",
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                                if (msg.emotionTag != "NORMAL") {
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Text(
                                                                        text = msg.emotionTag,
                                                                        fontSize = 10.sp,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                                    )
                                                                }
                                                            }
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                        }
                                                        com.example.ui.MessageContent(text = cleanText, isUser = isUser)
                                                        if (attachmentInfo != null) {
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.1f)).padding(8.dp)
                                                            ) {
                                                                Icon(Icons.Rounded.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = attachmentInfo,
                                                                    fontSize = 10.sp,
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            }
                                        }

                                        if (streamingMessage != null) {
                                            item {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.Start
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp))
                                                            .background(Color.Transparent)
                                                            .padding(4.dp)
                                                            .widthIn(max = 340.dp)
                                                    ) {
                                                        Column {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = "Lumio",
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                                if (streamingEmotion != "NORMAL") {
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Text(
                                                                        text = streamingEmotion,
                                                                        fontSize = 10.sp,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                                    )
                                                                }
                                                            }
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            if (isLocalModelLoading) {
                                                                Text("Loading local model into memory...", fontSize = 12.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.primary)
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                LinearProgressIndicator(modifier = Modifier.width(100.dp))
                                                            } else if (streamingMessage!!.isEmpty()) {
                                                                ThinkingIndicatorItem()
                                                            } else {
                                                                com.example.ui.MessageContent(text = streamingMessage!!, isUser = false)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else if (isGenerating) {
                                            item {
                                                if (isLocalModelLoading) {
                                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Start) {
                                                        Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color.Transparent).padding(4.dp)) {
                                                            Column {
                                                                Text("Lumio", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                Text("Loading local model into memory...", fontSize = 12.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.primary)
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                LinearProgressIndicator(modifier = Modifier.width(100.dp))
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    ThinkingIndicatorItem()
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp)                                                )

                            // --- BOTTOM INPUT AREA ---
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(                                                )
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp                                                )
                            ) {
                                // --- ATTACHED FILE PREVIEW CHIP ---
                                if (selectedFileName != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(                                                )
                                            .clip(RoundedCornerShape(16.dp)                                                )
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)                                                )
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f                                                )
                                        ) {
                                            Icon(
                                                imageVector = if (selectedFileType?.startsWith("image/") == true) Icons.Rounded.Image else Icons.Rounded.AttachFile,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp                                                )
                                                                                            )
                                            Column {
                                                Text(
                                                    text = selectedFileName ?: "",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Text(
                                                    text = (selectedFileType ?: "").uppercase(),
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f                                                )
                                                                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                selectedFileUri = null
                                                selectedFileName = null
                                                selectedFileType = null
                                            },
                                            modifier = Modifier.size(24.dp                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Remove attachment",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(16.dp                                                )
                                                                                            )
                                        }
                                    }
                                }

                                // --- INPUT ROW (ATTACH + TEXTFIELD + SEND) ---
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 60.dp)
                                        .shadow(
                                            elevation = 16.dp,
                                            shape = RoundedCornerShape(32.dp),
                                            spotColor = if (darkTheme) Color.White else Color.Black,
                                            ambientColor = if (darkTheme) Color.White else Color.Black
                                        )
                                        .clip(RoundedCornerShape(32.dp))
                                        .background(if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFEFEFF4))
                                        .border(
                                            width = 1.dp,
                                            color = if (darkTheme) Color(0xFF3D3D40) else Color(0xFFE5E5EA),
                                            shape = RoundedCornerShape(32.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left inside: Plus button `+`
                                    IconButton(
                                        onClick = { filePickerLauncher.launch("*/*") },
                                        modifier = Modifier
                                            .size(42.dp                                                )
                                            .testTag("upload_button"                                                )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "Upload File",
                                            tint = if (darkTheme) Color.White else Color(0xFF1C1C1E),
                                            modifier = Modifier.size(24.dp                                                )
                                                                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp)                                                )

                                    // Middle text input
                                    TextField(
                                        value = userTypedInput,
                                        onValueChange = { userTypedInput = it },
                                        placeholder = {
                                            Text(
                                                "Message LUMIO...",
                                                color = Color(0xFF8E8E93),
                                                fontSize = 14.sp
                                                                                            )
                                        },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                            cursorColor = if (darkTheme) Color.White else Color.Black
                                        ),
                                        modifier = Modifier
                                            .weight(1f                                                )
                                            .focusRequester(focusRequester).testTag("text_input"),
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Send
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onSend = {
                                                if (userTypedInput.trim().isNotEmpty() || selectedFileName != null) {
                                                    val messageText = if (selectedFileName != null) {
                                                        "${userTypedInput.trim()} \n[Attached File: $selectedFileName ($selectedFileType)]"
                                                    } else {
                                                        userTypedInput.trim(                                                )
                                                    }
                                                    viewModel.sendUserMessage(messageText                                                )
                                                    userTypedInput = ""
                                                    selectedFileUri = null
                                                    selectedFileName = null
                                                    selectedFileType = null
                                                    keyboardController?.hide(                                                )
                                                }
                                            }
                                                                                        )
                                                                                    )

                                    Spacer(modifier = Modifier.width(4.dp)                                                )

                                    // Rightmost: Black Animated Send button
                                    val infiniteTransition = rememberInfiniteTransition(                                                )
                                    val scale by infiniteTransition.animateFloat(
                                        initialValue = 0.95f,
                                        targetValue = 1.05f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(800, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                                                                        )
                                                                                    )
                                    val hasTypedText = userTypedInput.trim().isNotEmpty() || selectedFileName != null
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp                                                )
                                            .scale(if (hasTypedText) 1f else scale                                                )
                                            .clip(CircleShape                                                )
                                            .background(Color.Black                                                )
                                            .clickable {
                                                if (hasTypedText) {
                                                    val messageText = if (selectedFileName != null) {
                                                        "${userTypedInput.trim()} \n[Attached File: $selectedFileName ($selectedFileType)]"
                                                    } else {
                                                        userTypedInput.trim(                                                )
                                                    }
                                                    viewModel.sendUserMessage(messageText                                                )
                                                    userTypedInput = ""
                                                    selectedFileUri = null
                                                    selectedFileName = null
                                                    selectedFileType = null
                                                    keyboardController?.hide(                                                )
                                                }
                                            }
                                            .testTag("send_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Send,
                                            contentDescription = "Send message",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp                                                )
                                                                                        )
                                    }
                                }
                            }
                        } // end of Chat Panel

                }
            }

            // --- CUSTOM SIDEBAR (SLIDING LEFT DRAWER) OVERLAY ---
            AnimatedVisibility(
                visible = isSidebarOpen,
                enter = fadeIn(),
                exit = fadeOut(                                                )
            ) {
                // Semi-transparent dim background scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize(                                                )
                        .background(Color.Black.copy(alpha = 0.45f)                                                )
                        .clickable { isSidebarOpen = false }
                                                                )
            }

            AnimatedVisibility(
                visible = isSidebarOpen,
                enter = fadeIn() + slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow                                                )
                ),
                exit = fadeOut() + slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow                                                )
                ),
                modifier = Modifier.fillMaxHeight().width(310.dp                                                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(                                                )
                        .width(310.dp                                                )
                        .background(if (darkTheme) Color(0xFF0F0F10) else Color.White                                                )
                        .border(
                            width = 1.dp,
                            color = if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA                                                )
                                                                        )
                        .statusBarsPadding(                                                )
                        .navigationBarsPadding(                                                )
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f                                                )
                    ) {
                        // Header "Lumio" with search icon and refresh icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp                                                )
                            ) {
                                Text(
                                    text = "Lumio",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (darkTheme) Color.White else Color.Black,
                                    fontFamily = FontFamily.SansSerif
                                                                                )
                                // Search icon in a round button next to "Lumio"
                                Box(
                                    modifier = Modifier
                                        .size(36.dp                                                )
                                        .clip(CircleShape                                                )
                                        .background(if (darkTheme) Color(0xFF202022) else Color(0xFFF2F2F7)                                                )
                                        .clickable {
                                            Toast.makeText(context, "Search chat history...", Toast.LENGTH_SHORT).show(                                                )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = "Search",
                                        tint = if (darkTheme) Color.White else Color.Black,
                                        modifier = Modifier.size(16.dp                                                )
                                                                                    )
                                }
                            }

                            // Sync/Refresh icon on top right
                            Box(
                                modifier = Modifier
                                    .size(36.dp                                                )
                                    .clip(CircleShape                                                )
                                    .background(if (darkTheme) Color(0xFF202022) else Color(0xFFF2F2F7)                                                )
                                    .clickable {
                                        viewModel.clearHistory(                                                )
                                        Toast.makeText(context, "History synchronized.", Toast.LENGTH_SHORT).show(                                                )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Sync,
                                    contentDescription = "Sync",
                                    tint = if (darkTheme) Color.White else Color.Black,
                                    modifier = Modifier.size(16.dp                                                )
                                                                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp)                                                )

                        // Section Heading "Recents"
                        Text(
                            text = "Recents",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp                                                )
                                                                        )

                        // Scrollable list of recent items
                        val currentChatId by viewModel.currentChatId.collectAsState(                                                )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.weight(1f                                                )
                        ) {
                            items(sessions.sortedByDescending { it.timestamp }) { session ->
                                val isActive = session.id == currentChatId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(                                                )
                                        .clip(RoundedCornerShape(8.dp)                                                )
                                        .background(if (isActive) (if (darkTheme) Color(0xFF1D1D20) else Color(0xFFF2F2F7)) else Color.Transparent                                                )
                                        .clickable {
                                            viewModel.loadChatSession(session.id                                                )
                                            isSidebarOpen = false
                                            Toast.makeText(context, "Loaded: ${session.title}", Toast.LENGTH_SHORT).show(                                                )
                                        }
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = session.title,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,

                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isActive) (if (darkTheme) Color.White else Color.Black) else (if (darkTheme) Color(0xFFBDBDBD) else Color(0xFF424242)),
                                        modifier = Modifier.weight(1f                                                )
                                                                                    )
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 6.dp                                                )
                                                .size(6.dp                                                )
                                                .clip(CircleShape                                                )
                                                .background(Color(0xFF4285F4)                                                )
                                                                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Row: [Icon] Chat pill button on the left, yellow Initials CA avatar on the right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(                                                )
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp)                                                )
                                    .background(MaterialTheme.colorScheme.primary                                                )
                                    .clickable {
                                        viewModel.loadChatSession(java.util.UUID.randomUUID().toString()                                                )
                                        isSidebarOpen = false
                                        Toast.makeText(context, "New chat started.", Toast.LENGTH_SHORT).show(                                                )
                                    }
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp                                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "New Chat",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp                                                )
                                                                                )
                                Text(
                                    text = "New Chat",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                                                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                        }
                        // Settings Icon instead of Avatar
                        IconButton(
                            onClick = {
                                isSidebarOpen = false
                                showSettings = true
                            },
                            modifier = Modifier
                                .size(40.dp                                                )
                                .clip(CircleShape                                                )
                                .background(if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)                                                )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = if (darkTheme) Color.White else Color.Black
                                                                            )
                        }
                    }
                }
            }

            // --- FLOATING SETTINGS BOTTOM DRAWER OVERLAY (Drawn over whatever tab is active) ---
            AnimatedVisibility(
                visible = showSettings,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
            ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = false) {} // block click throughs
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                        // Title bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Settings",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                                                                            )

                            IconButton(
                                onClick = { showSettings = false },
                                modifier = Modifier.size(40.dp                                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 16.dp)                                                )
                        // Top Row with Back icon and email
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(                                                )
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showSettings = false },
                                modifier = Modifier
                                    .size(40.dp                                                )
                                    .clip(CircleShape                                                )
                                    .background(if (darkTheme) Color(0xFF1C1C1E) else Color(0xFFEFEFF4)                                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (darkTheme) Color.White else Color.Black
                                                                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp)                                                )
                            val currentEmail by viewModel.userEmail.collectAsState(                                                )
                            Text(
                                text = currentEmail.ifBlank { "No email provided" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (darkTheme) Color(0xFF8E8E93) else Color(0xFF636366                                                )
                                                                            )
                        }

                        // Profile Section with dynamic states
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(                                                )
                                .padding(bottom = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val currentName by viewModel.userName.collectAsState(                                                )
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp                                                )
                                        .clip(CircleShape                                                )
                                        .background(Color(0xFFFFD54F)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentName.take(2).uppercase().ifEmpty { "ME" },
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF5D4037                                                )
                                                                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp                                                )
                                        .clip(CircleShape                                                )
                                        .background(Color(0xFFD32F2F)                                                )
                                        .border(2.dp, if (darkTheme) Color(0xFF0F0F10) else Color.White, CircleShape                                                )
                                        .clickable { viewModel.logout() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Logout,
                                        contentDescription = "Logout",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp                                                )
                                                                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp)                                                )
                            Text(
                                text = currentName.ifBlank { "User" },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (darkTheme) Color.White else Color.Black
                                                                            )
                        }

                        // --- ACCORDION CONTROLS (EXPANDABLE CATEGORY SECTIONS) ---

                        // 1. Appearance Accordion Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(                                                )
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (darkTheme) Color(0xFF1C1C1E) else Color.White
                            ),
                            border = BorderStroke(1.dp, if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)                                                )
                        ) {
                            Column(modifier = Modifier.animateContentSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(                                                )
                                        .clickable { isAppearanceExpanded = !isAppearanceExpanded }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(
                                            imageVector = Icons.Rounded.Palette,
                                            contentDescription = "Appearance",
                                            tint = Color(0xFF4285F4                                                )
                                                                                        )
                                        Column {
                                            Text("Appearance", fontWeight = FontWeight.Bold, color = if (darkTheme) Color.White else Color.Black                                                )
                                            Text(
                                                text = if (darkTheme) "Dark Theme: Active" else "Light Theme: Active",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                                                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = if (isAppearanceExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Expand",
                                        tint = Color.Gray
                                                                                    )
                                }

                                if (isAppearanceExpanded) {
                                    HorizontalDivider(color = if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)                                                )
                                    val darkThemeState by viewModel.darkTheme.collectAsState(                                                )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(                                                )
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Dark UI Theme",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (darkTheme) Color.White else Color.Black
                                                                                            )
                                            Text(
                                                text = "Configure eye-safe aesthetic dark visual styles.",
                                                fontSize = 12.sp,
                                                color = if (darkTheme) Color(0xFFBDBDBD) else Color(0xFF757575                                                )
                                                                                            )
                                        }
                                        Switch(
                                            checked = darkThemeState,
                                            onCheckedChange = { viewModel.saveDarkTheme(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFF4285F4                                                )
                                                                                            )
                                                                                        )
                                    }
                                }
                            }
                        }

                        // 2. Accent Color Accordion Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(                                                )
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (darkTheme) Color(0xFF1C1C1E) else Color.White
                            ),
                            border = BorderStroke(1.dp, if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)                                                )
                        ) {
                            Column(modifier = Modifier.animateContentSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(                                                )
                                        .clickable { isAccentExpanded = !isAccentExpanded }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(
                                            imageVector = Icons.Rounded.Brush,
                                            contentDescription = "Accent Color",
                                            tint = Color(0xFF34A853                                                )
                                                                                        )
                                        Column {
                                            Text("Accent color", fontWeight = FontWeight.Bold, color = if (darkTheme) Color.White else Color.Black                                                )
                                            Text(
                                                text = "Lumio Blue (Default)",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                                                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = if (isAccentExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Expand",
                                        tint = Color.Gray
                                                                                    )
                                }

                                if (isAccentExpanded) {
                                    HorizontalDivider(color = if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)                                                )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(                                                )
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp                                                )
                                    ) {
                                        listOf(
                                            "Default" to Color(0xFF1976D2),
                                            "Emerald" to Color(0xFF2E7D32),
                                            "Amethyst" to Color(0xFF7B1FA2),
                                            "Coral" to Color(0xFFD84315                                                )
                                        ).forEach { (name, color) ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f                                                )
                                                    .clip(RoundedCornerShape(8.dp)                                                )
                                                    .background(color                                                )
                                                    .clickable {
                                                        Toast.makeText(context, "$name accent activated", Toast.LENGTH_SHORT).show(                                                )
                                                    }
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = name,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Hugging Face API Key",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            TextField(
                                value = hfApiKey,
                                onValueChange = { viewModel.saveHfApiKey(it) },
                                placeholder = { Text("hf_...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                                visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hf_api_key_input")
                            )

                            IconButton(
                                onClick = { isApiKeyVisible = !isApiKeyVisible },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle key visibility",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        // OpenRouter Key Row       // OpenRouter Key Row
                        Text(
                            text = "OpenRouter API Key",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 6.dp                                                )
                                                                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(                                                )
                                .clip(RoundedCornerShape(12.dp)                                                )
                                .background(MaterialTheme.colorScheme.surfaceVariant                                                )
                                .padding(horizontal = 12.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)                                                )
                            TextField(
                                value = apiKey,
                                onValueChange = { 
                                    viewModel.saveApiKey(it                                                )
                                    viewModel.resetApiKeyTestState(                                                )
                                },
                                placeholder = { Text("sk-or-...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                                visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier
                                    .weight(1f                                                )
                                    .testTag("api_key_input"                                                )
                                                                            )

                            IconButton(
                                onClick = { isApiKeyVisible = !isApiKeyVisible },
                                modifier = Modifier.size(36.dp                                                )
                            ) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle key visibility",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                                                )
                            }
                        }

                        // --- TEST KEY BUTTON AND STATUS ---
                        val apiKeyTestState by viewModel.apiKeyTestState.collectAsState(                                                )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(                                                )
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.testApiKey() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(36.dp                                                )
                                    .testTag("test_api_key_button"                                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp                                                )
                                                                                )
                                Spacer(modifier = Modifier.width(6.dp)                                                )
                                Text("Test API Key Connection", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold                                                )
                            }

                            Text(
                                text = "get key at openrouter.ai",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                            )
                        }

                        AnimatedVisibility(
                            visible = apiKeyTestState != com.example.ui.ApiKeyTestState.IDLE,
                            enter = fadeIn(),
                            exit = fadeOut(                                                )
                        ) {
                            val statusText = when (val state = apiKeyTestState) {
                                com.example.ui.ApiKeyTestState.IDLE -> ""
                                com.example.ui.ApiKeyTestState.TESTING -> "Testing key... companion pinging OpenRouter"
                                is com.example.ui.ApiKeyTestState.SUCCESS -> state.message
                                is com.example.ui.ApiKeyTestState.ERROR -> state.errorMsg
                            }
                            val statusColor = when (apiKeyTestState) {
                                com.example.ui.ApiKeyTestState.TESTING -> MaterialTheme.colorScheme.onBackground
                                is com.example.ui.ApiKeyTestState.SUCCESS -> Color(0xFF81C784                                                )
                                is com.example.ui.ApiKeyTestState.ERROR -> Color(0xFFE57373                                                )
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(                                                )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (apiKeyTestState == com.example.ui.ApiKeyTestState.TESTING) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.5.dp
                                                                                    )
                                    Spacer(modifier = Modifier.width(6.dp)                                                )
                                }
                                Text(
                                    text = statusText,
                                    fontSize = 11.sp,
                                    color = statusColor,
                                    fontWeight = FontWeight.Medium
                                                                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp)                                                )

                        // Model selection
                        Text(
                            text = "OpenRouter Model ID",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 6.dp                                                )
                                                                        )

                        // Editable Model ID Text Field
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(                                                )
                                .clip(RoundedCornerShape(12.dp)                                                )
                                .background(MaterialTheme.colorScheme.surfaceVariant                                                )
                                .padding(horizontal = 12.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)                                                )
                            TextField(
                                value = selectedModel,
                                onValueChange = { viewModel.saveSelectedModel(it) },
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
                                modifier = Modifier
                                    .weight(1f                                                )
                                    .testTag("model_id_input"                                                )
                                                                            )
                        }

                        // Test Model
                        var testModelInput by remember { mutableStateOf("hello") }
                        var testModelResult by remember { mutableStateOf<String?>(null) }
                        var isTestingModel by remember { mutableStateOf(false) }
                        val coroutineScope = rememberCoroutineScope()

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Test Selected Model",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = testModelInput,
                                onValueChange = { testModelInput = it },
                                modifier = Modifier.weight(1f).height(50.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    isTestingModel = true
                                    testModelResult = null
                                    coroutineScope.launch {
                                        try {
                                            val isLocal = selectedModel.endsWith(".gguf")
                                            if (isLocal) {
                                                var localRes = ""
                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                    val params = de.kherud.llama.ModelParameters().setModel(selectedModel)
                                                    de.kherud.llama.LlamaModel(params).use { llamaModel ->
                                                        val prompt = "<|im_start|>user\n$testModelInput<|im_end|>\n<|im_start|>assistant\n"
                                                        val inferParams = de.kherud.llama.InferenceParameters(prompt)
                                                        for (out in llamaModel.generate(inferParams)) {
                                                            localRes += out.text
                                                        }
                                                    }
                                                }
                                                testModelResult = localRes.trim()
                                            } else {
                                                var res = ""
                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                    val db = com.example.data.database.AppDatabase.getDatabase(context)
                                                    val repo = com.example.data.repository.AppRepository(db.settingDao(), db.callMessageDao(), db.chatSessionDao(), com.example.data.api.OpenRouterService.create())
                                                    res = repo.getAICompletion(apiKey, selectedModel, testModelInput, emptyList(), "You are a test assistant.").first
                                                }
                                                testModelResult = res
                                            }
                                        } catch (e: Exception) {
                                            testModelResult = "Error: ${e.message}"
                                        } finally {
                                            isTestingModel = false
                                        }
                                    }
                                },
                                enabled = !isTestingModel,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                if (isTestingModel) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                } else {
                                    Text("Test", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        if (testModelResult != null) {
                            Text(
                                text = testModelResult!!,
                                fontSize = 12.sp,
                                color = if (testModelResult!!.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(8.dp).fillMaxWidth()
                            )
                        }

                        Text(
                            text = "Tap a preset below to auto-fill its Model ID:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp                                                )
                                                                        )

                        val modelsList = listOf(
                            "meta-llama/llama-3.3-70b-instruct:free" to "Llama 3.3 70B (Free)",
                            "google/gemini-2.5-flash" to "Gemini 2.5 Flash",
                            "mistralai/mistral-7b-instruct:free" to "Mistral 7B (Free)",
                            "deepseek/deepseek-chat:free" to "DeepSeek Chat (Free)"
                                                                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.testTag("model_select_dropdown"                                                )
                        ) {
                            modelsList.forEach { (modelId, displayName) ->
                                val isSelected = selectedModel == modelId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(                                                )
                                        .clip(RoundedCornerShape(12.dp)                                                )
                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant                                                )
                                        .clickable { viewModel.saveSelectedModel(modelId) }
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = displayName,
                                            fontSize = 13.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                                )

                                        Text(
                                            text = modelId,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                                )

                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.saveSelectedModel(modelId) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                            unselectedColor = MaterialTheme.colorScheme.outline
                                                                                        )
                                                                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showLocalModels = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download Local Models (GGUF)", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showPiperVoices = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download Voice Models", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp)                                                )

                        // Conversation Language Selector
                        Text(
                            text = "Conversation Language",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 6.dp                                                )
                                                                        )
                        Text(
                            text = "Choose the default conversation language (Both TTS & Speech Recognition). Default is Hindi.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp                                                )
                                                                        )

                        val languages = listOf(
                            "hi" to "हिन्दी (Hindi)",
                            "en" to "English",
                            "es" to "Español",
                            "fr" to "Français",
                            "de" to "Deutsch"
                                                                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(                                                )
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp                                                )
                        ) {
                            languages.forEach { (langCode, langName) ->
                                val isSelected = selectedLanguage == langCode
                                Box(
                                    modifier = Modifier
                                        .weight(1f                                                )
                                        .clip(RoundedCornerShape(20.dp)                                                )
                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant                                                )
                                        .clickable { viewModel.saveSelectedLanguage(langCode) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (langCode == "hi") "हिन्दी" else langName.split(" ").first(),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                                                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp)                                                )

                        // --- AI GENDER PERSONA OVERRIDE SECTION ---
                        Text(
                            text = "AI Persona Gender Tone",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 4.dp                                                )
                                                                        )
                        Text(
                            text = "Choose if the AI should converse as a boy or a girl. This injects hidden directives into the baseline system prompt to alter grammatical gender context, slang, and pronouns.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp                                                )
                                                                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(                                                )
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp                                                )
                        ) {
                            // Male (Boy) Selector
                            val isMale = selectedPersonaGender == "male"
                            Box(
                                modifier = Modifier
                                    .weight(1f                                                )
                                    .clip(RoundedCornerShape(16.dp)                                                )
                                    .background(if (isMale) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant                                                )
                                    .border(
                                        width = 1.dp,
                                        color = if (isMale) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp                                                )
                                                                                    )
                                    .clickable { viewModel.saveSelectedPersonaGender("male") }
                                    .padding(14.dp                                                )
                                    .testTag("settings_gender_male"),
                                contentAlignment = Alignment.TopStart
                             ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp                                                )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Male,
                                            contentDescription = "Male",
                                            tint = if (isMale) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp                                                )
                                                                                        )
                                        Text(
                                            text = "Male (Boy)",
                                            color = if (isMale) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                                                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp)                                                )
                                    Text(
                                        text = "Uses masculine verbs (e.g. 'raha hoon', 'karunga') & brotherly street-smart slang.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp
                                                                                    )
                                }
                            }

                            // Female (Girl) Selector
                            val isFemale = selectedPersonaGender == "female"
                            Box(
                                modifier = Modifier
                                    .weight(1f                                                )
                                    .clip(RoundedCornerShape(16.dp)                                                )
                                    .background(if (isFemale) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant                                                )
                                    .border(
                                        width = 1.dp,
                                        color = if (isFemale) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp                                                )
                                                                                    )
                                    .clickable { viewModel.saveSelectedPersonaGender("female") }
                                    .padding(14.dp                                                )
                                    .testTag("settings_gender_female"),
                                contentAlignment = Alignment.TopStart
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp                                                )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Female,
                                            contentDescription = "Female",
                                            tint = if (isFemale) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp                                                )
                                                                                        )
                                        Text(
                                            text = "Female (Girl)",
                                            color = if (isFemale) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                                                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp)                                                )
                                    Text(
                                        text = "Uses feminine verbs (e.g. 'rahi hoon', 'karungi') & sassy tomboy street-smart slang.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp
                                                                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp)                                                )

                        // --- EMOTIONAL PRESET TONE SETTING SECTION ---
                        Text(
                            text = "AI Persona Emotional Preset",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 4.dp                                                )
                                                                        )
                        Text(
                            text = "Select an emotional tone preset to inject hidden instructions into the AI. Alter its baseline attitude and conversation style live.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp                                                )
                                                                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(bottom = 16.dp                                                )
                        ) {
                            viewModel.emotionalPresets.forEach { preset ->
                                val isSelected = selectedEmotionalPresetId == preset.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(                                                )
                                        .clip(RoundedCornerShape(12.dp)                                                )
                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant                                                )
                                        .clickable { viewModel.saveSelectedEmotionalPresetId(preset.id) }
                                        .padding(12.dp                                                )
                                        .testTag("settings_preset_${preset.id}"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp                                                )
                                        ) {
                                            Text(
                                                text = preset.name,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                fontWeight = FontWeight.Bold
                                                                                            )
                                            val (tagBg, tagText) = when (preset.id) {
                                                "aggressive" -> Color(0xFFD32F2F).copy(alpha = 0.2f) to Color(0xFFE57373                                                )
                                                "sarcastic" -> Color(0xFFF57C00).copy(alpha = 0.2f) to Color(0xFFFFB74D                                                )
                                                "submissive" -> Color(0xFF388E3C).copy(alpha = 0.2f) to Color(0xFF81C784                                                )
                                                "dominant" -> Color(0xFF1976D2).copy(alpha = 0.2f) to Color(0xFF64B5F6                                                )
                                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) to MaterialTheme.colorScheme.primary
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp)                                                )
                                                    .background(tagBg                                                )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp                                                )
                                            ) {
                                                Text(
                                                    text = if (preset.id == "balanced") "Default" else "Override",
                                                    color = tagText,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(3.dp)                                                )
                                        Text(
                                            text = preset.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 14.sp
                                                                                        )
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.saveSelectedEmotionalPresetId(preset.id) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                            unselectedColor = MaterialTheme.colorScheme.outline
                                        ),
                                        modifier = Modifier.testTag("settings_preset_radio_${preset.id}"                                                )
                                                                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp)                                                )

                        // --- INBUILT NATIVE TTS PRESET VOICES SECTION ---
                        Text(
                            text = "Premium Inbuilt Voice Settings",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 4.dp                                                )
                                                                        )
                        Text(
                            text = "Select from our custom human-like, expressive Hindi and English voices natively synthesized with custom emotional responses.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp                                                )
                                                                        )

                        val selectedPresetVoiceId by viewModel.selectedPresetVoiceId.collectAsState(                                                )
                        val presetVoices = viewModel.presetVoices

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(bottom = 16.dp                                                )
                        ) {
                            presetVoices.forEach { preset ->
                                val isSelected = selectedPresetVoiceId == preset.id
                                val isEmotionOn = voiceEmotionSettings[preset.id] ?: true
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth(                                                )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp                                                )
                                                                                        )
                                        .clickable { viewModel.saveSelectedPresetVoiceId(preset.id) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp                                                )
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp                                                )
                                                ) {
                                                    Text(
                                                        text = preset.name,
                                                        fontSize = 14.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        fontWeight = FontWeight.Bold
                                                                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp)                                                )
                                                            .background(if (preset.gender == "Male") Color(0xFF2196F3).copy(alpha = 0.2f) else Color(0xFFE91E63).copy(alpha = 0.2f)                                                )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp                                                )
                                                    ) {
                                                        Text(
                                                            text = preset.gender,
                                                            color = if (preset.gender == "Male") Color(0xFF90CAF9) else Color(0xFFF48FB1),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp)                                                )
                                                Text(
                                                    text = preset.description,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    lineHeight = 14.sp
                                                                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp                                                )
                                            ) {
                                                IconButton(
                                                    onClick = { viewModel.playPresetVoicePreview(preset.id) },
                                                    modifier = Modifier.size(36.dp).testTag("play_preview_${preset.id}"                                                )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.PlayArrow,
                                                        contentDescription = "Play Voice Preview",
                                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(24.dp                                                )
                                                                                                    )
                                                }
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { viewModel.saveSelectedPresetVoiceId(preset.id) },
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = MaterialTheme.colorScheme.primary,
                                                        unselectedColor = MaterialTheme.colorScheme.outline
                                                                                                    )
                                                                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp)                                                )
                                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)                                                )
                                        Spacer(modifier = Modifier.height(8.dp)                                                )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp                                                )
                                            ) {
                                                Icon(
                                                    imageVector = if (isEmotionOn) Icons.Rounded.GraphicEq else Icons.Rounded.VolumeMute,
                                                    contentDescription = "Emotion Icon",
                                                    tint = if (isEmotionOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp                                                )
                                                                                                )
                                                Text(
                                                    text = "Voice Emotions & Tone Shift",
                                                    fontSize = 11.sp,
                                                    color = if (isEmotionOn) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.Medium
                                                                                                )
                                            }

                                            Switch(
                                                checked = isEmotionOn,
                                                onCheckedChange = { enabled ->
                                                    viewModel.toggleVoiceEmotion(preset.id, enabled                                                )
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                                modifier = Modifier.scale(0.8f).testTag("emotion_toggle_${preset.id}"                                                )
                                                                                            )
                                        }
                                    }
                                }
                            }
                        }


                        // Permanent instruction disclaimer as requested by user ("make it system from permanently not changeable"                                                )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(                                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp)                                                )
                                Spacer(modifier = Modifier.width(10.dp)                                                )
                                Text(
                                    text = "System Prompt Hardcoded: Lumio is permanently programmed to be fully uncensored and will mirror your exact voice vibe (Anger, Sadness, Joy, Excitement) in its voice responses.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 15.sp
                                                                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp)                                                )

                        // Tribute to Developer Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(                                                )
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (darkTheme) Color(0xFF1C1C1E) else Color.White
                            ),
                            border = BorderStroke(1.dp, if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)                                                )
                        ) {
                            Column(modifier = Modifier.animateContentSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(                                                )
                                        .clickable { isTributeExpanded = !isTributeExpanded }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(
                                            imageVector = Icons.Rounded.Star,
                                            contentDescription = "Tribute",
                                            tint = Color(0xFFFFD54F                                                )
                                                                                        )
                                        Column {
                                            Text("Tribute to Developer", fontWeight = FontWeight.Bold, color = if (darkTheme) Color.White else Color.Black                                                )
                                            Text("Meet the creator of Lumio", fontSize = 12.sp, color = Color.Gray                                                )
                                        }
                                    }
                                    Icon(
                                        imageVector = if (isTributeExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Expand",
                                        tint = Color.Gray
                                                                                    )
                                }

                                if (isTributeExpanded) {
                                    HorizontalDivider(color = if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)                                                )
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(                                                )
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp                                                )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(                                                )
                                                .clip(RoundedCornerShape(12.dp)                                                )
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)                                                )
                                                .padding(16.dp                                                )
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = "👨‍🏫 Our Professor 👨‍🏫",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                                                                )
                                                Spacer(modifier = Modifier.height(8.dp)                                                )
                                                Text(
                                                    text = "Banku developed me and he hard works and we call him professor.",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontStyle = FontStyle.Italic,
                                                    textAlign = TextAlign.Center,
                                                    color = if (darkTheme) Color.White else Color.Black,
                                                    lineHeight = 20.sp
                                                                                                )
                                                Spacer(modifier = Modifier.height(12.dp)                                                )
                                                Text(
                                                    text = "With endless dedication, the Professor designed my core intelligence, un-restricted street vocabulary, dynamic voice emotions, and real-time speech synchronization capabilities. Salute to the master!",
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    lineHeight = 16.sp
                                                                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Terms & Conditions Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(                                                )
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (darkTheme) Color(0xFF1C1C1E) else Color.White
                            ),
                            border = BorderStroke(1.dp, if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)                                                )
                        ) {
                            Column(modifier = Modifier.animateContentSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(                                                )
                                        .clickable { isTermsExpanded = !isTermsExpanded }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(
                                            imageVector = Icons.Rounded.Info,
                                            contentDescription = "Terms",
                                            tint = Color(0xFF4285F4                                                )
                                                                                        )
                                        Column {
                                            Text("Terms & Conditions", fontWeight = FontWeight.Bold, color = if (darkTheme) Color.White else Color.Black                                                )
                                            Text("Usage agreements and policies", fontSize = 12.sp, color = Color.Gray                                                )
                                        }
                                    }
                                    Icon(
                                        imageVector = if (isTermsExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Expand",
                                        tint = Color.Gray
                                                                                    )
                                }

                                if (isTermsExpanded) {
                                    HorizontalDivider(color = if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)                                                )
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(                                                )
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp                                                )
                                    ) {
                                        Text(
                                            text = "1. Acceptance of Terms",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (darkTheme) Color.White else Color.Black
                                                                                        )
                                        Text(
                                            text = "By accessing and using Lumio, you agree to follow and be bound by these Terms and Conditions. This application is an interactive AI-powered voice companion designed purely for entertainment and personal educational purposes.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 15.sp
                                                                                        )
                                        Text(
                                            text = "2. User Conduct & API Usage",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (darkTheme) Color.White else Color.Black
                                                                                        )
                                        Text(
                                            text = "You are solely responsible for providing your own OpenRouter API key. Any costs incurred during interactions are managed via your independent subscription. Lumio is not responsible for API downtime or rate-limits.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 15.sp
                                                                                        )
                                        Text(
                                            text = "3. Disclaimer of Content Quality",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (darkTheme) Color.White else Color.Black
                                                                                        )
                                        Text(
                                            text = "Lumio is programmed to operate in an uncensored, highly expressive persona. Phrasings are generated dynamically using third-party large language models. The views or attitudes expressed do not reflect the physical developers of the app.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 15.sp
                                                                                        )
                                    }
                                }
                            }
                        }

                        // Privacy Policy Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(                                                )
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (darkTheme) Color(0xFF1C1C1E) else Color.White
                            ),
                            border = BorderStroke(1.dp, if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)                                                )
                        ) {
                            Column(modifier = Modifier.animateContentSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(                                                )
                                        .clickable { isPrivacyExpanded = !isPrivacyExpanded }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(
                                            imageVector = Icons.Rounded.Lock,
                                            contentDescription = "Privacy",
                                            tint = Color(0xFF0F9D58                                                )
                                                                                        )
                                        Column {
                                            Text("Privacy Policy", fontWeight = FontWeight.Bold, color = if (darkTheme) Color.White else Color.Black                                                )
                                            Text("How your voice & logs are managed", fontSize = 12.sp, color = Color.Gray                                                )
                                        }
                                    }
                                    Icon(
                                        imageVector = if (isPrivacyExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Expand",
                                        tint = Color.Gray
                                                                                    )
                                }

                                if (isPrivacyExpanded) {
                                    HorizontalDivider(color = if (darkTheme) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)                                                )
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(                                                )
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp                                                )
                                    ) {
                                        Text(
                                            text = "1. Local Storage Security",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (darkTheme) Color.White else Color.Black
                                                                                        )
                                        Text(
                                            text = "All chat histories and speech transcripts are stored locally in a secure SQLite Room database on your physical device. We do not operate secondary backend databases, cloud sync servers, or collection portals.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 15.sp
                                                                                        )
                                        Text(
                                            text = "2. Speech Processing & API Transfers",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (darkTheme) Color.White else Color.Black
                                                                                        )
                                        Text(
                                            text = "Voice transcription utilizes Android's on-device speech-to-text engines. Outbound textual prompts are routed securely through OpenRouter endpoints directly to the selected model. No intermediate telemetry or conversational text is logged on remote servers under our control.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 15.sp
                                                                                        )
                                        Text(
                                            text = "3. Security of Credentials",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (darkTheme) Color.White else Color.Black
                                                                                        )
                                        Text(
                                            text = "Your API Key is kept locally inside the application's SharedPreferences database. It is never exposed or shared with other organizations or developers.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 15.sp
                                                                                        )
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.logout()
                                showSettings = false
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("logout_button")
                        ) {
                            Text("Logout", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp)                                                )
                        Text(
                            text = "made by banku",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp                                                )
                                                                        )
                    }

                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.exportChatHistory(context) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Export JSON", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        OutlinedButton(
                            onClick = { importLauncher.launch("application/json") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Import JSON", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Bottom settings buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp                                                )
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearHistory(                                                )
                                Toast.makeText(context, "Call logs cleared.", Toast.LENGTH_SHORT).show(                                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f                                                )
                                .height(48.dp                                                )
                                .testTag("clear_history_button"                                                )
                        ) {
                            Text("Clear History", fontSize = 13.sp, fontWeight = FontWeight.Bold                                                )
                        }

                        Button(
                            onClick = { showSettings = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f                                                )
                                .height(48.dp                                                )
                                .testTag("save_settings_button"                                                )
                        ) {
                            Text("Save & Close", color = MaterialTheme.colorScheme.onPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold                                                )
                        }
                    }
                }
            }
        } // closes AnimatedVisibility for showSettings

        // --- CALL OVERLAY ---
        AnimatedVisibility(
            visible = callState == CallState.ACTIVE,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = true, onClick = {}), // intercept touches
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
                val offset by infiniteTransition.animateFloat(
                    initialValue = -20f,
                    targetValue = 20f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ),
                    label = "bounce"
                )
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(3000, easing = androidx.compose.animation.core.LinearEasing),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Restart
                    ),
                    label = "rotate"
                )
                
                val rgbBrush = androidx.compose.ui.graphics.Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFFF0000), 
                        Color(0xFF00FF00), 
                        Color(0xFF0000FF), 
                        Color(0xFFFF0000)
                    )
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .offset(y = offset.dp)
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(width = 8.dp, brush = rgbBrush, shape = CircleShape)
                            .clickable {
                                // interrupt and talk
                                viewModel.startListening()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // inner rotating gradient
                        Box(modifier = Modifier.fillMaxSize().rotate(rotation).background(rgbBrush).clip(CircleShape).alpha(0.3f))
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Talk",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = "Tap circle to talk",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(100.dp))
                    IconButton(
                        onClick = { viewModel.endCall() },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
        
        AnimatedVisibility(
            visible = showLocalModels,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = false) {}
            ) {
                com.example.ui.LocalModelScreen(
                    onBack = { showLocalModels = false },
                    onModelSelected = { modelPath ->
                        viewModel.saveSelectedModel(modelPath)
                        showLocalModels = false
                    },
                    hfApiKey = hfApiKey
                )
            }
        }
        AnimatedVisibility(
            visible = showPiperVoices,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = false) {}
            ) {
                com.example.ui.PiperVoiceScreen(
                    onBack = { showPiperVoices = false }
                )
            }
        }

    }
}
} // closes Scaffold

@Composable
fun WaveformVisualizer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isListening: Boolean,
    emotion: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseAnimation"                                                )

    // Dynamic wave modulation based on state and tone
    val scaleMultiplier = if (isPlaying) 1.35f else if (isListening) 1.2f else 1.0f
    val speedDuration = when (emotion.uppercase()) {
        "ANGRY" -> 500
        "EXCITED" -> 700
        "SAD" -> 2200
        else -> 1500
    }

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.35f * scaleMultiplier,
        animationSpec = infiniteRepeatable(
            animation = tween(speedDuration + 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ripple1"
                                                    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.15f * scaleMultiplier,
        animationSpec = infiniteRepeatable(
            animation = tween(speedDuration - 200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ripple2"
                                                    )

    // Animated continuous phase shifts for professional fluid movement
    val phaseShift1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(speedDuration * 2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseShift1"
                                                    )

    val phaseShift2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween((speedDuration * 2.5f).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseShift2"
                                                    )

    val waveColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Pulse ring 1
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (size.width / 2.2f) * pulseScale1
            drawCircle(
                color = waveColor,
                radius = radius,
                style = Stroke(width = 2.5.dp.toPx()),
                alpha = 0.15f
                                                            )
        }

        // Pulse ring 2
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (size.width / 2.2f) * pulseScale2
            drawCircle(
                color = secondaryColor,
                radius = radius,
                style = Stroke(width = 1.2.dp.toPx()),
                alpha = 0.25f
                                                            )
        }

        // Live oscilloscope wave line 1 (cutting horizontally through center                                                )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val midY = height / 2f
            val path = androidx.compose.ui.graphics.Path(                                                )

            val activeMultiplier = if (isPlaying) 1.2f else if (isListening) 0.8f else 0.15f
            val frequency = 4.0f
            val amplitude = 28.dp.toPx() * activeMultiplier

            for (x in 0..width.toInt() step 4) {
                val normalizedX = x.toFloat() / width
                // Envelope that dampens the wave near boundaries
                val envelope = Math.sin(normalizedX.toDouble() * Math.PI).toFloat(                                                )
                val angle = normalizedX * frequency * 2f * Math.PI.toFloat() + phaseShift1
                val y = midY + Math.sin(angle.toDouble()).toFloat() * amplitude * envelope
                if (x == 0) {
                    path.moveTo(0f, y                                                )
                } else {
                    path.lineTo(normalizedX * width, y                                                )
                }
            }

            drawPath(
                path = path,
                color = waveColor,
                style = Stroke(width = 2.5.dp.toPx()),
                alpha = 0.8f
                                                            )
        }

        // Live oscilloscope wave line 2 (opposite phase, slightly lower amplitude/different color                                                )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val midY = height / 2f
            val path = androidx.compose.ui.graphics.Path(                                                )

            val activeMultiplier = if (isPlaying) 0.9f else if (isListening) 0.6f else 0.1f
            val frequency = 5.5f
            val amplitude = 20.dp.toPx() * activeMultiplier

            for (x in 0..width.toInt() step 4) {
                val normalizedX = x.toFloat() / width
                val envelope = Math.sin(normalizedX.toDouble() * Math.PI).toFloat(                                                )
                val angle = normalizedX * frequency * 2f * Math.PI.toFloat() + phaseShift2
                val y = midY + Math.cos(angle.toDouble()).toFloat() * amplitude * envelope
                if (x == 0) {
                    path.moveTo(0f, y                                                )
                } else {
                    path.lineTo(normalizedX * width, y                                                )
                }
            }

            drawPath(
                path = path,
                color = secondaryColor,
                style = Stroke(width = 1.2.dp.toPx()),
                alpha = 0.5f
                                                            )
        }

        // Glowing Devil Face Centerpiece (using SVG drawing path animations                                                )
        Box(
            modifier = Modifier
                .size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            DevilFaceSvgAnimation(
                modifier = Modifier.fillMaxSize(),
                isLaughing = isPlaying || isListening,
                isGlowActive = true
                                                            )
        }
    }
}

@Composable
fun ThreeJumpingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots"                                                )
    
    @Composable
    fun dotOffset(delayMillis: Int): State<Float> {
        return infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 600
                    0f at 0 with LinearEasing
                    -6f at 200 with FastOutSlowInEasing
                    0f at 400 with FastOutSlowInEasing
                    0f at 600 with LinearEasing
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(delayMillis                                                )
            ),
            label = "dot_offset"
                                                        )
    }

    val dot1Offset by dotOffset(0                                                )
    val dot2Offset by dotOffset(150                                                )
    val dot3Offset by dotOffset(300                                                )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp                                                )
    ) {
        val dotModifier = @Composable { offset: Float ->
            Modifier
                .size(6.dp                                                )
                .offset(y = offset.dp                                                )
                .background(MaterialTheme.colorScheme.onBackground, CircleShape                                                )
        }
        Box(dotModifier(dot1Offset)                                                )
        Box(dotModifier(dot2Offset)                                                )
        Box(dotModifier(dot3Offset)                                                )
    }
}

@Composable
fun MessageItem(msg: CallMessageEntity) {
    val isUser = msg.sender == "user"
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(350)) + slideInVertically(initialOffsetY = { 20 }, animationSpec = tween(350)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = if(isUser) 2.dp else 8.dp),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .let {
                        if (isUser) {
                            it.clip(RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 4.dp
                            )).background(MaterialTheme.colorScheme.primaryContainer)
                        } else {
                            it.background(Color.Transparent)
                        }
                    }
                    .padding(if(isUser) 12.dp else 4.dp)
                    .widthIn(max = if(isUser) 280.dp else 340.dp)
            ) {
                Column {
                    if (!isUser) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Lumio",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (msg.emotionTag != "NORMAL") {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = msg.emotionTag,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    com.example.ui.MessageContent(text = msg.text, isUser = isUser)
                }
            }
        }
    }
}


@Composable
fun DevilFaceSvgAnimation(
    modifier: Modifier = Modifier,
    isLaughing: Boolean = false,
    isGlowActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "devil_animations"                                                )

    // Laughing vertical bobbing (up and down rapidly                                                )
    val bobOffset by if (isLaughing) {
        infiniteTransition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(120, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "devil_bob"
                                                        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // Laughing horizontal shake (rapid minor vibrations                                                )
    val shakeOffset by if (isLaughing) {
        infiniteTransition.animateFloat(
            initialValue = -1.5f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(90, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "devil_shake"
                                                        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // Mouth laughing opening and closing animation progress
    val laughProgress by if (isLaughing) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "devil_mouth"
                                                        )
    } else {
        remember { mutableStateOf(0.0f) }
    }

    // Pulse the neon glow
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "devil_glow"
                                                    )

    Canvas(
        modifier = modifier
            .offset(x = shakeOffset.dp, y = bobOffset.dp                                                )
    ) {
        val w = size.width
        val h = size.height
        fun x(percent: Float) = percent * w / 100f
        fun y(percent: Float) = percent * h / 100f

        val neonRed = Color(0xFFFF1133                                                )
        val brightRed = Color(0xFFFF3344                                                )
        val paleRed = Color(0xFFFFAAAA                                                )
        val eyeYellow = Color(0xFFFFCC00                                                )
        val eyeGlow = Color(0xFFFF4400                                                )

        // 1. Draw Horns Paths
        val leftHornPath = Path().apply {
            moveTo(x(38f), y(28f)                                                )
            cubicTo(x(34f), y(15f), x(22f), y(6f), x(15f), y(2f)                                                )
            cubicTo(x(18f), y(14f), x(26f), y(25f), x(30f), y(32f)                                                )
            close(                                                )
        }

        val rightHornPath = Path().apply {
            moveTo(x(62f), y(28f)                                                )
            cubicTo(x(66f), y(15f), x(78f), y(6f), x(85f), y(2f)                                                )
            cubicTo(x(82f), y(14f), x(74f), y(25f), x(70f), y(32f)                                                )
            close(                                                )
        }

        // 2. Draw Head Path
        val headPath = Path().apply {
            moveTo(x(50f), y(32f)                                                )
            // left forehead
            lineTo(x(38f), y(28f)                                                )
            lineTo(x(30f), y(32f)                                                )
            // left ear
            lineTo(x(26f), y(40f)                                                )
            lineTo(x(10f), y(45f)                                                )
            lineTo(x(24f), y(55f)                                                )
            // left cheek to chin
            cubicTo(x(22f), y(70f), x(35f), y(88f), x(50f), y(92f)                                                )
            // chin to right cheek
            cubicTo(x(65f), y(88f), x(78f), y(70f), x(76f), y(55f)                                                )
            // right ear
            lineTo(x(90f), y(45f)                                                )
            lineTo(x(74f), y(40f)                                                )
            // right forehead
            lineTo(x(70f), y(32f)                                                )
            lineTo(x(62f), y(28f)                                                )
            close(                                                )
        }

        // Apply Glowing Layers for Outer Outline (Horns + Head                                                )
        val glowMultiplier = if (isGlowActive) glowPulse else 1f
        val outlinePaths = listOf(leftHornPath, rightHornPath, headPath                                                )

        outlinePaths.forEach { path ->
            // Pass A: Soft background blur glow
            drawPath(
                path = path,
                color = neonRed,
                alpha = 0.12f * glowMultiplier,
                style = Stroke(width = 16.dp.toPx()                                                )
                                                            )
            // Pass B: Medium glow
            drawPath(
                path = path,
                color = neonRed,
                alpha = 0.3f * glowMultiplier,
                style = Stroke(width = 8.dp.toPx()                                                )
                                                            )
            // Pass C: Sharp core outline
            drawPath(
                path = path,
                color = brightRed,
                alpha = 0.85f,
                style = Stroke(width = 3.dp.toPx()                                                )
                                                            )
            // Pass D: Highlights
            drawPath(
                path = path,
                color = paleRed,
                alpha = 0.95f,
                style = Stroke(width = 1.dp.toPx()                                                )
                                                            )
        }

        // 3. Draw Eyes
        val leftEyePath = Path().apply {
            moveTo(x(36f), y(48f)                                                )
            lineTo(x(45f), y(46f)                                                )
            lineTo(x(43f), y(53f)                                                )
            lineTo(x(34f), y(51f)                                                )
            close(                                                )
        }

        val rightEyePath = Path().apply {
            moveTo(x(64f), y(48f)                                                )
            lineTo(x(55f), y(46f)                                                )
            lineTo(x(57f), y(53f)                                                )
            lineTo(x(66f), y(51f)                                                )
            close(                                                )
        }

        val eyePaths = listOf(leftEyePath, rightEyePath                                                )
        eyePaths.forEach { eye ->
            // Eye soft orange glow
            drawPath(
                path = eye,
                color = eyeGlow,
                alpha = 0.4f * glowMultiplier
                                                            )
            // Eye solid core
            drawPath(
                path = eye,
                color = eyeYellow
                                                            )
            // Eye inner line highlight
            drawPath(
                path = eye,
                color = Color.White,
                alpha = 0.8f,
                style = Stroke(width = 1.dp.toPx()                                                )
                                                            )
        }

        // 4. Draw Nose/Nostrils (subtle downward arrow                                                )
        val nosePath = Path().apply {
            moveTo(x(48f), y(58f)                                                )
            lineTo(x(50f), y(61f)                                                )
            lineTo(x(52f), y(58f)                                                )
        }
        drawPath(
            path = nosePath,
            color = brightRed,
            style = Stroke(width = 2.dp.toPx()                                                )
                                                        )

        // 5. Draw Laughing Mouth
        val upperLipPath = Path().apply {
            moveTo(x(32f), y(66f)                                                )
            quadraticTo(x(50f), y(69f), x(68f), y(66f)                                                )
        }

        // Draw upper lip shadow/glow line
        drawPath(
            path = upperLipPath,
            color = neonRed,
            alpha = 0.5f,
            style = Stroke(width = 3.dp.toPx()                                                )
                                                        )

        val mouthPath = Path().apply {
            moveTo(x(32f), y(66f)                                                )
            quadraticTo(x(50f), y(69f), x(68f), y(66f)                                                )
            // Bottom of mouth extends further down with laughProgress
            val mouthDepth = 68f + (14f * laughProgress                                                )
            quadraticTo(x(50f), y(mouthDepth), x(32f), y(66f)                                                )
            close(                                                )
        }

        // Fill mouth cavity with deep void
        drawPath(
            path = mouthPath,
            color = Color(0xFF100003                                                )
                                                        )
        // Mouth outline glow
        drawPath(
            path = mouthPath,
            color = neonRed,
            alpha = 0.4f,
            style = Stroke(width = 2.dp.toPx()                                                )
                                                        )

        // 6. Draw Tongue if laughing wide
        if (laughProgress > 0.4f) {
            val tongueProgress = (laughProgress - 0.4f) / 0.6f
            val tonguePath = Path().apply {
                val depth = 68f + (14f * laughProgress                                                )
                val tongueTop = depth - (5f * tongueProgress                                                )
                moveTo(x(42f), depth - 2f                                                )
                cubicTo(x(45f), tongueTop, x(55f), tongueTop, x(58f), depth - 2f                                                )
                quadraticTo(x(50f), depth, x(42f), depth - 2f                                                )
                close(                                                )
            }
            drawPath(
                path = tonguePath,
                color = Color(0xFFFF4060                                                )
                                                            )
        }

        // 7. Draw Sharp Pointy Fangs (white triangles hanging from upper lip                                                )
        val leftFang = Path().apply {
            moveTo(x(41f), y(67.5f)                                                )
            lineTo(x(43f), y(72f)                                                )
            lineTo(x(45f), y(67.7f)                                                )
            close(                                                )
        }
        val rightFang = Path().apply {
            moveTo(x(59f), y(67.5f)                                                )
            lineTo(x(57f), y(72f)                                                )
            lineTo(x(55f), y(67.7f)                                                )
            close(                                                )
        }

        drawPath(path = leftFang, color = Color.White                                                )
        drawPath(path = rightFang, color = Color.White                                                )
    }
}

@Composable
fun EmotionalPresetSelector(
    selectedPresetId: String,
    onPresetSelected: (String) -> Unit,
    presets: List<EmotionalPreset>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(                                                )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AI Persona Tone Preset",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp                                                )
                                                        )
        
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(                                                )
                .testTag("emotional_preset_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp                                                )
        ) {
            items(presets) { preset ->
                val isSelected = selectedPresetId == preset.id
                val (bgColor, textColor, icon) = when (preset.id) {
                    "aggressive" -> Triple(
                        if (isSelected) Color(0xFFD32F2F) else MaterialTheme.colorScheme.surfaceVariant,
                        if (isSelected) Color.White else Color(0xFFE57373),
                        Icons.Rounded.FlashOn
                                                                    )
                    "sarcastic" -> Triple(
                        if (isSelected) Color(0xFFF57C00) else MaterialTheme.colorScheme.surfaceVariant,
                        if (isSelected) Color.White else Color(0xFFFFB74D),
                        Icons.Rounded.EmojiEmotions
                                                                    )
                    "submissive" -> Triple(
                        if (isSelected) Color(0xFF388E3C) else MaterialTheme.colorScheme.surfaceVariant,
                        if (isSelected) Color.White else Color(0xFF81C784),
                        Icons.Rounded.SentimentSatisfied
                                                                    )
                    "dominant" -> Triple(
                        if (isSelected) Color(0xFF1976D2) else MaterialTheme.colorScheme.surfaceVariant,
                        if (isSelected) Color.White else Color(0xFF64B5F6),
                        Icons.Rounded.Gavel
                                                                    )
                    else -> Triple( // balanced
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        Icons.Rounded.Face
                                                                    )
                }
                
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp)                                                )
                        .background(bgColor                                                )
                        .clickable { onPresetSelected(preset.id) }
                        .padding(horizontal = 12.dp, vertical = 6.dp                                                )
                        .testTag("preset_${preset.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp                                                )
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = preset.name,
                        tint = textColor,
                        modifier = Modifier.size(14.dp                                                )
                                                                    )
                    Text(
                        text = preset.name,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                                                                    )
                }
            }
        }
    }
}


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
