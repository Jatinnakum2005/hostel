package com.example.hostel;

import android.os.Bundle;
import android.util.Log;
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

public class studentlist  extends AppCompatActivity {

    private static final String TAG = "StudentsListActivity";

    private ListView listViewStudents;
    private DatabaseReference databaseReference;
    private ArrayList<String> studentList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studentlist);

        // Initialize ListView and Firebase reference
        listViewStudents = findViewById(R.id.listViewStudents);
        databaseReference = FirebaseDatabase.getInstance().getReference("StudentFees");

        // Initialize student list and adapter
        studentList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentList);
        listViewStudents.setAdapter(adapter);

        // Get data from Intent
        String feesStatus = getIntent().getStringExtra("feesStatus");
        String hostelName = getIntent().getStringExtra("hostelName");

        // Validate Intent data
        if (feesStatus == null || hostelName == null) {
            Toast.makeText(this, "Invalid input data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch students from Firebase
        fetchStudents(feesStatus, hostelName);
    }

    private void fetchStudents(String feesStatus, String hostelName) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear(); // Clear existing data

                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    String studentFeesStatus = studentSnapshot.child("feesStatus").getValue(String.class);
                    String studentHostel = studentSnapshot.child("hostel").getValue(String.class);

                    // Filter based on feesStatus and hostelName
                    if (feesStatus.equals(studentFeesStatus) && hostelName.equals(studentHostel)) {
                        String name = studentSnapshot.child("name").getValue(String.class);
                        String room = studentSnapshot.child("room").getValue(String.class);
                        String mobile = studentSnapshot.child("mobile").getValue(String.class);

                        // Avoid null values
                        String studentInfo = "Name: " + (name != null ? name : "N/A") +
                                "\nRoom: " + (room != null ? room : "N/A") +
                                "\nMobile: " + (mobile != null ? mobile : "N/A");

                        studentList.add(studentInfo);
                    }
                }

                if (studentList.isEmpty()) {
                    studentList.add("No students found with " + feesStatus + " in hostel " + hostelName);
                }

                // Notify adapter of data change
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(studentlist.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }
}
