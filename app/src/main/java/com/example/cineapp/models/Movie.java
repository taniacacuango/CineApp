package com.example.cineapp.models;

import com.google.gson.annotations.SerializedName;

public class Movie {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("overview")
    private String overview;

    // --- CONSTRUCTOR VACÍO (OBLIGATORIO PARA FIREBASE) ---
    public Movie() {
    }

    // --- CONSTRUCTOR PARA GUARDAR FAVORITOS MANUALMENTE ---
    public Movie(int id, String title, String posterPath) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }

    public String getFullPosterPath() {
        // Si el link ya viene completo (desde Firebase), lo devolvemos tal cual
        if (posterPath != null && posterPath.startsWith("http")) {
            return posterPath;
        }
        // Si viene de TMDB, le agregamos la base
        if (posterPath != null && !posterPath.isEmpty()) {
            return "https://image.tmdb.org/t/p/w500" + posterPath;
        }
        return null;
    }
}