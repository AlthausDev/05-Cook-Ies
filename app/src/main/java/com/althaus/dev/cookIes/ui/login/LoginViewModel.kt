package com.althaus.dev.cookIes.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private var failedLoginAttempts = 0
    private var isTimeoutActive = false

    // LiveData para el estado de autenticación y el tiempo restante del timeout
//    val authState = // ...
//    val timeoutRemaining = // ...
    private val timeoutDuration = 30000L
    private var timeoutStartTime = 0L

    // Método para iniciar sesión
    fun signIn(email: String, password: String) {
        if (isTimeoutActive && System.currentTimeMillis() - timeoutStartTime < timeoutDuration) {
            // Si el tiempo de espera aún está activo, maneja según sea necesario
            // Puedes actualizar la interfaz o mostrar un mensaje al usuario
            return
        }

        viewModelScope.launch {
            try {
                authRepository.signIn(email, password)
                // Si la autenticación es exitosa, restablece los intentos fallidos
                failedLoginAttempts = 0
            } catch (e: Exception) {
                // Maneja el fallo de inicio de sesión
                failedLoginAttempts++
                if (failedLoginAttempts >= 5) {
                    // Si alcanza 5 intentos fallidos, activa el tiempo de espera
                    isTimeoutActive = true
                    timeoutStartTime = System.currentTimeMillis()
                }
            } finally {
                // Actualiza el LiveData 'authState' y 'timeoutRemaining'
            }
        }
    }

    // Método para registrarse
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
        }
    }
}
