package com.example.conectamobile.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private String photoUrl; // Para futuro uso

    // Constructor vacío obligatorio para Firebase
    public User() { }

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photoUrl = "";
    }

    // Getters y Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}