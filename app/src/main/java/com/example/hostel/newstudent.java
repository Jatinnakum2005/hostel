package com.example.hostel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class newstudent extends AppCompatActivity {

    private EditText editText1, editText2, editText3, editText4, editText5, editText6, editText7, editText8;
    private Spinner spinner;
    private Button btnSave, btnClear;

    private DatabaseReference databaseReference;
    private String selectedRoom = "";
    private ArrayList<String> roomList;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newstudent);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        // Fetch username dynamically from SharedPreferences
        String username = sharedPreferences.getString("username", null);

        // Initialize Firebase database reference dynamically
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(username).child("Rooms");

        // Initialize UI components
        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        editText3 = findViewById(R.id.editText3);
        editText4 = findViewById(R.id.editText4);
        editText5 = findViewById(R.id.editText5);
        editText6 = findViewById(R.id.editText6);
        editText7 = findViewById(R.id.editText7);
        editText8 = findViewById(R.id.editText8);
        spinner = findViewById(R.id.spinner);
        btnSave = findViewById(R.id.btnSave);
        btnClear = findViewById(R.id.btnClear);

        roomList = new ArrayList<>();

        // Fetch room data from Firebase and populate spinner
        fetchRoomData();

        // Handle Spinner item selection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoom = roomList.get(position); // Get the selected room
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRoom = "";
            }
        });

        // Handle Save button click
        btnSave.setOnClickListener(v -> validateRoomCapacity(username));

        // Handle Clear button click
        btnClear.setOnClickListener(v -> clearFields());
    }

    // Fetch room data from Firebase Realtime Database
    private void fetchRoomData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomList.clear(); // Clear the previous list
                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                    roomList.add(roomSnapshot.getKey()); // Add room numbers to the list
                }
                updateSpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(newstudent.this, "Failed to fetch rooms: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update the spinner with room data
    private void updateSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roomList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // Validate room capacity before saving student data
    private void validateRoomCapacity(String username) {
        if (selectedRoom.isEmpty()) {
            Toast.makeText(this, "Please select a room.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the selected room's students node
        DatabaseReference studentsRef = databaseReference.child(selectedRoom).child("Students");

        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long studentCount = snapshot.getChildrenCount();
                if (studentCount >= 4) {
                    Toast.makeText(newstudent.this, "Room is full. Cannot add more than 4 students.", Toast.LENGTH_SHORT).show();
                } else {
                    saveStudentData(username); // Proceed to save student data
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(newstudent.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Save student data to Firebase
    private void saveStudentData(String username) {
        // Get student details from input fields
        String mobile = editText1.getText().toString().trim();
        String name = editText2.getText().toString().trim();
        String fatherName = editText3.getText().toString().trim();
        String motherName = editText4.getText().toString().trim();
        String email = editText5.getText().toString().trim();
        String address = editText6.getText().toString().trim();
        String collegeName = editText7.getText().toString().trim();
        String prn = editText8.getText().toString().trim();

        // Validate fields
        if (mobile.isEmpty()) {
            editText1.setError("Mobile number is required");
            editText1.requestFocus();
            return;
        }
        if (!mobile.matches("\\d+")) {
            editText1.setError("Mobile number must contain only digits");
            editText1.requestFocus();
            return;
        }
        if (name.isEmpty()) {
            editText2.setError("Name is required");
            editText2.requestFocus();
            return;
        }
        if (prn.isEmpty()) {
            editText8.setError("PRN is required");
            editText8.requestFocus();
            return;
        }
        if (!prn.matches("\\d{10,16}")) {
            editText8.setError("PRN must contain 10 to 16 digits only");
            editText8.requestFocus();
            return;
        }

        // Save student data to both nodes
        saveStudentToFirebase(mobile, name, fatherName, motherName, email, address, collegeName, prn);
        saveStudentToLivingStudent(mobile, name, fatherName, motherName, email, address, collegeName, prn);
    }

    // Save student to the Rooms node
    private void saveStudentToFirebase(String mobile, String name, String fatherName, String motherName,
                                       String email, String address, String collegeName, String prn) {
        DatabaseReference roomRef = databaseReference.child(selectedRoom).child("Students");

        // Create a student object
        Map<String, String> student = new HashMap<>();
        student.put("mobile", mobile);
        student.put("name", name);
        student.put("fatherName", fatherName);
        student.put("motherName", motherName);
        student.put("email", email);
        student.put("address", address);
        student.put("collegeName", collegeName);
        student.put("prn", prn);

        // Save student data to Firebase using PRN as the key
        roomRef.child(prn).setValue(student)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(newstudent.this, "Done", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> Toast.makeText(newstudent.this, "Failed to add student: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Save student to the LivingStudent node
    private void saveStudentToLivingStudent(String mobile, String name, String fatherName, String motherName,
                                            String email, String address, String collegeName, String prn) {
        String username = sharedPreferences.getString("username", null); // Get the logged-in user's username
        if (username == null) {
            Toast.makeText(this, "User not logged in. Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to LivingStudent node
        DatabaseReference livingStudentRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(username).child("LivingStudent");

        // Create the student object including the room number
        Map<String, Object> student = new HashMap<>();
        student.put("mobile", mobile);
        student.put("name", name);
        student.put("fatherName", fatherName);
        student.put("motherName", motherName);
        student.put("email", email);
        student.put("address", address);
        student.put("collegeName", collegeName);
        student.put("prn", prn);
        student.put("room", selectedRoom); // Add the room number to the student details

        // Save student data to the LivingStudent node using PRN as the key
        livingStudentRef.child(prn).setValue(student)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Student Added.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add student." + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Clear all input fields
    private void clearFields() {
        editText1.setText("");
        editText2.setText("");
        editText3.setText("");
        editText4.setText("");
        editText5.setText("");
        editText6.setText("");
        editText7.setText("");
        editText8.setText("");
        spinner.setSelection(0);
        selectedRoom = "";
    }
}
