package com.example.cineapp.models;

import com.google.gson.annotations.SerializedName;

public class Cast {
    @SerializedName("name")
    private String name;
    @SerializedName("character")
    private String character;
    @SerializedName("profile_path")
    private String profilePath;

    public String getName() { return name; }
    public String getCharacter() { return character; }
    public String getFullProfilePath() {
        return "https://image.tmdb.org/t/p/w200" + profilePath;
    }
}
