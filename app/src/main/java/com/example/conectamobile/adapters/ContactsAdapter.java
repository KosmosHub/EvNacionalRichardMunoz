package com.example.conectamobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conectamobile.R;
import com.example.conectamobile.activities.ChatActivity;
import com.example.conectamobile.models.User;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private Context context;
    private List<User> usersList;

    public ContactsAdapter(Context context, List<User> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Aquí conectamos con el diseño "item_contact.xml"
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = usersList.get(position);

        // Poner los datos en la pantalla
        holder.username.setText(user.getName());
        holder.email.setText(user.getEmail()); // Usamos el email como estado por ahora

        // Al hacer clic en un usuario, abrimos el chat
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                // Pasamos el ID del usuario al chat para saber con quién hablamos
                intent.putExtra("userid", user.getUid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    // Clase interna para guardar las referencias de la vista
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public TextView email;

        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.tvContactName);
            // Asegúrate de que este ID exista en tu item_contact.xml (quizás le pusimos tvContactStatus)
            email = itemView.findViewById(R.id.tvContactStatus);
        }
    }
}