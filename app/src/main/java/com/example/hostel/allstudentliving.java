package com.example.hostel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class allstudentliving extends AppCompatActivity {

    private EditText searchEditText;
    private TextView studentDetailsTextView;

    private DatabaseReference databaseReference;

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

        // Debugging: Log the username for verification
        Log.d("Username", "Logged-in user: " + username);

        // Initialize Firebase database reference for HostelStudent node
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(username).child("HostelStudent");

        // Initialize UI components
        searchEditText = findViewById(R.id.searchEditText);
        studentDetailsTextView = findViewById(R.id.studentDetailsTextView);

        // Fetch all students initially
        fetchAllStudentData();

        // Add a listener to the search box
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    fetchAllStudentData(); // Fetch all data when the search box is empty
                } else {
                    searchStudentByPRN(query); // Fetch specific student data when searching
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    // Fetch all student data from Firebase Realtime Database
    private void fetchAllStudentData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder detailsBuilder = new StringBuilder();

                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    String prn = studentSnapshot.getKey(); // PRN as the key

                    // Check if student data exists
                    String name = studentSnapshot.child("name").getValue(String.class);
                    String mobile = studentSnapshot.child("mobile").getValue(String.class);
                    String room = studentSnapshot.child("room").getValue(String.class);

                    if (prn != null) {
                        detailsBuilder.append("PRN: ").append(prn).append("\n");

                        // Include additional data if available
                        if (name != null) detailsBuilder.append("Name: ").append(name).append("\n");
                        if (mobile != null) detailsBuilder.append("Mobile: ").append(mobile).append("\n");
                        if (room != null) detailsBuilder.append("Room: ").append(room).append("\n");

                        detailsBuilder.append("\n"); // Add spacing between student entries
                    }
                }

                if (detailsBuilder.length() == 0) {
                    studentDetailsTextView.setText("No hostel students found.");
                } else {
                    studentDetailsTextView.setText(detailsBuilder.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(allstudentliving.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Search for a student by PRN
    private void searchStudentByPRN(String prnQuery) {
        databaseReference.child(prnQuery).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot studentSnapshot) {
                if (studentSnapshot.exists()) {
                    String name = studentSnapshot.child("name").getValue(String.class);
                    String mobile = studentSnapshot.child("mobile").getValue(String.class);
                    String room = studentSnapshot.child("room").getValue(String.class);

                    StringBuilder studentDetails = new StringBuilder();
                    studentDetails.append("PRN: ").append(prnQuery).append("\n");

                    if (name != null) studentDetails.append("Name: ").append(name).append("\n");
                    if (mobile != null) studentDetails.append("Mobile: ").append(mobile).append("\n");
                    if (room != null) studentDetails.append("Room: ").append(room);

                    studentDetailsTextView.setText(studentDetails.toString());
                } else {
                    studentDetailsTextView.setText("No student found with PRN: " + prnQuery);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(allstudentliving.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
