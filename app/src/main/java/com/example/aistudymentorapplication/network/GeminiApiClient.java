package com.example.aistudymentorapplication.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * GeminiApiClient is a singleton class that provides a Retrofit instance
 * for making networking calls to the Google Gemini API.
 */
public class GeminiApiClient {
    // The base URL for the Google Generative Language API
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
    private static Retrofit retrofit = null;

    /**
     * Provides a singleton instance of GeminiApiService.
     * Uses GsonConverterFactory to handle JSON serialization/deserialization.
     */
    public static GeminiApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(GeminiApiService.class);
    }
}
