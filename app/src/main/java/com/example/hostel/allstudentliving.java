package com.example.hostel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

        // Initialize Firebase database reference for LivingStudent node
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(username).child("LivingStudent");

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
                    String name = studentSnapshot.child("name").getValue(String.class);
                    String mobile = studentSnapshot.child("mobile").getValue(String.class);
                    String room = studentSnapshot.child("room").getValue(String.class);

                    if (prn != null && name != null && mobile != null && room != null) {
                        detailsBuilder.append("PRN: ").append(prn).append("\n");
                        detailsBuilder.append("Name: ").append(name).append("\n");
                        detailsBuilder.append("Mobile: ").append(mobile).append("\n");
                        detailsBuilder.append("Room: ").append(room).append("\n\n");
                    }
                }

                if (detailsBuilder.length() == 0) {
                    studentDetailsTextView.setText("No living students found.");
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

                    if (name != null && mobile != null && room != null) {
                        String studentDetails = "PRN: " + prnQuery + "\n" +
                                "Name: " + name + "\n" +
                                "Mobile: " + mobile + "\n" +
                                "Room: " + room;

                        studentDetailsTextView.setText(studentDetails);
                    }
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
