package com.example.cineapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreditsResponse {
    @SerializedName("cast")
    private List<Cast> cast;
    public List<Cast> getCast() { return cast; }
}