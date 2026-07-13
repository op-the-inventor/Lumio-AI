#!/bin/bash
awk '
/fun loginWithProvider/ {
    print $0
    print "        viewModelScope.launch {"
    print "            try {"
    print "                when (provider) {"
    print "                    \"google\" -> com.example.data.api.supabase.auth.signInWith(io.github.jan.supabase.auth.providers.Google)"
    print "                }"
    print "                login(provider.replaceFirstChar { it.uppercase() } + \" User\", \"user@${provider}.com\")"
    print "            } catch (e: Throwable) {"
    print "                e.printStackTrace()"
    print "                _error.value = \"Provider login failed: ${e.localizedMessage}\""
    print "            }"
    print "        }"
    print "    }"
    skip = 1
    next
}
/fun logout\(\) \{/ && skip {
    skip = 0
    print $0
    next
}
skip { next }
{ print $0 }
' app/src/main/java/com/example/ui/CallViewModel.kt > app/src/main/java/com/example/ui/CallViewModel.tmp && mv app/src/main/java/com/example/ui/CallViewModel.tmp app/src/main/java/com/example/ui/CallViewModel.kt
