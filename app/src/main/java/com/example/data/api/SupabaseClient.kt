package com.example.data.api

import com.example.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google

val supabase = createSupabaseClient(
    supabaseUrl = "https://xfbflzbmduoevwppwxvc.supabase.co",
    supabaseKey = "sb_publishable_upLlPZDRWfvdySsR3QtTxg_M9JR8Jmv"
) {
    install(Auth) {
        scheme = "lumio"
        host = "login"
    }
}
