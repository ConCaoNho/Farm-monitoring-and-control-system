package com.duong_21011224.btl;

public class Account {
    private int id;
    private String username;
    private String password;
    private String role;
    private String email;

    public Account(int id, String username, String password, String role, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
    }

    // Getter & Setter đầy đủ
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }
}
