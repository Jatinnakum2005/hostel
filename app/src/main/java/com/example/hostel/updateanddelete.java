package com.example.hostel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.HashMap;

public class updateanddelete extends AppCompatActivity {

    private EditText etPrn, etName, etFatherName, etMotherName, etEmail, etMobileno, etAddress, etCollegeName, etRoomNumber;
    private Spinner spinnerLivingStatus;
    private Button btnSearch, btnUpdate, btnDelete, btnClear;

    private DatabaseReference databaseReference;
    private String currentUsername;
    private DatabaseReference studentNodeRef; // Reference to the current student's node

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
        ArrayAdapter<String> livingStatusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Living", "Leaved"});
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
                studentNodeRef = null;

                // Check in Rooms
                DataSnapshot roomsSnapshot = snapshot.child("Rooms");
                for (DataSnapshot roomSnapshot : roomsSnapshot.getChildren()) {
                    DataSnapshot studentsSnapshot = roomSnapshot.child("Students");

                    for (DataSnapshot studentSnapshot : studentsSnapshot.getChildren()) {
                        String studentPrn = studentSnapshot.child("prn").getValue(String.class);

                        if (prn.equals(studentPrn)) {
                            found = true;
                            studentNodeRef = studentSnapshot.getRef(); // Save reference to the student node
                            fillStudentDetails(studentSnapshot);
                            etRoomNumber.setText(roomSnapshot.getKey()); // Display room number
                            break;
                        }
                    }

                    if (found) break;
                }

                // Check in HostelStudent if not found in Rooms
                if (!found) {
                    DataSnapshot hostelStudentsSnapshot = snapshot.child("HostelStudent");
                    if (hostelStudentsSnapshot.hasChild(prn)) {
                        found = true;
                        studentNodeRef = hostelStudentsSnapshot.child(prn).getRef(); // Save reference to the student node
                        fillStudentDetails(hostelStudentsSnapshot.child(prn));
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

    private void fillStudentDetails(DataSnapshot studentSnapshot) {
        etName.setText(studentSnapshot.child("name").getValue(String.class));
        etFatherName.setText(studentSnapshot.child("fatherName").getValue(String.class));
        etMotherName.setText(studentSnapshot.child("motherName").getValue(String.class));
        etEmail.setText(studentSnapshot.child("email").getValue(String.class));
        etMobileno.setText(studentSnapshot.child("mobile").getValue(String.class));
        etAddress.setText(studentSnapshot.child("address").getValue(String.class));
        etCollegeName.setText(studentSnapshot.child("collegeName").getValue(String.class));
        spinnerLivingStatus.setSelection("Leaved".equals(studentSnapshot.child("status").getValue(String.class)) ? 1 : 0); // Set Living status
    }

    private void updateStudent() {
        if (studentNodeRef == null) {
            Toast.makeText(this, "Please search for a student first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String fatherName = etFatherName.getText().toString().trim();
        String motherName = etMotherName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobileno.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String collegeName = etCollegeName.getText().toString().trim();
        String roomNumber = etRoomNumber.getText().toString().trim();
        String livingStatus = spinnerLivingStatus.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "Name, Email, and Mobile cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("fatherName", fatherName);
        updates.put("motherName", motherName);
        updates.put("email", email);
        updates.put("mobile", mobile);
        updates.put("address", address);
        updates.put("collegeName", collegeName);
        updates.put("status", livingStatus);

        studentNodeRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(updateanddelete.this, "Student details updated successfully!", Toast.LENGTH_SHORT).show();

            if ("Living".equals(livingStatus)) {
                updateExistingLivingStudentAndRoom(updates, roomNumber);
            } else if ("Left".equals(livingStatus)) {
                moveToLeftStudents(updates);
                removeFromRoomAndHostelStudent(roomNumber);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(updateanddelete.this, "Failed to update student details!", Toast.LENGTH_SHORT).show();
        });
    }

    private void moveToLeftStudents(HashMap<String, Object> updates) {
        String prn = etPrn.getText().toString().trim();
        String roomNumber = etRoomNumber.getText().toString().trim();

        if (prn.isEmpty()) {
            Toast.makeText(this, "Please enter PRN", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!roomNumber.isEmpty()) {
            // Add Room Number to the updates map
            updates.put("roomNumber", roomNumber);
        }

        DatabaseReference leftStudentsRef = databaseReference.child("LeftStudents").child(prn);
        leftStudentsRef.setValue(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(updateanddelete.this, "Student moved to LeftStudents with Room Number", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(updateanddelete.this, "Failed to move student to LeftStudents!", Toast.LENGTH_SHORT).show();
        });

        // Remove student from HostelStudent
        databaseReference.child("HostelStudent").child(prn).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(updateanddelete.this, "Student removed from HostelStudent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(updateanddelete.this, "Failed to remove student from HostelStudent", Toast.LENGTH_SHORT).show();
                });
    }


    private void updateExistingLivingStudentAndRoom(HashMap<String, Object> updates, String roomNumber) {
        if (roomNumber.isEmpty()) {
            Toast.makeText(this, "Please enter Room Number", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference roomRef = databaseReference.child("Rooms").child(roomNumber).child("Students").child(etPrn.getText().toString().trim());
        roomRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(updateanddelete.this, "Student details updated in Room successfully!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(updateanddelete.this, "Failed to update student in Room!", Toast.LENGTH_SHORT).show();
        });

        DatabaseReference hostelStudentRef = databaseReference.child("HostelStudent").child(etPrn.getText().toString().trim());
        hostelStudentRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(updateanddelete.this, "Student details updated in HostelStudent successfully!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(updateanddelete.this, "Failed to update student in HostelStudent!", Toast.LENGTH_SHORT).show();
        });
    }

    private void removeFromRoomAndHostelStudent(String roomNumber) {
        String prn = etPrn.getText().toString().trim();
        if (prn.isEmpty()) {
            Toast.makeText(this, "Please enter PRN", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!roomNumber.isEmpty()) {
            databaseReference.child("Rooms").child(roomNumber).child("Students").child(prn).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(updateanddelete.this, "Student removed from Room", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(updateanddelete.this, "Failed to remove student from Room", Toast.LENGTH_SHORT).show();
                    });
        }

        databaseReference.child("HostelStudent").child(prn).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(updateanddelete.this, "Student removed from HostelStudent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(updateanddelete.this, "Failed to remove student from HostelStudent", Toast.LENGTH_SHORT).show();
                });
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
    }

    private void deleteStudent() {
        String prn = etPrn.getText().toString().trim();

        if (prn.isEmpty()) {
            Toast.makeText(this, "Please search for a student first!", Toast.LENGTH_SHORT).show();
            return;
        }

        studentNodeRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Student data deleted successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete student!", Toast.LENGTH_SHORT).show();
                });
    }
}