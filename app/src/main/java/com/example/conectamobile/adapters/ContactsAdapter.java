package com.example.conectamobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.conectamobile.R;
import com.example.conectamobile.activities.ChatActivity;
import com.example.conectamobile.models.Message;
import com.example.conectamobile.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private Context context;
    private List<User> usersList;
    private boolean isChat; // ¿Es la pestaña de Chats?

    String theLastMessage;

    public ContactsAdapter(Context context, List<User> usersList, boolean isChat) {
        this.context = context;
        this.usersList = usersList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = usersList.get(position);
        holder.username.setText(user.getName());

        // LÓGICA DE FOTO (Si el usuario tiene foto, la cargamos con Glide)
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().equals("")) {
            Glide.with(context).load(user.getPhotoUrl()).into(holder.profile_image);
        } else {
            holder.profile_image.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        // LÓGICA DE ÚLTIMO MENSAJE
        if (isChat) {
            lastMessage(user.getUid(), holder.status);
        } else {
            // En la pestaña Contactos mostramos el email o estado
            holder.status.setText(user.getEmail());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userid", user.getUid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public TextView status; // Aquí pondremos el último mensaje
        public ImageView profile_image;

        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.tvContactName);
            status = itemView.findViewById(R.id.tvContactStatus);
            profile_image = itemView.findViewById(R.id.imgContactPhoto);
        }
    }

    // --- MAGIA: Buscar el último mensaje en la BD ---
    private void lastMessage(final String userid, final TextView last_msg) {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                theLastMessage = "default";
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message chat = snapshot.getValue(Message.class);
                    if (firebaseUser != null && chat != null) {
                        // Buscamos mensajes entre YO y EL CONTACTO (en cualquier dirección)
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = chat.getMessage();
                        }
                    }
                }

                switch (theLastMessage) {
                    case "default":
                        last_msg.setText("Sin mensajes");
                        break;
                    default:
                        // Si el mensaje es muy largo, lo cortamos con "..."
                        if (theLastMessage.length() > 30) {
                            last_msg.setText(theLastMessage.substring(0, 30) + "...");
                        } else {
                            last_msg.setText(theLastMessage);
                        }
                        break;
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}