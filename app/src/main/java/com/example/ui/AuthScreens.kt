package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.border

val PurplePrimary = Color(0xFF8B5CF6)
val DarkBackground = Color(0xFF16161A)
val LightBackground = Color(0xFFF8F9FA)

@Composable
fun LumioLogo(isDark: Boolean = true, modifier: Modifier = Modifier) {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = if (isDark) Color.White else Color.Black, fontWeight = FontWeight.Bold)) {
                append("lumio ")
            }
            withStyle(style = SpanStyle(color = PurplePrimary, fontWeight = FontWeight.Bold)) {
                append("ai")
            }
        },
        fontSize = 20.sp,
        modifier = modifier
    )
}

@Composable
fun AnimatedOrb() {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PurplePrimary.copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.width / 2f
                ),
                radius = (size.minDimension / 2) * pulse
            )
            drawCircle(
                color = Color(0xFF0F0F12),
                radius = (size.minDimension / 3)
            )
            // Eyes
            drawRoundRect(
                color = PurplePrimary,
                topLeft = Offset(size.width / 2 - 30f, size.height / 2 - 10f),
                size = androidx.compose.ui.geometry.Size(15f, 25f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = PurplePrimary,
                topLeft = Offset(size.width / 2 + 15f, size.height / 2 - 10f),
                size = androidx.compose.ui.geometry.Size(15f, 25f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )
        }
    }
}

@Composable
fun LandingScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        LumioLogo(isDark = true, modifier = Modifier.padding(top = 24.dp))
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = buildAnnotatedString {
                append("Your ")
                withStyle(style = SpanStyle(color = PurplePrimary)) {
                    append("AI\n")
                }
                append("with\nNo Restrictions")
            },
            fontSize = 42.sp,
            lineHeight = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Ask anything. Do anything.\nPowered by advanced AI.",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f),
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AnimatedOrb()
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Get Started Button
        SwipeToStartButton(onGetStarted = onGetStarted)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.15f), ambientColor = Color.Black.copy(alpha = 0.1f))
                .background(Color.White, RoundedCornerShape(16.dp))
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                ),
                singleLine = true,
                visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                trailingIcon = if (isPassword) {
                    {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = Color.Gray
                            )
                        }
                    }
                } else null
            )
        }
    }
}
@Composable
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
}
@Composable
fun LoginContent(
    viewModel: CallViewModel,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LumioLogo(isDark = false)
        
        Spacer(modifier = Modifier.height(32.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome back!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Login to your lumio ai account",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )
                
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "johndoe@email.com"
                )
                
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "••••••••",
                    isPassword = true
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(checkedColor = PurplePrimary)
                    )
                    Text(
                        text = "Remember me",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Forgot password?",
                        fontSize = 12.sp,
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { /* Handle forgot password */ }
                    )
                }
                
                Button3D(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.loginWithEmail("", email.trim(), password.trim())
                        } else {
                            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                        }
                    },
                    text = "Login",
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Or Sign in with",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                SocialButtons { provider ->
                    viewModel.loginWithProvider(provider, context)
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Don't have an account? ", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        text = "Register",
                        fontSize = 14.sp,
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterContent(
    viewModel: CallViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Spacer(modifier = Modifier.weight(1f))
            LumioLogo(isDark = false)
            Spacer(modifier = Modifier.weight(1.5f)) // Balance the layout
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create your account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Get started with lumio ai",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )
                
                AuthTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name",
                    placeholder = "John Doe"
                )
                
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "johndoe@email.com"
                )
                
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "••••••••",
                    isPassword = true
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agreeToTerms,
                        onCheckedChange = { agreeToTerms = it },
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(checkedColor = PurplePrimary)
                    )
                    Text(
                        text = "I agree to the ",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        text = "Terms of Service",
                        fontSize = 12.sp,
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button3D(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank() && name.isNotBlank() && agreeToTerms) {
                            viewModel.loginWithEmail(name.trim(), email.trim(), password.trim())
                        } else {
                            Toast.makeText(context, "Please fill all fields and agree to terms", Toast.LENGTH_SHORT).show()
                        }
                    },
                    text = "Create account",
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Or Sign up with",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                SocialButtons { provider ->
                    viewModel.loginWithProvider(provider, context)
                }
            }
        }
    }
}

@Composable
fun LumioAuthScreen(viewModel: CallViewModel) {
    var currentScreen by remember { mutableStateOf("landing") }
    
    Crossfade(targetState = currentScreen, label = "auth_crossfade") { screen ->
        when (screen) {
            "landing" -> LandingScreen(onGetStarted = { currentScreen = "login" })
            "login" -> LoginContent(
                viewModel = viewModel,
                onNavigateToRegister = { currentScreen = "register" }
            )
            "register" -> RegisterContent(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = "login" }
            )
        }
    }
}

@Composable
fun SwipeToStartButton(onGetStarted: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var componentWidth by remember { mutableStateOf(0f) }
    val thumbSize = 48.dp
    val thumbSizePx = with(androidx.compose.ui.platform.LocalDensity.current) { thumbSize.toPx() }
    val paddingPx = with(androidx.compose.ui.platform.LocalDensity.current) { 8.dp.toPx() }
    
    val maxDrag = (componentWidth - thumbSizePx - paddingPx * 2).coerceAtLeast(0f)
    
    val animOffsetX = remember { Animatable(0f) }

    LaunchedEffect(animOffsetX.value, maxDrag) {
        if (maxDrag > 0 && animOffsetX.value >= maxDrag * 0.95f) {
            onGetStarted()
            animOffsetX.snapTo(0f)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .onSizeChanged { componentWidth = it.width.toFloat() },
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Get Started",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Row(
                modifier = Modifier.fillMaxSize().padding(end = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ">>>",
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Box(
                modifier = Modifier
                    .offset { IntOffset(animOffsetX.value.roundToInt(), 0) }
                    .size(thumbSize)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .pointerInput(maxDrag) {
                        detectDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (animOffsetX.value < maxDrag * 0.95f) {
                                        animOffsetX.animateTo(0f, tween(300))
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    animOffsetX.animateTo(0f, tween(300))
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val newOffset = (animOffsetX.value + dragAmount.x).coerceIn(0f, maxDrag)
                                animOffsetX.snapTo(newOffset)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Start",
                    tint = DarkBackground
                )
            }
        }
    }
}

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
