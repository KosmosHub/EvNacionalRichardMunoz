package com.example.conectamobile.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.conectamobile.R;
import com.example.conectamobile.activities.LoginActivity;
import com.example.conectamobile.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileFragment extends Fragment {

    ImageView imgProfile;
    TextInputEditText etProfileName, etProfileEmail;
    Button btnSave, btnLogout, btnChangePhoto;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    FirebaseUser fuser;
    GoogleSignInClient mGoogleSignInClient;

    private Uri imageUri;
    private ActivityResultLauncher<String> galleryLauncher;
    private ProgressDialog pd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgProfile = view.findViewById(R.id.imgProfile);
        etProfileName = view.findViewById(R.id.etProfileName);
        etProfileEmail = view.findViewById(R.id.etProfileEmail);
        btnSave = view.findViewById(R.id.btnSave);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto);

        mAuth = FirebaseAuth.getInstance();
        fuser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Launcher para abrir galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            imageUri = result;
                            imgProfile.setImageURI(imageUri);
                            saveImageAsBase64(); // Guardar como texto al elegir
                        }
                    }
                }
        );

        if (fuser != null) {
            loadUserInfo();
        }

        btnChangePhoto.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnLogout.setOnClickListener(v -> signOut());

        return view;
    }

    private void loadUserInfo() {
        mDatabase.child(fuser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    etProfileName.setText(user.getName());
                    etProfileEmail.setText(user.getEmail());

                    // Cargar foto Base64 con Glide (Glide soporta strings base64 si son URLs válidas o bytes,
                    // pero para simplificar, usaremos decodificación directa si es un string base64 puro)
                    if (user.getPhotoUrl() != null && !user.getPhotoUrl().equals("")) {
                        try {
                            // Intentamos cargar como Base64
                            byte[] imageByteArray = Base64.decode(user.getPhotoUrl(), Base64.DEFAULT);
                            if (getContext() != null) {
                                Glide.with(getContext()).load(imageByteArray).into(imgProfile);
                            }
                        } catch (IllegalArgumentException e) {
                            // Si falla, quizás es una URL normal de Google, intentamos cargarla normal
                            if (getContext() != null) {
                                Glide.with(getContext()).load(user.getPhotoUrl()).into(imgProfile);
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // MÉTODO CLAVE: Convierte la imagen a Texto Base64
    private void saveImageAsBase64() {
        pd = new ProgressDialog(getContext());
        pd.setMessage("Guardando foto...");
        pd.show();

        if (imageUri != null) {
            try {
                Bitmap bitmap;
                if (Build.VERSION.SDK_INT >= 29) {
                    ImageDecoder.Source source = ImageDecoder.createSource(requireContext().getContentResolver(), imageUri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                }

                // Reducir tamaño para no saturar la base de datos (IMPORTANTE)
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream); // Calidad 70%
                byte[] byteFormat = stream.toByteArray();

                // Obtenemos el String Base64
                String imgString = Base64.encodeToString(byteFormat, Base64.DEFAULT);

                // Guardamos ese string larguísimo en el campo "photoUrl"
                mDatabase.child(fuser.getUid()).child("photoUrl").setValue(imgString)
                        .addOnCompleteListener(task -> {
                            pd.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Foto guardada correctamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
                            }
                        });

            } catch (IOException e) {
                pd.dismiss();
                e.printStackTrace();
                Toast.makeText(getContext(), "Error procesando imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}