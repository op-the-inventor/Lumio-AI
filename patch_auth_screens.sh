#!/bin/bash
sed -i '/fun LumioAuthScreen(viewModel: CallViewModel) {/a \    val error by viewModel.error.collectAsState()\n    val context = LocalContext.current\n    LaunchedEffect(error) {\n        error?.let {\n            Toast.makeText(context, it, Toast.LENGTH_LONG).show()\n        }\n    }' app/src/main/java/com/example/ui/AuthScreens.kt
