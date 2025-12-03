package com.example.conectamobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conectamobile.R;
import com.example.conectamobile.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<Message> mChat;
    private FirebaseUser fuser;

    public MessagesAdapter(Context context, List<Message> mChat) {
        this.context = context;
        this.mChat = mChat;
    }

    @NonNull
    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            // Si es mío: Burbuja derecha (Verde)
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new ViewHolder(view);
        } else {
            // Si es del otro: Burbuja izquierda (Blanca)
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.ViewHolder holder, int position) {
        Message chat = mChat.get(position);
        holder.show_message.setText(chat.getMessage());
        // Aquí podrías poner la hora si la agregas al modelo después
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        // Lógica clave: Si el remitente soy YO, devuelve TIPO DERECHA
        if (mChat.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;
        // public TextView time_tv; // Descomentar si usas hora

        public ViewHolder(View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.tvMessageBody);
            // time_tv = itemView.findViewById(R.id.tvMessageTime);
        }
    }
}