package com.althaus.dev.cookIes.di

import android.content.Context
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.data.repository.AuthRepository
import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.althaus.dev.cookIes.data.repository.NotificationRepository
import com.althaus.dev.cookIes.data.repository.RecipeRepository
import com.althaus.dev.cookIes.data.repository.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Provisión de FirebaseAuth
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // Provisión de Firestore
    @Provides
    @Singleton
    fun provideFirestoreInstance(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // Provisión de AuthRepository
    @Singleton
    @Provides
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestoreRepository: FirestoreRepository,
        @ApplicationContext context: Context
    ): AuthRepository = AuthRepository(firebaseAuth, firestoreRepository, context)


    // Provisión de FirestoreRepository
    @Provides
    @Singleton
    fun provideFirestoreRepository(firestore: FirebaseFirestore): FirestoreRepository =
        FirestoreRepository(firestore)

    // Configuración y provisión de GoogleSignInClient
    @Singleton
    @Provides
    fun provideGoogleSignInClient(@ApplicationContext context: Context): GoogleSignInClient {
        val clientId = context.getString(R.string.default_web_client_id)
        require(clientId.isNotBlank()) { "default_web_client_id no está configurado en strings.xml" }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    // Provisión de UserRepository
    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepository = UserRepository(firebaseAuth, firestore)

    // Provisión de NotificationRepository
    @Provides
    @Singleton
    fun provideNotificationRepository(firestore: FirebaseFirestore): NotificationRepository =
        NotificationRepository(firestore)
}
