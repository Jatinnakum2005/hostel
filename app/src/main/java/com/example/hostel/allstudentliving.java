package com.example.hostel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
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

public class allstudentliving extends AppCompatActivity {

    private Spinner spinner;
    private TextView studentDetailsTextView;

    private DatabaseReference databaseReference;
    private ArrayList<String> studentList; // PRN list
    private Map<String, Map<String, String>> studentDetailsMap; // Map to store details of each student
    private String selectedPRN = "";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allstudentliving);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        // Fetch username dynamically from SharedPreferences
        String username = sharedPreferences.getString("username", null);
        if (username == null) {
            Toast.makeText(this, "User not logged in. Please log in first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase database reference for LivingStudent node
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(username).child("LivingStudent");

        // Initialize UI components
        spinner = findViewById(R.id.spinner);
        studentDetailsTextView = findViewById(R.id.studentDetailsTextView);

        studentList = new ArrayList<>();
        studentDetailsMap = new HashMap<>();

        // Fetch student data from Firebase
        fetchStudentData();

        // Handle Spinner item selection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPRN = studentList.get(position); // Get the selected PRN
                displayStudentDetails(selectedPRN); // Display the details for the selected PRN
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPRN = "";
                studentDetailsTextView.setText(""); // Clear the details view
            }
        });
    }

    // Fetch student data from Firebase Realtime Database
    private void fetchStudentData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                studentDetailsMap.clear();

                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    String prn = studentSnapshot.getKey(); // PRN as the key
                    Map<String, String> studentData = (Map<String, String>) studentSnapshot.getValue();

                    if (prn != null && studentData != null) {
                        studentList.add(prn); // Add PRN to the list
                        studentDetailsMap.put(prn, studentData); // Store student details in the map
                    }
                }

                if (studentList.isEmpty()) {
                    Toast.makeText(allstudentliving.this, "No living students found.", Toast.LENGTH_SHORT).show();
                } else {
                    updateSpinner();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(allstudentliving.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update the spinner with student PRN list
    private void updateSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studentList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // Display student details for the selected PRN
    private void displayStudentDetails(String prn) {
        Map<String, String> studentData = studentDetailsMap.get(prn);

        if (studentData != null) {
            StringBuilder detailsBuilder = new StringBuilder();
            detailsBuilder.append("PRN: ").append(prn).append("\n");
            detailsBuilder.append("Name: ").append(studentData.get("name")).append("\n");
            detailsBuilder.append("Mobile: ").append(studentData.get("mobile")).append("\n");
            detailsBuilder.append("Room: ").append(studentData.get("room")).append("\n");
            detailsBuilder.append("College Name: ").append(studentData.get("collegeName")).append("\n");
            detailsBuilder.append("Address: ").append(studentData.get("address")).append("\n");

            studentDetailsTextView.setText(detailsBuilder.toString());
        } else {
            studentDetailsTextView.setText("No details available for the selected student.");
        }
    }
}
