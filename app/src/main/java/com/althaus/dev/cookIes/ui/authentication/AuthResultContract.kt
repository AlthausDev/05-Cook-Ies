package com.althaus.dev.cookIes.ui.authentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException

/**
 * Clase que extiende [ActivityResultContract] para manejar el proceso de inicio de sesión con Google.
 *
 * Esta clase encapsula la lógica para crear el intent de inicio de sesión y procesar el resultado
 * devuelto por Google Sign-In, extrayendo el ID Token del usuario autenticado.
 *
 * @property googleSignInClient El cliente de Google Sign-In configurado.
 */
class AuthResultContract(
    private val googleSignInClient: GoogleSignInClient
) : ActivityResultContract<Unit, String?>() {

    /**
     * Crea el intent necesario para iniciar el proceso de autenticación con Google.
     *
     * @param context Contexto desde el cual se crea el intent.
     * @param input Entrada requerida para este contrato (en este caso, no se usa).
     * @return Un [Intent] configurado para iniciar el flujo de autenticación con Google.
     */
    override fun createIntent(context: Context, input: Unit): Intent {
        return googleSignInClient.signInIntent
    }

    /**
     * Procesa el resultado devuelto por el flujo de autenticación de Google.
     *
     * @param resultCode Código de resultado devuelto por la actividad iniciada.
     * @param intent [Intent] que contiene los datos devueltos por Google Sign-In.
     * @return El ID Token del usuario autenticado si la operación fue exitosa, o `null` si no lo fue.
     */
    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return null // Si el resultado no es exitoso o el intent es nulo, retorna null
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        return try {
            val account = task.getResult(ApiException::class.java)
            account.idToken // Retorna el ID Token extraído de la cuenta autenticada
        } catch (e: ApiException) {
            println("Error al obtener la cuenta: ${e.localizedMessage}")
            null // Retorna null si ocurre un error al procesar la cuenta
        }
    }
}
