package com.example.cineapp.network;

import com.example.cineapp.models.CreditsResponse;
import com.example.cineapp.models.MovieResponse; // La crearemos en el siguiente paso
import com.example.cineapp.models.VideoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TmdbApi {
    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );
    @GET("movie/{movie_id}/credits")
    Call<CreditsResponse> getMovieCredits(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey
    );
    @GET("search/movie")
    Call<MovieResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("query") String query,      // Aquí irá lo que el usuario escriba
            @Query("language") String language
    );
    @GET("movie/{movie_id}/videos")
    Call<VideoResponse> getMovieVideos(

            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey
    );
}