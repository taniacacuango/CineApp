package com.example.cineapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cineapp.R;
import com.example.cineapp.adapters.MovieAdapter;
import com.example.cineapp.models.Movie;
import com.example.cineapp.models.MovieResponse;
import com.example.cineapp.network.RetrofitClient;
import com.example.cineapp.network.TmdbApi;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.bottomnavigation.BottomNavigationView; // <--- FALTABA ESTO
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieCatalogActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movie> movieLines = new ArrayList<>();
    private final String API_KEY = "f1b9e88067a26e85e746a2cd968ecb97";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_catalog);

        // 1. Configurar RecyclerView
        recyclerView = findViewById(R.id.rvMovies);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // 2. Cargar datos iniciales
        cargarPeliculas();

        // 3. Configurar Buscador
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                buscarPeliculas(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    cargarPeliculas();
                }
                return false;
            }
        });

        // 4. Configurar Menú Inferior (ESTO ESTABA FUERA Y DABA ERROR)
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_movies) {
                cargarPeliculas();
            } else if (id == R.id.nav_favorites) {
                Toast.makeText(this, "Próximamente: Favoritos", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MovieCatalogActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            return true;
        });
        // 1. Referenciar el NavigationView (el menú lateral)
        NavigationView navigationView = findViewById(R.id.nav_view);

// 2. Obtener la vista del Header (el archivo que acabamos de crear)
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserEmail = headerView.findViewById(R.id.tvUserEmail);

// 3. Obtener el usuario actual de Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Ponemos el correo del usuario real en el texto
            tvUserEmail.setText(user.getEmail());
        }
        // Configurar el botón de abrir menú (Toolbar)
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void cargarPeliculas() {
        TmdbApi api = RetrofitClient.getInstance().getApi();
        Call<MovieResponse> call = api.getPopularMovies(API_KEY, "es-ES");

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieLines = response.body().getMovies();
                    configurarAdapter(movieLines);
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
                    configurarAdapter(movieLines);
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(MovieCatalogActivity.this, "Error al buscar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método auxiliar para no repetir código del adapter
    private void configurarAdapter(List<Movie> movies) {
        adapter = new MovieAdapter(movies, movie -> {
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