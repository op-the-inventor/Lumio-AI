#!/bin/bash
awk '
/fun loginWithEmail\(name: String, email: String, password: String\) \{/ {
    print "    fun signInWithEmail(email: String, password: String) {"
    print "        viewModelScope.launch {"
    print "            try {"
    print "                com.example.data.api.supabase.auth.signInWith(io.github.jan.supabase.auth.providers.builtin.Email) {"
    print "                    this.email = email"
    print "                    this.password = password"
    print "                }"
    print "                login(\"User\", email)"
    print "            } catch (e: Throwable) {"
    print "                e.printStackTrace()"
    print "                _error.value = \"Login failed: ${e.localizedMessage}\""
    print "            }"
    print "        }"
    print "    }"
    print ""
    print "    fun signUpWithEmail(name: String, email: String, password: String) {"
    print "        viewModelScope.launch {"
    print "            try {"
    print "                com.example.data.api.supabase.auth.signUpWith(io.github.jan.supabase.auth.providers.builtin.Email) {"
    print "                    this.email = email"
    print "                    this.password = password"
    print "                }"
    print "                login(if (name.isBlank()) email.substringBefore(\"@\") else name, email)"
    print "            } catch (e: Throwable) {"
    print "                e.printStackTrace()"
    print "                _error.value = \"Sign up failed: ${e.localizedMessage}\""
    print "            }"
    print "        }"
    print "    }"
    skip = 1
    next
}
/fun loginWithProvider/ && skip {
    skip = 0
    print $0
    next
}
skip { next }
{ print $0 }
' app/src/main/java/com/example/ui/CallViewModel.kt > app/src/main/java/com/example/ui/CallViewModel.tmp && mv app/src/main/java/com/example/ui/CallViewModel.tmp app/src/main/java/com/example/ui/CallViewModel.kt
