package com.example.model;

public class LivedDetails {
    private String name;
    private String prn;
    private String address;
    private String collegeName;
    private String email;

    private String roomNumber;
    private String fatherName;
    private String motherName;
    private String mobile;

    // Default constructor (required for Firebase)
    public LivedDetails() {}

    // Getters
    public String getName() {
        return name;
    }

    public String getPrn() {
        return prn;
    }

    public String getAddress() {
        return address;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public String getEmail() {
        return email;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getFatherName() {
        return fatherName;
    }

    public String getMotherName() {
        return motherName;
    }

    public String getMobile() {
        return mobile;
    }

    // Setters (optional, required only if you plan to modify data)
    public void setName(String name) {
        this.name = name;
    }

    public void setPrn(String prn) {
        this.prn = prn;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
