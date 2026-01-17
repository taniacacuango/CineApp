package com.example.cineapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cineapp.R;
import com.example.cineapp.adapters.CastAdapter;
import com.example.cineapp.models.Cast;
import com.example.cineapp.models.CreditsResponse;
import com.example.cineapp.models.Movie;
import com.example.cineapp.models.Video;
import com.example.cineapp.models.VideoResponse;
import com.example.cineapp.network.RetrofitClient;
import com.example.cineapp.network.TmdbApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference; // FALTABA ESTO
import com.google.firebase.database.FirebaseDatabase; // FALTABA ESTO
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView ivPoster;
    private TextView tvTitle, tvOverview;
    private RecyclerView rvCast;
    private CastAdapter castAdapter;
    private YouTubePlayerView youTubePlayerView;

    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        ivPoster = findViewById(R.id.ivDetailPoster);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvOverview = findViewById(R.id.tvDetailOverview);
        rvCast = findViewById(R.id.rvCast);
        youTubePlayerView = findViewById(R.id.youtube_player_view);

        getLifecycle().addObserver(youTubePlayerView);

        // --- RECIBIR DATOS DEL INTENT (Variables Globales del método) ---
        String title = getIntent().getStringExtra("title");
        String overview = getIntent().getStringExtra("overview");
        String posterPath = getIntent().getStringExtra("poster");
        int movieId = getIntent().getIntExtra("id", 0);

        tvTitle.setText(title);
        tvOverview.setText(overview);

        if (posterPath != null) {
            Glide.with(this).load(posterPath).into(ivPoster);
        }

        if (rvCast != null) {
            rvCast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        if (movieId != 0) {
            cargarReparto(movieId);
            obtenerTrailer(movieId);
        }

        // Firebase Setup
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // --- BOTÓN FAVORITOS ---
        FloatingActionButton fabFavorite = findViewById(R.id.fabFavorite);
        fabFavorite.setOnClickListener(v -> {
            if (userId == null) {
                Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
                return;
            }

            // CORRECCIÓN: No volvemos a declarar "int movieId" ni "String title".
            // Usamos las variables que ya recibimos arriba del Intent.

            // Creamos el objeto Movie usando el constructor nuevo
            // NOTA: posterPath debe ser la URL completa para que se guarde bien
            Movie favoriteMovie = new Movie(movieId, title, posterPath);

            mDatabase.child("users").child(userId).child("favorites").child(String.valueOf(movieId))
                    .setValue(favoriteMovie)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void cargarReparto(int movieId) {
        TmdbApi api = RetrofitClient.getInstance().getApi();
        api.getMovieCredits(movieId, "f1b9e88067a26e85e746a2cd968ecb97").enqueue(new Callback<CreditsResponse>() {
            @Override
            public void onResponse(Call<CreditsResponse> call, Response<CreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    configurarRecyclerViewCast(response.body().getCast());
                }
            }
            @Override
            public void onFailure(Call<CreditsResponse> call, Throwable t) { }
        });
    }

    private void configurarRecyclerViewCast(List<Cast> castList) {
        if (rvCast != null) {
            castAdapter = new CastAdapter(castList);
            rvCast.setAdapter(castAdapter);
        }
    }

    private void obtenerTrailer(int movieId) {
        TmdbApi api = RetrofitClient.getInstance().getApi();
        api.getMovieVideos(movieId, "f1b9e88067a26e85e746a2cd968ecb97").enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Video> videos = response.body().getResults();
                    for (Video v : videos) {
                        if (v.getSite().equals("YouTube") && v.getType().equals("Trailer")) {
                            cargarVideoEnApp(v.getKey());
                            return;
                        }
                    }
                    youTubePlayerView.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                youTubePlayerView.setVisibility(View.GONE);
            }
        });
    }

    private void cargarVideoEnApp(String videoKey) {
        youTubePlayerView.setVisibility(View.VISIBLE);
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.cueVideo(videoKey, 0);
            }
        });
    }
}