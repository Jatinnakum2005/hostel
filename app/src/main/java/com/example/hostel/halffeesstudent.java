package com.example.hostel;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class halffeesstudent extends AppCompatActivity {

    private ListView halfFeesListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> studentList;
    private DatabaseReference databaseReference;
    private String loggedInUsername; // To store the logged-in username (same as hostel name)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halffeesstudent);

        // Retrieve logged-in username from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        loggedInUsername = preferences.getString("username", ""); // Retrieve username as hostel name

        if (loggedInUsername.isEmpty()) {
            Toast.makeText(this, "User not logged in. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("StudentFees");

        // Initialize ListView and ArrayList
        halfFeesListView = findViewById(R.id.halfFeesListView);
        studentList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentList);
        halfFeesListView.setAdapter(adapter);

        // Fetch students with "Half Fees Paid" status and matching hostel name (username)
        fetchHalfFeesStudents();
    }

    private void fetchHalfFeesStudents() {
        databaseReference.orderByChild("hostel").equalTo(loggedInUsername) // Query to filter by hostel name
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        studentList.clear();
                        for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                            String feesStatus = studentSnapshot.child("feesStatus").getValue(String.class);

                            // Check if the fees status is "Half Fees Paid"
                            if ("Half Fees Paid".equals(feesStatus)) {
                                String name = studentSnapshot.child("name").getValue(String.class);
                                String room = studentSnapshot.child("room").getValue(String.class);
                                String mobile = studentSnapshot.child("mobile").getValue(String.class);

                                String studentInfo = "Name: " + name + "\nRoom: " + room + "\nMobile: " + mobile;
                                studentList.add(studentInfo);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (studentList.isEmpty()) {
                            Toast.makeText(halffeesstudent.this, "No students found with 'Half Fees Paid' for your hostel.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(halffeesstudent.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
