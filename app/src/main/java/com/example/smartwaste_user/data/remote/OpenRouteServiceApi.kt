package com.example.smartwaste_user.data.remote

import com.example.smartwaste_user.data.models.DirectionsResponse
import com.example.smartwaste_user.data.models.ORSRouteResponse
import com.example.smartwaste_user.presentation.screens.home.CleanRouteItem
import com.google.android.gms.common.api.Api
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface OpenRouteServiceApi {


    @GET("/v2/directions/driving-car")
    suspend fun getDrivingRoute(
        @Header("Authorization") apiKey: String,
        @Query("start") start: String,
        @Query("end") end: String
    ): ORSRouteResponse
}



interface DirectionsApi {

    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "driving",
        @Query("key") apiKey: String
    ): DirectionsResponse
}
object NetworkModule {

    private const val BASE_URL = "https://api.openrouteservice.org/"
    private const val BASE_URL_FOR_GOOGLE_MAPS= "https://maps.googleapis.com/maps/api/"

    fun provideOkHttp(): OkHttpClient {

        return OkHttpClient.Builder()

            .build()
    }

    fun provideRetrofitFOROSM(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun provideRetrofitforGoogleMaps(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_FOR_GOOGLE_MAPS)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }

    fun provideORSApi(): OpenRouteServiceApi {
        return provideRetrofitFOROSM(provideOkHttp()).create(OpenRouteServiceApi::class.java)
    }

    fun provideDirectionsApi(): DirectionsApi {
        return provideRetrofitforGoogleMaps(provideOkHttp())
            .create(DirectionsApi::class.java)
    }
}