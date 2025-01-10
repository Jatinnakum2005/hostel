package com.example.hostel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class updateanddelete extends AppCompatActivity {

    private EditText etPrn, etName, etFatherName, etMotherName, etEmail, etMobileno, etAddress, etCollegeName, etRoomNumber;
    private Spinner spinnerLivingStatus;
    private Button btnSearch, btnUpdate, btnDelete, btnClear;

    private DatabaseReference databaseReference;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateanddelete);

        // Get currently logged-in username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", null);

        if (currentUsername == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish(); // Exit the activity if no username is found
            return;
        }

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUsername);

        // Initialize UI components
        etPrn = findViewById(R.id.etprn);
        etName = findViewById(R.id.etName);
        etFatherName = findViewById(R.id.etFatherName);
        etMotherName = findViewById(R.id.etMotherName);
        etEmail = findViewById(R.id.etEmail);
        etMobileno = findViewById(R.id.etmobileno);
        etAddress = findViewById(R.id.etAddress);
        etCollegeName = findViewById(R.id.etCollegeName);
        etRoomNumber = findViewById(R.id.etRoomNumber);
        spinnerLivingStatus = findViewById(R.id.spinnerLivingStatus);

        btnSearch = findViewById(R.id.btnSearch);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnClear = findViewById(R.id.btnClear);

        // Set Spinner options
        ArrayAdapter<String> livingStatusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Living", "Lived"});
        livingStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLivingStatus.setAdapter(livingStatusAdapter);

        // Set button listeners
        btnSearch.setOnClickListener(v -> searchStudent());
        btnUpdate.setOnClickListener(v -> updateStudent());
        btnDelete.setOnClickListener(v -> deleteStudent());
        btnClear.setOnClickListener(v -> clearFields());
    }

    private void searchStudent() {
        String prn = etPrn.getText().toString().trim();

        if (prn.isEmpty() || (!prn.matches("\\d{10}") && !prn.matches("\\d{16}"))) {
            Toast.makeText(this, "Please enter a valid 10 or 16-digit PRN", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;

                // Check in LivedDetails
                DataSnapshot livedDetailsSnapshot = snapshot.child("LivedDetails");
                if (livedDetailsSnapshot.hasChild(prn)) {
                    found = true;

                    DataSnapshot studentSnapshot = livedDetailsSnapshot.child(prn);
                    etName.setText(studentSnapshot.child("name").getValue(String.class));
                    etFatherName.setText(studentSnapshot.child("fatherName").getValue(String.class));
                    etMotherName.setText(studentSnapshot.child("motherName").getValue(String.class));
                    etEmail.setText(studentSnapshot.child("email").getValue(String.class));
                    etMobileno.setText(studentSnapshot.child("mobile").getValue(String.class));
                    etAddress.setText(studentSnapshot.child("address").getValue(String.class));
                    etCollegeName.setText(studentSnapshot.child("collegeName").getValue(String.class));
                    spinnerLivingStatus.setSelection(1); // Set to "Lived"
                }

                // Check in Rooms if not found in LivedDetails
                if (!found) {
                    DataSnapshot roomsSnapshot = snapshot.child("Rooms");
                    for (DataSnapshot roomSnapshot : roomsSnapshot.getChildren()) {
                        DataSnapshot studentsSnapshot = roomSnapshot.child("Students");

                        for (DataSnapshot studentSnapshot : studentsSnapshot.getChildren()) {
                            String studentPrn = studentSnapshot.child("prn").getValue(String.class);

                            if (prn.equals(studentPrn)) {
                                found = true;

                                etName.setText(studentSnapshot.child("name").getValue(String.class));
                                etFatherName.setText(studentSnapshot.child("fatherName").getValue(String.class));
                                etMotherName.setText(studentSnapshot.child("motherName").getValue(String.class));
                                etEmail.setText(studentSnapshot.child("email").getValue(String.class));
                                etMobileno.setText(studentSnapshot.child("mobile").getValue(String.class));
                                etAddress.setText(studentSnapshot.child("address").getValue(String.class));
                                etCollegeName.setText(studentSnapshot.child("collegeName").getValue(String.class));
                                etRoomNumber.setText(roomSnapshot.getKey());
                                spinnerLivingStatus.setSelection(0); // Set to "Living"
                                break;
                            }
                        }

                        if (found) break;
                    }
                }

                if (!found) {
                    Toast.makeText(updateanddelete.this, "PRN not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(updateanddelete.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStudent() {
        String prn = etPrn.getText().toString().trim();
        if (prn.isEmpty()) {
            Toast.makeText(this, "Please enter PRN", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String fatherName = etFatherName.getText().toString().trim();
        String motherName = etMotherName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobileno.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String collegeName = etCollegeName.getText().toString().trim();
        String livingStatus = spinnerLivingStatus.getSelectedItem().toString();

        if ("Lived".equals(livingStatus)) {
            DatabaseReference livedDetailsRef = databaseReference.child("LivedDetails").child(prn);
            livedDetailsRef.child("name").setValue(name);
            livedDetailsRef.child("fatherName").setValue(fatherName);
            livedDetailsRef.child("motherName").setValue(motherName);
            livedDetailsRef.child("email").setValue(email);
            livedDetailsRef.child("mobile").setValue(mobile);
            livedDetailsRef.child("address").setValue(address);
            livedDetailsRef.child("collegeName").setValue(collegeName);

            // Delete the student from LivingStudent
            databaseReference.child("LivingStudent").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean studentFound = false;

                    for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                        String studentPrn = studentSnapshot.child("prn").getValue(String.class);

                        if (prn.equals(studentPrn)) {
                            studentFound = true;
                            studentSnapshot.getRef().removeValue();
                            break;
                        }
                    }

                    if (studentFound) {
                        Toast.makeText(updateanddelete.this, "Student moved to LivedDetails successfully and removed from LivingStudent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(updateanddelete.this, "Student not found in LivingStudent", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(updateanddelete.this, "Failed to move student to LivedDetails", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteStudent() {
        String prn = etPrn.getText().toString().trim();
        if (prn.isEmpty()) {
            Toast.makeText(this, "Please enter PRN", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("LivedDetails").child(prn).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(updateanddelete.this, "Student deleted successfully from LivedDetails", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> Toast.makeText(updateanddelete.this, "Failed to delete student", Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        etPrn.setText("");
        etName.setText("");
        etFatherName.setText("");
        etMotherName.setText("");
        etEmail.setText("");
        etMobileno.setText("");
        etAddress.setText("");
        etCollegeName.setText("");
        etRoomNumber.setText("");
        spinnerLivingStatus.setSelection(0);
    }
}
