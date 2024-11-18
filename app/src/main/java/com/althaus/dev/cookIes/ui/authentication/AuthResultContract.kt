import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class AuthResultContract(private val googleSignInClient: GoogleSignInClient) :
    ActivityResultContract<Int, String?>() {

    override fun createIntent(context: Context, input: Int): Intent {
        return googleSignInClient.signInIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return if (resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            task.result?.idToken
        } else null
    }
}
