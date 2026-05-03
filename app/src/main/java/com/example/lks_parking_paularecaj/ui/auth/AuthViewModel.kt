package com.example.lks_parking_paularecaj.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lks_parking_paularecaj.data.model.User
import com.example.lks_parking_paularecaj.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Login
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    // Register
    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> = _registerSuccess

    private val _registerError = MutableLiveData<String?>()
    val registerError: LiveData<String?> = _registerError

    // Forgot Password
    private val _forgotPasswordSuccess = MutableLiveData<Boolean>()
    val forgotPasswordSuccess: LiveData<Boolean> = _forgotPasswordSuccess

    private val _forgotPasswordError = MutableLiveData<String?>()
    val forgotPasswordError: LiveData<String?> = _forgotPasswordError

    // Current User
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    /**
     * Login del usuario
     * Usa mockLogin para desarrollo, cambiar a login() cuando el backend esté listo
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _loginError.value = null

            try {
                // Usar mockLogin para desarrollo
                // Cambiar a authRepository.login() en producción
                val result = authRepository.mockLogin(email, password)

                if (result.isSuccess) {
                    _currentUser.value = result.getOrNull()
                    _loginSuccess.value = true
                } else {
                    _loginError.value = result.exceptionOrNull()?.message
                        ?: "Error al iniciar sesión"
                }
            } catch (e: Exception) {
                _loginError.value = e.message ?: "Error desconocido"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Registro de nuevo usuario
     */
    fun register(name: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _loading.value = true
            _registerError.value = null

            try {
                // Validaciones locales
                if (name.isEmpty()) {
                    _registerError.value = "El nombre es requerido"
                    _loading.value = false
                    return@launch
                }

                if (email.isEmpty()) {
                    _registerError.value = "El email es requerido"
                    _loading.value = false
                    return@launch
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _registerError.value = "Email inválido"
                    _loading.value = false
                    return@launch
                }

                if (password.isEmpty()) {
                    _registerError.value = "La contraseña es requerida"
                    _loading.value = false
                    return@launch
                }

                if (password.length < 6) {
                    _registerError.value = "La contraseña debe tener al menos 6 caracteres"
                    _loading.value = false
                    return@launch
                }

                if (password != confirmPassword) {
                    _registerError.value = "Las contraseñas no coinciden"
                    _loading.value = false
                    return@launch
                }

                // Usar mockRegister para desarrollo
                // Cambiar a authRepository.register() en producción
                val result = authRepository.mockRegister(name, email, password)

                if (result.isSuccess) {
                    _currentUser.value = result.getOrNull()
                    _registerSuccess.value = true
                } else {
                    _registerError.value = result.exceptionOrNull()?.message
                        ?: "Error al registrarse"
                }
            } catch (e: Exception) {
                _registerError.value = e.message ?: "Error desconocido"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Recuperar contraseña
     */
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _loading.value = true
            _forgotPasswordError.value = null

            try {
                if (email.isEmpty()) {
                    _forgotPasswordError.value = "El email es requerido"
                    _loading.value = false
                    return@launch
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _forgotPasswordError.value = "Email inválido"
                    _loading.value = false
                    return@launch
                }

                // Usar mockForgotPassword para desarrollo
                // Cambiar a authRepository.forgotPassword() en producción
                val result = authRepository.mockForgotPassword(email)

                if (result.isSuccess) {
                    _forgotPasswordSuccess.value = true
                } else {
                    _forgotPasswordError.value = result.exceptionOrNull()?.message
                        ?: "Error al recuperar contraseña"
                }
            } catch (e: Exception) {
                _forgotPasswordError.value = e.message ?: "Error desconocido"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Verificar sesión actual
     */
    fun checkCurrentSession() {
        if (authRepository.isLoggedIn()) {
            _currentUser.value = authRepository.getCurrentUser()
            _loginSuccess.value = true
        }
    }

    /**
     * Logout
     */
    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _loginSuccess.value = false
    }

    /**
     * Limpiar errores
     */
    fun clearErrors() {
        _loginError.value = null
        _registerError.value = null
        _forgotPasswordError.value = null
    }
}