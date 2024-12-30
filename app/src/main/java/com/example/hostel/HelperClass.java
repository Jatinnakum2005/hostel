package com.example.hostel;

public class HelperClass {
    private String userName;
    private String email;
    private String phone;
    private String password;

    // Constructor with parameters
    public HelperClass(String userName, String email, String phone, String password) {
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    // Default constructor
    public HelperClass() {}

    // Getters and Setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
