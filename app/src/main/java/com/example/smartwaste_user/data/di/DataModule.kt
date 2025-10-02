package com.example.smartwaste_user.data.di

import android.content.Context
import androidx.compose.ui.res.stringResource
import com.example.smartwaste_user.R
import com.example.smartwaste_user.datastore.DataStoreManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton



@Module
@InstallIn(SingletonComponent::class)
object DataModule {



    @Provides
    @Singleton
    fun provideFirebaseAuth() : FirebaseAuth {
        return FirebaseAuth.getInstance()

    }

    @Provides
    @Singleton
    fun provideFirebaseFireStore() : FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }


    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideCloudMessaging() : FirebaseMessaging {
        return FirebaseMessaging.getInstance()

    }

    @Provides
    @Singleton
    fun provideGenerativeModel(@ApplicationContext context: Context): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = context.getString(R.string.GEMINI_API_KEY),

                systemInstruction = content {
                    text("You are an assistant and give short, user-friendly answers. " +
                            "If the question is about waste (garbage collection, recycling, smart waste, plastic, etc.), " +
                            "then answer it properly.")
                },

        )
    }

}

