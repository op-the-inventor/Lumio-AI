import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.createSupabaseClient

fun test() {
    createSupabaseClient("", "") {
        install(Auth) {
            scheme = "lumio"
            host = "login"
        }
    }
}
