package com.chatzz.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseConfig {
    const val URL = "https://jhzqjhfcgtuocqygoxof.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpoenFqaGZjZ3R1b2NxeWdveG9mIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE5ODY5NDEsImV4cCI6MjA4NzU2Mjk0MX0.fUhfYUBB6csT6u9wJoNXKwU9mJZVGToFTBf0C2RCHRQ"
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
