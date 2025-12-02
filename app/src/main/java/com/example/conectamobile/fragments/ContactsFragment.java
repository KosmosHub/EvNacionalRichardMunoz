package com.example.conectamobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conectamobile.R;
import com.example.conectamobile.adapters.ContactsAdapter;
import com.example.conectamobile.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ContactsAdapter contactsAdapter;
    private List<User> mUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        recyclerView = view.findViewById(R.id.recyclerContacts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();
        readUsers();

        return view;
    }

    private void readUsers() {
        // Obtenemos el usuario actual para no mostrarnos a nosotros mismos en la lista
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Referencia a la base de datos "users"
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                // Recorremos todos los usuarios en la base de datos
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    // Validación de seguridad para que no explote si hay datos raros
                    if (user != null && user.getUid() != null && firebaseUser != null) {
                        // Agregamos a la lista a todos MENOS a nosotros mismos
                        if (!user.getUid().equals(firebaseUser.getUid())) {
                            mUsers.add(user);
                        }
                    }
                }

                // Le pasamos la lista llena al adaptador
                contactsAdapter = new ContactsAdapter(getContext(), mUsers);
                recyclerView.setAdapter(contactsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejo de errores si falla la conexión
            }
        });
    }
}