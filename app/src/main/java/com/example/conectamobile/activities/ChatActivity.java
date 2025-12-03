package com.example.conectamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conectamobile.R;
import com.example.conectamobile.adapters.MessagesAdapter;
import com.example.conectamobile.models.Message;
import com.example.conectamobile.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    TextView username;
    FirebaseUser fuser;
    DatabaseReference reference;

    FloatingActionButton btn_send; // O ImageButton según tu XML
    EditText text_send;

    MessagesAdapter messagesAdapter;
    List<Message> mChat;
    RecyclerView recyclerView;

    String userid;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Configuración del Toolbar y Header
        username = findViewById(R.id.tvChatUserName);

        // Configuración de la lista de mensajes
        recyclerView = findViewById(R.id.recyclerMessages);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true); // Para que empiece desde abajo
        recyclerView.setLayoutManager(linearLayoutManager);

        // Botones e Inputs
        btn_send = findViewById(R.id.btnSend);
        text_send = findViewById(R.id.etMessage);

        intent = getIntent();
        userid = intent.getStringExtra("userid"); // Recibimos el ID del usuario con quien hablamos
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        // 1. Botón Enviar
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(fuser.getUid(), userid, msg);
                } else {
                    Toast.makeText(ChatActivity.this, "No puedes enviar un mensaje vacío", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        // 2. Cargar datos del usuario con el que hablamos (Nombre en la barra superior)
        reference = FirebaseDatabase.getInstance().getReference("users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    username.setText(user.getName());
                    readMessages(fuser.getUid(), userid); // Comenzar a leer chat
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Función para guardar mensaje en Firebase
    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        reference.child("Chats").push().setValue(hashMap);
    }

    // Función para leer mensajes en tiempo real
    private void readMessages(final String myid, final String userid) {
        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message chat = snapshot.getValue(Message.class);

                    // Filtro mágico: Solo mostrar mensajes entre TÚ y ÉL/ELLA
                    if (chat != null) {
                        if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                            mChat.add(chat);
                        }
                    }
                }
                messagesAdapter = new MessagesAdapter(ChatActivity.this, mChat);
                recyclerView.setAdapter(messagesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
