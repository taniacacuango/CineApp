package com.example.cineapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cineapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLoginUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLoginUser = findViewById(R.id.btnLoginUser);

        btnLoginUser.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                iniciarSesion(email, password);
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarSesion(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // --- AQUÍ APLICAMOS LA VERIFICACIÓN 2FA ---
                        if (user != null && user.isEmailVerified()) {
                            // El usuario ya verificó su correo, puede pasar
                            Toast.makeText(this, "Acceso concedido", Toast.LENGTH_SHORT).show();
                            // Intent a la pantalla de Películas (la crearemos luego)
                            startActivity(new Intent(LoginActivity.this, MovieCatalogActivity.class));
                        } else {
                            // El usuario no ha verificado su correo
                            Toast.makeText(this, "Por favor, verifica tu correo antes de entrar.", Toast.LENGTH_LONG).show();
                            mAuth.signOut(); // Cerramos sesión hasta que verifique
                        }
                    } else {
                        Toast.makeText(this, "Error: Datos incorrectos", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}