package com.example.lks_parking_paularecaj.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.lks_parking_paularecaj.data.api.ParkingApiService
import com.example.lks_parking_paularecaj.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ParkingApiService,
    private val context: Context
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("parking_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_TOKEN = "user_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    /**
     * Login del usuario
     */
    suspend fun login(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // Llamada a la API
                val credentials = mapOf(
                    "email" to email,
                    "password" to password
                )

                val user = apiService.login(credentials)

                // Guardar sesión
                saveUserSession(user)

                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Mock login (para desarrollo sin backend)
     */
    suspend fun mockLogin(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // Simular delay de red
                kotlinx.coroutines.delay(1000)

                // Validación simple
                if (email.isEmpty() || password.isEmpty()) {
                    throw Exception("Email y contraseña son requeridos")
                }

                // Usuario mock
                val user = User(
                    id = "mock_user_123",
                    email = email,
                    name = "Usuario Demo",
                    token = "mock_token_${System.currentTimeMillis()}"
                )

                // Guardar sesión
                saveUserSession(user)

                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Registro de nuevo usuario
     */
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val userData = mapOf(
                    "name" to name,
                    "email" to email,
                    "password" to password
                )

                val user = apiService.register(userData)

                // Guardar sesión
                saveUserSession(user)

                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Mock register (para desarrollo sin backend)
     */
    suspend fun mockRegister(
        name: String,
        email: String,
        password: String
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                kotlinx.coroutines.delay(1000)

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    throw Exception("Todos los campos son requeridos")
                }

                if (password.length < 6) {
                    throw Exception("La contraseña debe tener al menos 6 caracteres")
                }

                val user = User(
                    id = "mock_user_${System.currentTimeMillis()}",
                    email = email,
                    name = name,
                    token = "mock_token_${System.currentTimeMillis()}"
                )

                saveUserSession(user)

                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Recuperar contraseña
     */
    suspend fun forgotPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val emailMap = mapOf("email" to email)
                apiService.forgotPassword(emailMap)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Mock forgot password
     */
    suspend fun mockForgotPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                kotlinx.coroutines.delay(1000)

                if (email.isEmpty()) {
                    throw Exception("El email es requerido")
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    throw Exception("Email inválido")
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Logout del usuario
     */
    fun logout() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }

    /**
     * Verificar si el usuario está logueado
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Obtener usuario actual
     */
    fun getCurrentUser(): User? {
        if (!isLoggedIn()) return null

        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_USER_EMAIL, null) ?: return null
        val name = prefs.getString(KEY_USER_NAME, null) ?: return null
        val token = prefs.getString(KEY_USER_TOKEN, null)

        return User(id, email, name, token)
    }

    /**
     * Obtener token de autenticación
     */
    fun getAuthToken(): String? {
        return prefs.getString(KEY_USER_TOKEN, null)
    }

    /**
     * Guardar sesión del usuario
     */
    private fun saveUserSession(user: User) {
        prefs.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_TOKEN, user.token)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
}
