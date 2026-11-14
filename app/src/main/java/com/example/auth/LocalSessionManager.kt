package com.example.auth

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocalSessionUser(
    val uid: String,
    val displayName: String,
    val email: String
)

class LocalSessionManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(readSession())
    val currentUser: StateFlow<LocalSessionUser?> = _currentUser.asStateFlow()

    private fun readSession(): LocalSessionUser? {
        if (!prefs.getBoolean(KEY_SIGNED_IN, false)) return null
        val uid = prefs.getString(KEY_USER_ID, DEFAULT_UID) ?: DEFAULT_UID
        val name = prefs.getString(KEY_DISPLAY_NAME, DEFAULT_NAME) ?: DEFAULT_NAME
        val email = prefs.getString(KEY_EMAIL, DEFAULT_EMAIL) ?: DEFAULT_EMAIL
        return LocalSessionUser(uid = uid, displayName = name, email = email)
    }

    fun signInLocalMember(displayName: String, email: String): LocalSessionUser {
        val cleanName = displayName.trim().ifBlank { DEFAULT_NAME }.take(80)
        val cleanEmail = email.trim().ifBlank { DEFAULT_EMAIL }.take(120)
        prefs.edit()
            .putBoolean(KEY_SIGNED_IN, true)
            .putString(KEY_USER_ID, DEFAULT_UID)
            .putString(KEY_DISPLAY_NAME, cleanName)
            .putString(KEY_EMAIL, cleanEmail)
            .apply()
        val user = LocalSessionUser(uid = DEFAULT_UID, displayName = cleanName, email = cleanEmail)
        _currentUser.value = user
        return user
    }

    fun signOut() {
        prefs.edit()
            .putBoolean(KEY_SIGNED_IN, false)
            .apply()
        _currentUser.value = null
    }

    private companion object {
        const val PREFS_NAME = "aurevia_local_session"
        const val KEY_SIGNED_IN = "is_signed_in"
        const val KEY_USER_ID = "user_id"
        const val KEY_DISPLAY_NAME = "display_name"
        const val KEY_EMAIL = "email"
        const val DEFAULT_UID = "local_member"
        const val DEFAULT_NAME = "Aurevia Member"
        const val DEFAULT_EMAIL = "member@aurevia.local"
    }
}
