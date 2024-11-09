package com.althaus.dev.cookIes.data
//
//import android.app.Activity
//import android.content.Context
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInClient
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.firebase.auth.*
//import dagger.hilt.android.qualifiers.ApplicationContext
//import kotlinx.coroutines.tasks.await
//import javax.inject.Inject
//
//class AuthService @Inject constructor(
//    private val firebaseAuth: FirebaseAuth,
//    @ApplicationContext private val context: Context
//) {
//
//    val currentUser: FirebaseUser?
//        get() = firebaseAuth.currentUser
//
//    suspend fun login(user: String, password: String): FirebaseUser? {
//        return try {
//            firebaseAuth.signInWithEmailAndPassword(user, password).await().user
//        } catch (e: Exception) {
//            e.printStackTrace() // Manejo de excepciones
//            null
//        }
//    }
//
//    suspend fun loginAnonymously(): FirebaseUser? {
//        return try {
//            firebaseAuth.signInAnonymously().await().user
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    suspend fun register(email: String, password: String): FirebaseUser? {
//        return try {
//            firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    fun isUserLogged(): Boolean = currentUser != null
//
//    fun logout() {
//        firebaseAuth.signOut()
//        // Elimina esta línea si LoginManager no es relevante en tu contexto:
//        // LoginManager.getInstance().logOut()
//    }
//
//    suspend fun verifyCode(verificationCode: String, phoneCode: String): FirebaseUser? {
//        val credentials = PhoneAuthProvider.getCredential(verificationCode, phoneCode)
//        return signInWithCredential(credentials)
//    }
//
//    private suspend fun signInWithCredential(credential: AuthCredential): FirebaseUser? {
//        return try {
//            firebaseAuth.signInWithCredential(credential).await().user
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    fun getGoogleClient(): GoogleSignInClient {
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(context.getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//        return GoogleSignIn.getClient(context, gso)
//    }
//
//    suspend fun loginWithGoogle(idToken: String): FirebaseUser? {
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        return signInWithCredential(credential)
//    }
//
//    suspend fun initRegisterWithProvider(
//        activity: Activity, provider: OAuthProvider
//    ): FirebaseUser? {
//        return firebaseAuth.pendingAuthResult?.user ?: try {
//            firebaseAuth.startActivityForSignInWithProvider(activity, provider).await().user
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//}
