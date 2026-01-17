package com.example.cineapp.models;

import com.google.gson.annotations.SerializedName;

public class Video {
    @SerializedName("key")
    private String key; // Esta es la ID del video de YouTube (ej: "dQw4w9WgXcQ")
    @SerializedName("site")
    private String site;
    @SerializedName("type")
    private String type;

    public String getKey() { return key; }
    public String getSite() { return site; }
    public String getType() { return type; }
}
