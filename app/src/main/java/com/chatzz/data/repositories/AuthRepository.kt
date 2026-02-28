package com.chatzz.data.repositories

import com.chatzz.data.SupabaseInstance
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.OtpType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val auth = SupabaseInstance.client.auth

    suspend fun signInWithOtp(email: String) = withContext(Dispatchers.IO) {
        auth.signInWith(OtpType.Email(email))
    }

    suspend fun verifyOtp(email: String, token: String) = withContext(Dispatchers.IO) {
        auth.verifyOtp(
            type = OtpType.Email(email),
            token = token
        )
    }

    fun getCurrentUserId(): String? = auth.currentUserOrNull()?.id
}
