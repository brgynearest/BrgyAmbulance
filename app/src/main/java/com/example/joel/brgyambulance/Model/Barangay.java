package com.example.joel.brgyambulance.Model;

public class Barangay {
    private String email, password, name, platenumber;

    public Barangay() {
    }

    public Barangay(String email, String password, String name, String platenumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.platenumber = platenumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatenumber() {
        return platenumber;
    }

    public void setPlatenumber(String platenumber) {
        this.platenumber = platenumber;
    }
}
