package com.example.cineapp.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cineapp.R;
import com.example.cineapp.adapters.MovieAdapter;
import com.example.cineapp.models.Movie;
import com.example.cineapp.models.MovieResponse;
import com.example.cineapp.network.RetrofitClient; // Esto lo crearemos en el siguiente paso
import com.example.cineapp.network.TmdbApi;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieCatalogActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movie> movieLines = new ArrayList<>();
    private final String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJmMWI5ZTg4MDY3YTI2ZTg1ZTc0NmEyY2Q5NjhlY2I5NyIsIm5iZiI6MTc2ODYwMDIxNy4yMDk5OTk4LCJzdWIiOiI2OTZhYjI5OTBkNTMwNWVjOTQ4MjdkOTkiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.FFw9g85vkCSe2BNLhpWtVgNHeyIjGUxtEoNXNje3D3Q"; // Pega aquí tu clave de TMDB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_catalog);

        recyclerView = findViewById(R.id.rvMovies);

        // Usamos GridLayoutManager para que se vea como una cuadrícula (2 columnas)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Llamamos al método que traerá las películas
        cargarPeliculas();
    }

    private void cargarPeliculas() {
        // Aquí es donde usaremos el Cliente Retrofit
        TmdbApi api = RetrofitClient.getInstance().getApi();

        Call<MovieResponse> call = api.getPopularMovies(API_KEY, "es-ES");

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieLines = response.body().getMovies();
                    adapter = new MovieAdapter(movieLines);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(MovieCatalogActivity.this, "Error en la respuesta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(MovieCatalogActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}