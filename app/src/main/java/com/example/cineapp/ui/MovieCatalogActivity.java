package com.example.cineapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cineapp.R;
import com.example.cineapp.adapters.MovieAdapter;
import com.example.cineapp.models.Movie;
import com.example.cineapp.models.MovieResponse;
import com.example.cineapp.network.RetrofitClient;
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

    // Tu API KEY
    private final String API_KEY = "f1b9e88067a26e85e746a2cd968ecb97";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_catalog);

        recyclerView = findViewById(R.id.rvMovies);
        // Configura la cuadrícula de 2 columnas
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        cargarPeliculas();
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Cuando el usuario presiona la lupa del teclado
                buscarPeliculas(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Opcional: buscar mientras escribe.
                // Si el texto está vacío, volver a mostrar las populares
                if (newText.isEmpty()) {
                    cargarPeliculas();
                }
                return false;
            }
        });
    }

    private void cargarPeliculas() {
        TmdbApi api = RetrofitClient.getInstance().getApi();
        Call<MovieResponse> call = api.getPopularMovies(API_KEY, "es-ES");

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieLines = response.body().getMovies();

                    // IMPORTANTE: Aquí inicializamos el adaptador pasándole:
                    // 1. La lista de películas
                    // 2. El evento (Listener) de qué hacer al dar clic
                    adapter = new MovieAdapter(movieLines, new MovieAdapter.OnMovieClickListener() {
                        @Override
                        public void onMovieClick(Movie movie) {
                            // Navegación a la pantalla de detalle
                            Intent intent = new Intent(MovieCatalogActivity.this, MovieDetailActivity.class);
                            intent.putExtra("title", movie.getTitle());
                            intent.putExtra("overview", movie.getOverview());
                            intent.putExtra("poster", movie.getFullPosterPath());
                            intent.putExtra("id", movie.getId());
                            startActivity(intent);
                        }
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(MovieCatalogActivity.this, "Error en la respuesta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(MovieCatalogActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void buscarPeliculas(String query) {
        TmdbApi api = RetrofitClient.getInstance().getApi();
        api.searchMovies(API_KEY, query, "es-ES").enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieLines = response.body().getMovies();
                    // Actualizamos el adapter con los nuevos resultados
                    adapter = new MovieAdapter(movieLines, movie -> {
                        // Mantenemos el mismo evento de clic para ir al detalle
                        Intent intent = new Intent(MovieCatalogActivity.this, MovieDetailActivity.class);
                        intent.putExtra("id", movie.getId());
                        intent.putExtra("title", movie.getTitle());
                        intent.putExtra("overview", movie.getOverview());
                        intent.putExtra("poster", movie.getFullPosterPath());
                        startActivity(intent);
                    });
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(MovieCatalogActivity.this, "Error al buscar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}