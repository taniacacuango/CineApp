package com.example.cineapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.example.cineapp.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference; // FALTABA ESTO
import com.google.firebase.database.FirebaseDatabase; // FALTABA ESTO
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieCatalogActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movie> movieLines = new ArrayList<>();
    // BuildConfig se genera automáticamente al compilar
    private final String API_KEY = BuildConfig.TMDB_API_KEY;

    // Declaramos la referencia a la base de datos
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_catalog);

        // Inicializar Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 1. Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2. Configurar RecyclerView
        recyclerView = findViewById(R.id.rvMovies);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        cargarPeliculas();

        // 3. Configurar Buscador
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 1. Sanitización básica: Quitar espacios al inicio/final
                String cleanQuery = query.trim();

                // 2. Validación de seguridad OWASP
                // Evitamos búsquedas vacías o caracteres muy extraños que podrían intentar inyección
                if (!cleanQuery.isEmpty() && cleanQuery.matches("[a-zA-Z0-9 ñÑáéíóúÁÉÍÓÚ]+")) {
                    buscarPeliculas(cleanQuery);
                } else {
                    // Alerta al usuario (Feedback)
                    Toast.makeText(MovieCatalogActivity.this, "Por favor, busca un título válido (solo letras y números)", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) cargarPeliculas();
                return false;
            }
        });

        // 4. Configurar BottomNavigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_movies) {
                cargarPeliculas();
            } else if (id == R.id.nav_favorites) {
                cargarFavoritosDesdeFirebase();
            } else if (id == R.id.nav_logout) {
                mostrarDialogoCierreSesion();
            }
            return true;
        });

        // 5. Configurar Drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        View headerView = navigationView.getHeaderView(0);
        TextView tvUserEmail = headerView.findViewById(R.id.tvUserEmail);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvUserEmail.setText(user.getEmail());
        }
        // 1. Verificar versión de Android (Solo necesario en Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {

                // 2. Pedir el permiso
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
    private void mostrarDialogoCierreSesion() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres salir de CineApp?")
                .setPositiveButton("Sí, salir", (dialog, which) -> {
                    // Acción confirmada: Cerramos sesión
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MovieCatalogActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    // Acción cancelada: No hacemos nada, solo se cierra el diálogo
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert) // Icono de alerta del sistema
                .show();
    }
    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MovieCatalogActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(MovieCatalogActivity.this, "Error conexión", Toast.LENGTH_SHORT).show();
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
            public void onFailure(Call<MovieResponse> call, Throwable t) {}
        });
    }

    private void configuringAdapter(List<Movie> movies) { // (Esto es helper para configurarAdapter)
        configurarAdapter(movies);
    }

    // Método unificado para configurar el adaptador y el clic
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

    private void cargarFavoritosDesdeFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        mDatabase.child("users").child(uid).child("favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Movie> favList = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Movie m = data.getValue(Movie.class);
                            favList.add(m);
                        }
                        // Reutilizamos el método para mostrar la lista
                        configurarAdapter(favList);
                        Toast.makeText(MovieCatalogActivity.this, "Favoritos cargados", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MovieCatalogActivity.this, "Error al cargar favoritos", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}