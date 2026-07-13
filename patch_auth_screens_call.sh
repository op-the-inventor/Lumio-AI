#!/bin/bash
sed -i 's/viewModel.loginWithEmail("", email.trim(), password.trim())/viewModel.signInWithEmail(email.trim(), password.trim())/g' app/src/main/java/com/example/ui/AuthScreens.kt
sed -i 's/viewModel.loginWithEmail(name.trim(), email.trim(), password.trim())/viewModel.signUpWithEmail(name.trim(), email.trim(), password.trim())/g' app/src/main/java/com/example/ui/AuthScreens.kt
