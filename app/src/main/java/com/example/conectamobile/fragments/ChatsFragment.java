package com.example.conectamobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conectamobile.R;
import com.example.conectamobile.adapters.ContactsAdapter;
import com.example.conectamobile.models.Message;
import com.example.conectamobile.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmptyState;

    private ContactsAdapter contactsAdapter;
    private List<User> mUsers;
    private List<String> usersList; // Lista de IDs de gente con la que hablamos

    private FirebaseUser fuser;
    private DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recyclerChats);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        usersList = new ArrayList<>();

        // 1. Escuchar los mensajes para ver con quién hemos hablado
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                Set<String> uniqueUsers = new HashSet<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message chat = snapshot.getValue(Message.class);

                    if (chat != null) {
                        if (chat.getSender().equals(fuser.getUid())) {
                            uniqueUsers.add(chat.getReceiver());
                        }
                        if (chat.getReceiver().equals(fuser.getUid())) {
                            uniqueUsers.add(chat.getSender());
                        }
                    }
                }

                usersList.addAll(uniqueUsers);

                if (usersList.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    readChats();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        return view;
    }

    private void readChats() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    if (user != null && user.getUid() != null) {
                        if (usersList.contains(user.getUid())) {
                            mUsers.add(user);
                        }
                    }
                }

                // CORRECCIÓN AQUÍ: Pasamos 'true' para indicar que muestre el último mensaje
                contactsAdapter = new ContactsAdapter(getContext(), mUsers, true);
                recyclerView.setAdapter(contactsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}