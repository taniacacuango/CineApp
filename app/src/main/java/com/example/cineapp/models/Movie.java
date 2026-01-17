package com.example.cineapp.models;

import com.google.gson.annotations.SerializedName;

public class Movie {
    // TMDB nos envía el ID, el título y la ruta de la imagen
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("poster_path")
    private String posterPath;

    // Métodos para obtener los datos (Getters)
    public String getTitle() { return title; }

    public String getFullPosterPath() {
        // Las imágenes de TMDB necesitan esta URL base
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }
}
