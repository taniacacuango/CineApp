package com.example.cineapp.network;

import com.example.cineapp.models.MovieResponse; // La crearemos en el siguiente paso
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TmdbApi {
    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );
}