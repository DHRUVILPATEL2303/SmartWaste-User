package com.example.smartwaste_user

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.example.smartwaste_user.datastore.dataStore
import com.example.smartwaste_user.presentation.navigation.AppNavigation
import com.example.smartwaste_user.ui.theme.SmartWasteUserTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onboardingCompleted = runBlocking {
            val preferences = this@MainActivity.dataStore.data.first()
            preferences[booleanPreferencesKey("onboarding_completed")] ?: false
        }
        auth = FirebaseAuth.getInstance()
//

        setContent {
            SmartWasteUserTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding->
                    Box(
                        modifier = Modifier.padding(bottom=innerPadding.calculateBottomPadding())
                    ){
                        AppNavigation(shouldShowOnboarding = !onboardingCompleted, currentUser = auth.currentUser)
                    }


                }
            }
        }
    }
}



















//        oneTapClient = Identity.getSignInClient(this)
//        signInRequest = BeginSignInRequest.builder()
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    .setServerClientId("")
//                    .setFilterByAuthorizedAccounts(false)
//                    .setNonce("Smart-Waste App")
//                    .build()
//            )
//            .build()
//
//        val launcher = registerForActivityResult(
//            ActivityResultContracts.StartIntentSenderForResult()
//        ) { result ->
//            try {
//                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
//                val idToken = credential.googleIdToken
//                if (idToken != null) {
//                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
//                    auth.signInWithCredential(firebaseCredential)
//                        .addOnCompleteListener(this) { task ->
//                            if (task.isSuccessful) {
//                                val user = auth.currentUser
//                                Log.d("GoogleSignIn", "Success: ${user?.displayName}")
//                            } else {
//                                Log.e("GoogleSignIn", "Failure: ${task.exception}")
//                            }
//                        }
//                }
//            } catch (e: Exception) {
//                Log.e("GoogleSignIn", "Error: ${e.localizedMessage}")
//            }
//        }
