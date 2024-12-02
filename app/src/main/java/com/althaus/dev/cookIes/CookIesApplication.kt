package com.althaus.dev.cookIes

import android.app.Application
import android.content.Intent
import android.util.Log
import com.google.android.gms.security.ProviderInstaller
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase principal de la aplicación que extiende [Application].
 *
 * Esta clase sirve como punto de entrada para la configuración global de la aplicación y
 * está anotada con `@HiltAndroidApp` para habilitar la inyección de dependencias mediante Hilt.
 */
@HiltAndroidApp
class CookIesApplication : Application() {

    /**
     * Método llamado cuando la aplicación se inicializa.
     *
     * Se asegura de que el proveedor de seguridad de Google esté actualizado, instalando
     * cualquier actualización necesaria para garantizar el soporte adecuado de SSL/TLS.
     */
    override fun onCreate() {
        super.onCreate()

        try {
            // Instala actualizaciones de seguridad SSL/TLS si es necesario
            ProviderInstaller.installIfNeeded(applicationContext)
        } catch (e: Exception) {
            // Registra errores relacionados con la instalación del proveedor
            Log.e("CookIesApplication", "Error instalando el proveedor de seguridad: ${e.message}", e)
        }
    }
}
