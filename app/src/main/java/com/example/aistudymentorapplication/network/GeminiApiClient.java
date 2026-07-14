package com.example.aistudymentorapplication.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeminiApiClient {
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
    private static Retrofit retrofit = null;

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
