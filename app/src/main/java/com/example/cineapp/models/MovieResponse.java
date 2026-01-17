package com.example.cineapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieResponse {
    @SerializedName("results")
    private List<Movie> movies; // Usa la clase Movie que creamos antes

    public List<Movie> getMovies() {
        return movies;
    }
}