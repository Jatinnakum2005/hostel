package com.example.hostel;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class updateanddelete extends AppCompatActivity {

    private EditText etPrn, etName, etFatherName, etMotherName, etEmail, etAddress, etCollegeName, etAadhaar, etRoomNumber;
    private Spinner spinnerLivingStatus;
    private Button btnSearch, btnUpdate, btnDelete, btnClear;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateanddelete);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI components
        etPrn = findViewById(R.id.etprn);
        etName = findViewById(R.id.etName);
        etFatherName = findViewById(R.id.etFatherName);
        etMotherName = findViewById(R.id.etMotherName);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etCollegeName = findViewById(R.id.etCollegeName);
        etAadhaar = findViewById(R.id.etAadhaar);
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

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Check in LivedDetails
                    DataSnapshot livedDetailsSnapshot = userSnapshot.child("LivedDetails");
                    if (livedDetailsSnapshot.hasChild(prn)) {
                        found = true;

                        DataSnapshot studentSnapshot = livedDetailsSnapshot.child(prn);
                        etName.setText(studentSnapshot.child("name").getValue(String.class));
                        etFatherName.setText(studentSnapshot.child("fatherName").getValue(String.class));
                        etMotherName.setText(studentSnapshot.child("motherName").getValue(String.class));
                        etEmail.setText(studentSnapshot.child("email").getValue(String.class));
                        etAddress.setText(studentSnapshot.child("address").getValue(String.class));
                        etCollegeName.setText(studentSnapshot.child("collegeName").getValue(String.class));
                        spinnerLivingStatus.setSelection(1); // Set to "Lived"
                        break;
                    }

                    // Check in Rooms
                    DataSnapshot roomsSnapshot = userSnapshot.child("Rooms");
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
                                etAddress.setText(studentSnapshot.child("address").getValue(String.class));
                                etCollegeName.setText(studentSnapshot.child("collegeName").getValue(String.class));
                                etRoomNumber.setText(roomSnapshot.getKey());
                                spinnerLivingStatus.setSelection(0); // Set to "Living"
                                break;
                            }
                        }

                        if (found) break;
                    }

                    if (found) break;
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
        String address = etAddress.getText().toString().trim();
        String collegeName = etCollegeName.getText().toString().trim();
        String livingStatus = spinnerLivingStatus.getSelectedItem().toString();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean studentFound = false;
                String roomNumber = null;
                String userKey = null;

                // Search for the PRN across users
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    userKey = userSnapshot.getKey();

                    // Check in Rooms
                    DataSnapshot roomsSnapshot = userSnapshot.child("Rooms");
                    for (DataSnapshot roomSnapshot : roomsSnapshot.getChildren()) {
                        String currentRoomNumber = roomSnapshot.getKey();
                        DataSnapshot studentsSnapshot = roomSnapshot.child("Students");

                        for (DataSnapshot studentSnapshot : studentsSnapshot.getChildren()) {
                            String studentPrn = studentSnapshot.child("prn").getValue(String.class);

                            if (prn.equals(studentPrn)) {
                                studentFound = true;
                                roomNumber = currentRoomNumber;
                                break;
                            }
                        }

                        if (studentFound) break;
                    }

                    if (studentFound) break;
                }

                if (studentFound && roomNumber != null && userKey != null) {
                    DatabaseReference studentRef = databaseReference.child(userKey).child("Rooms").child(roomNumber).child("Students").child(prn);

                    if (livingStatus.equals("Lived")) {
                        DatabaseReference livedRef = databaseReference.child(userKey).child("LivedDetails").child(prn);

                        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot studentSnapshot) {
                                if (studentSnapshot.exists()) {
                                    livedRef.setValue(studentSnapshot.getValue()).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            studentRef.removeValue().addOnCompleteListener(removeTask -> {
                                                if (removeTask.isSuccessful()) {
                                                    Toast.makeText(updateanddelete.this, "Student moved to LivedDetails", Toast.LENGTH_SHORT).show();
                                                    clearFields();
                                                } else {
                                                    Toast.makeText(updateanddelete.this, "Failed to remove student from current room", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            Toast.makeText(updateanddelete.this, "Failed to move student to LivedDetails", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(updateanddelete.this, "Student not found in the current room", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(updateanddelete.this, "Failed to fetch student data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        studentRef.child("name").setValue(name);
                        studentRef.child("fatherName").setValue(fatherName);
                        studentRef.child("motherName").setValue(motherName);
                        studentRef.child("email").setValue(email);
                        studentRef.child("address").setValue(address);
                        studentRef.child("collegeName").setValue(collegeName);
                        studentRef.child("livingStatus").setValue(livingStatus);

                        Toast.makeText(updateanddelete.this, "Student details updated", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(updateanddelete.this, "PRN not found in any room", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(updateanddelete.this, "Failed to fetch room data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteStudent() {
        String prn = etPrn.getText().toString().trim();
        String livingStatus = spinnerLivingStatus.getSelectedItem().toString();

        if (prn.isEmpty()) {
            Toast.makeText(this, "Please provide PRN", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean deleted = false;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Delete from LivedDetails if "Lived" is selected
                    if (livingStatus.equals("Lived")) {
                        DataSnapshot livedDetailsSnapshot = userSnapshot.child("LivedDetails");
                        if (livedDetailsSnapshot.hasChild(prn)) {
                            livedDetailsSnapshot.child(prn).getRef().removeValue();
                            deleted = true;
                            break;
                        }
                    } else {
                        // Delete from Rooms if "Living" is selected
                        DataSnapshot roomsSnapshot = userSnapshot.child("Rooms");
                        for (DataSnapshot roomSnapshot : roomsSnapshot.getChildren()) {
                            DataSnapshot studentsSnapshot = roomSnapshot.child("Students");
                            if (studentsSnapshot.hasChild(prn)) {
                                studentsSnapshot.child(prn).getRef().removeValue();
                                deleted = true;
                                break;
                            }
                        }
                    }

                    if (deleted) break;
                }

                if (deleted) {
                    Toast.makeText(updateanddelete.this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                } else {
                    Toast.makeText(updateanddelete.this, "PRN not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(updateanddelete.this, "Failed to delete student", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        etPrn.setText("");
        etName.setText("");
        etFatherName.setText("");
        etMotherName.setText("");
        etEmail.setText("");
        etAddress.setText("");
        etCollegeName.setText("");
        etAadhaar.setText("");
        etRoomNumber.setText("");
        spinnerLivingStatus.setSelection(0);
    }
}
