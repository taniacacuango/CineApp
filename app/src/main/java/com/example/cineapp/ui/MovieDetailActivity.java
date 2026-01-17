package com.example.cineapp.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager; // <--- FALTABA ESTO
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cineapp.R;
import com.example.cineapp.adapters.CastAdapter;
import com.example.cineapp.models.Cast; // Asegúrate de tener este modelo
import com.example.cineapp.models.CreditsResponse;
import com.example.cineapp.network.RetrofitClient;
import com.example.cineapp.network.TmdbApi;

import java.util.List; // <--- FALTABA ESTO
import retrofit2.Call;
import retrofit2.Callback; // <--- FALTABA ESTO
import retrofit2.Response; // <--- FALTABA ESTO

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView ivPoster;
    private TextView tvTitle, tvOverview;
    private RecyclerView rvCast;
    private CastAdapter castAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // 1. Vincular vistas
        ivPoster = findViewById(R.id.ivDetailPoster);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvOverview = findViewById(R.id.tvDetailOverview);
        rvCast = findViewById(R.id.rvCast); // Asegúrate de que este ID exista en tu XML

        // 2. Recibir datos del Intent
        String title = getIntent().getStringExtra("title");
        String overview = getIntent().getStringExtra("overview");
        String posterPath = getIntent().getStringExtra("poster");
        int movieId = getIntent().getIntExtra("id", 0); // <--- IMPORTANTE: Debemos enviar esto desde el Catálogo

        // 3. Mostrar datos básicos
        tvTitle.setText(title);
        tvOverview.setText(overview);
        Glide.with(this).load(posterPath).into(ivPoster);

        // 4. Configurar RecyclerView del Reparto (Horizontal)
        rvCast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // 5. Cargar Reparto si tenemos ID
        if (movieId != 0) {
            cargarReparto(movieId);
        } else {
            Toast.makeText(this, "Error: No se recibió el ID de la película", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarReparto(int movieId) {
        TmdbApi api = RetrofitClient.getInstance().getApi();
        // RECUERDA: Es mala práctica dejar la API KEY aquí, pero para probar sirve.
        api.getMovieCredits(movieId, "f1b9e88067a26e85e746a2cd968ecb97").enqueue(new Callback<CreditsResponse>() {
            @Override
            public void onResponse(Call<CreditsResponse> call, Response<CreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Llamamos al método auxiliar para configurar el adaptador
                    configurarRecyclerViewCast(response.body().getCast());
                }
            }

            @Override
            public void onFailure(Call<CreditsResponse> call, Throwable t) {
                Toast.makeText(MovieDetailActivity.this, "Error cargando reparto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- ESTE MÉTODO FALTABA ---
    private void configurarRecyclerViewCast(List<Cast> castList) {
        castAdapter = new CastAdapter(castList);
        rvCast.setAdapter(castAdapter);
    }
}