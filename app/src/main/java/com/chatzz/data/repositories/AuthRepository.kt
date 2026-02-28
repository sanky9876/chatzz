package com.chatzz.data.repositories

import com.chatzz.data.SupabaseInstance
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val auth = SupabaseInstance.client.auth

    suspend fun signInWithOtp(email: String) = withContext(Dispatchers.IO) {
        // Mocking the OTP send step so the UI proceeds to the verification screen instantly
    }

    suspend fun verifyLoginOtp(email: String, passwordOrToken: String) = withContext(Dispatchers.IO) {
        // Now it's an actual password coming from the UI for Login mode!
        auth.signInWith(Email) {
            this.email = email
            this.password = passwordOrToken // use real text here instead of hardcoded demo!
        }
    }

    suspend fun verifySignUpOtp(email: String, token: String) = withContext(Dispatchers.IO) {
        if (token == "123456") {
            auth.signUpWith(Email) {
                this.email = email
                this.password = "123456demo!"
            }
        } else {
            throw Exception("Invalid Demo OTP! Please enter 123456")
        }
    }

    fun getCurrentUserId(): String? = auth.currentUserOrNull()?.id

    suspend fun signOut() = withContext(Dispatchers.IO) {
        auth.signOut()
    }
}
