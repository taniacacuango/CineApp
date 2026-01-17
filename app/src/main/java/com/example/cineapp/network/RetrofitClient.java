package com.example.cineapp.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // URL base de la API de TheMovieDB
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static RetrofitClient mInstance;
    private Retrofit retrofit;

    private RetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) // Convierte JSON a Java automáticamente
                .build();
    }

    // Método para obtener la instancia única del cliente
    public static synchronized RetrofitClient getInstance() {
        if (mInstance == null) {
            mInstance = new RetrofitClient();
        }
        return mInstance;
    }

    // Método para conectar con nuestra interfaz TmdbApi
    public TmdbApi getApi() {
        return retrofit.create(TmdbApi.class);
    }
}
