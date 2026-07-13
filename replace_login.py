import re

with open("app/src/main/java/com/example/ui/AuthScreens.kt", "r") as f:
    content = f.read()

def replace_surface_in_login(match):
    return """        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(32.dp),
            color = Color.White
        ) {"""

# Replace Login Surface
content = re.sub(r"""\s*Surface\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.weight\(1f\),\s*shape = RoundedCornerShape\(topStart = 32\.dp, topEnd = 32\.dp\),\s*color = Color\.White\s*\) \{""", replace_surface_in_login, content)

# Replace buttons in Login/Register
content = re.sub(r"""Button\(\s*onClick = \{\s*if \(email\.isNotBlank\(\) && password\.isNotBlank\(\)\) \{\s*viewModel\.loginWithEmail\("", email\.trim\(\), password\.trim\(\)\)\s*\} else \{\s*Toast\.makeText\(context, "Please enter email and password", Toast\.LENGTH_SHORT\)\.show\(\)\s*\}\s*\},[\s\S]*?Text\("Login", fontSize = 16\.sp, fontWeight = FontWeight\.Bold\)\s*\}""", 
"""Button3D(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.loginWithEmail("", email.trim(), password.trim())
                        } else {
                            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                        }
                    },
                    text = "Login",
                    modifier = Modifier.padding(top = 8.dp)
                )""", content)

content = re.sub(r"""Button\(\s*onClick = \{\s*if \(email\.isNotBlank\(\) && password\.isNotBlank\(\) && name\.isNotBlank\(\) && agreeToTerms\) \{\s*viewModel\.loginWithEmail\(name\.trim\(\), email\.trim\(\), password\.trim\(\)\)\s*\} else \{\s*Toast\.makeText\(context, "Please fill all fields and agree to terms", Toast\.LENGTH_SHORT\)\.show\(\)\s*\}\s*\},[\s\S]*?Text\("Create account", fontSize = 16\.sp, fontWeight = FontWeight\.Bold\)\s*\}""",
"""Button3D(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank() && name.isNotBlank() && agreeToTerms) {
                            viewModel.loginWithEmail(name.trim(), email.trim(), password.trim())
                        } else {
                            Toast.makeText(context, "Please fill all fields and agree to terms", Toast.LENGTH_SHORT).show()
                        }
                    },
                    text = "Create account",
                    modifier = Modifier.padding(top = 8.dp)
                )""", content)

with open("app/src/main/java/com/example/ui/AuthScreens.kt", "w") as f:
    f.write(content)
