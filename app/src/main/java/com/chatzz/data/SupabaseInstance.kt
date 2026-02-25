package com.chatzz.data

import io.github.jan_tennert.supabase.SupabaseClient
import io.github.jan_tennert.supabase.createSupabaseClient
import io.github.jan_tennert.supabase.gotrue.Auth
import io.github.jan_tennert.supabase.postgrest.Postgrest
import io.github.jan_tennert.supabase.realtime.Realtime
import io.github.jan_tennert.supabase.storage.Storage

object SupabaseConfig {
    const val URL = "YOUR_SUPABASE_URL"
    const val ANON_KEY = "YOUR_SUPABASE_ANON_KEY"
}

object SupabaseInstance {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SupabaseConfig.URL,
        supabaseKey = SupabaseConfig.ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}
