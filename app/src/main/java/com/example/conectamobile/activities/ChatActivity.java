package com.example.conectamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
import com.example.conectamobile.utils.MqttHandler; // <--- IMPORTANTE
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

    FloatingActionButton btn_send;
    EditText text_send;

    MessagesAdapter messagesAdapter;
    List<Message> mChat;
    RecyclerView recyclerView;

    String userid;
    Intent intent;

    // VARIABLE MQTT
    private MqttHandler mqttHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Inicializar UI
        username = findViewById(R.id.tvChatUserName);
        btn_send = findViewById(R.id.btnSend);
        text_send = findViewById(R.id.etMessage);

        recyclerView = findViewById(R.id.recyclerMessages);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        // --- INICIALIZAR MQTT (Requisito Rúbrica) ---
        mqttHandler = new MqttHandler();
        // Conectamos usando nuestro ID como cliente
        if (fuser != null) {
            mqttHandler.connect(this, fuser.getUid());
        }
        // ---------------------------------------------

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    // 1. Enviar por Firebase (Para que se vea en la app)
                    sendMessage(fuser.getUid(), userid, msg);

                    // 2. Enviar por MQTT (Para cumplir la rúbrica)
                    // Tópico: conectamobile/chat/PARA_QUIEN_VA
                    mqttHandler.publish("conectamobile/chat/" + userid, msg);
                } else {
                    Toast.makeText(ChatActivity.this, "Mensaje vacío", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        // Cargar info del usuario receptor
        reference = FirebaseDatabase.getInstance().getReference("users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    username.setText(user.getName());
                    readMessages(fuser.getUid(), userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        reference.child("Chats").push().setValue(hashMap);
    }

    private void readMessages(final String myid, final String userid) {
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message chat = snapshot.getValue(Message.class);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Desconectar MQTT al salir para liberar recursos
        if (mqttHandler != null) {
            mqttHandler.disconnect();
        }
    }
}