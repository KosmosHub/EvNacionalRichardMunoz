package com.example.conectamobile.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.conectamobile.R;
import com.example.conectamobile.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin, btnGoogle;
    private TextView tvGoToRegister;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference mDatabase;
    private ProgressDialog loader;

    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Configurar Firebase y Google
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Configuración para pedir el email y perfil básico a Google
        // El R.string.default_web_client_id lo genera Firebase automáticamente en el google-services.json
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Verificar sesión activa
        if (mAuth.getCurrentUser() != null) {
            goToMain();
        }

        setContentView(R.layout.activity_login);

        loader = new ProgressDialog(this);
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle); // Referencia al nuevo botón
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        // LOGIN NORMAL
        btnLogin.setOnClickListener(v -> {
            String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                loginWithEmail(email, password);
            } else {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show();
            }
        });

        // LOGIN GOOGLE
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    // --- LÓGICA GOOGLE ---
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Resultado del intent de Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google dio el OK, ahora autenticamos con Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Fallo Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        loader.setMessage("Ingresando con Google...");
        loader.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login exitoso, ahora guardamos/actualizamos datos en Realtime DB
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getUid();
                            String name = user.getDisplayName();
                            String email = user.getEmail();

                            // Creamos objeto usuario
                            User newUser = new User(userId, name, email);

                            // Guardamos en DB "users"
                            mDatabase.child("users").child(userId).setValue(newUser)
                                    .addOnCompleteListener(taskDB -> {
                                        loader.dismiss();
                                        goToMain();
                                    });
                        } else {
                            loader.dismiss();
                            Toast.makeText(LoginActivity.this, "Fallo en Firebase", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    // ---------------------

    private void loginWithEmail(String email, String password) {
        loader.setMessage("Iniciando sesión...");
        loader.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loader.dismiss();
                    if (task.isSuccessful()) {
                        goToMain();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}