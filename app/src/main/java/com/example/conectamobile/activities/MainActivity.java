package com.example.conectamobile.activities;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

// IMPORTANTE: Esta línea conecta tu código Java con tus diseños XML
import com.example.conectamobile.R;

// Importamos los fragments (Asegúrate de tener estos archivos creados en la carpeta fragments)
import com.example.conectamobile.fragments.ChatsFragment;
import com.example.conectamobile.fragments.ContactsFragment;
import com.example.conectamobile.fragments.ProfileFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enlazamos el menú de abajo
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Al abrir la app, cargamos la pestaña de Contactos por defecto
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ContactsFragment()).commit();
        }
    }

    // Lógica para cambiar de pantalla cuando tocas el menú
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_contacts) {
                        selectedFragment = new ContactsFragment();
                    } else if (itemId == R.id.nav_chats) {
                        selectedFragment = new ChatsFragment();
                    } else if (itemId == R.id.nav_profile) {
                        selectedFragment = new ProfileFragment();
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                selectedFragment).commit();
                    }
                    return true;
                }
            };
}