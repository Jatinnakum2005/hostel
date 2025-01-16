package com.example.hostel;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class viewroom extends AppCompatActivity {

    private LinearLayout roomsContainer;
    private DatabaseReference databaseReference;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewroom); // Ensure this matches your XML layout filename

        // Initialize views
        roomsContainer = findViewById(R.id.roomsContainer);

        // Get the currently logged-in username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", null);

        if (currentUsername == null) {
            Toast.makeText(this, "Error: User not logged in!", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no user is logged in
            return;
        }

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Fetch all room details and display
        fetchAllRoomDetails();
    }

    private void fetchAllRoomDetails() {
        // Reference to the current user's Rooms node
        DatabaseReference roomsReference = databaseReference.child(currentUsername).child("Rooms");

        roomsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    roomsContainer.removeAllViews(); // Clear any existing views

                    // Loop through all rooms
                    for (DataSnapshot roomSnapshot : dataSnapshot.getChildren()) {
                        String roomNumber = roomSnapshot.getKey();

                        // Get the active status
                        Boolean isActive = roomSnapshot.child("isActive").getValue(Boolean.class);
                        String roomStatus = (isActive != null && isActive) ? "Active" : "Inactive";

                        // Get the number of students
                        DataSnapshot studentsSnapshot = roomSnapshot.child("Students");
                        long studentCount = studentsSnapshot.getChildrenCount();

                        // Calculate available slots
                        int maxSlots = 4; // Assume each room has 4 slots
                        int availableSlots = maxSlots - (int) studentCount;
                        availableSlots = Math.max(availableSlots, 0); // Ensure non-negative

                        // Create a TextView for room details
                        TextView roomDetailsTextView = new TextView(viewroom.this);
                        roomDetailsTextView.setText(String.format(
                                "Room Number: %s\nStatus: %s\nAvailable Slots: %d/%d",
                                roomNumber, roomStatus, availableSlots, maxSlots
                        ));

                        // Create a Spinner (dropdown) for student details
                        Spinner studentDropdown = new Spinner(viewroom.this);
                        List<String> studentDetails = new ArrayList<>();

                        // Populate student details
                        for (DataSnapshot studentSnapshot : studentsSnapshot.getChildren()) {
                            String studentName = studentSnapshot.child("name").getValue(String.class);
                            String studentPRN = studentSnapshot.child("prn").getValue(String.class);
                            if (studentName != null && studentPRN != null) {
                                studentDetails.add(studentName + " (PRN: " + studentPRN + ")");
                            }
                        }

                        if (studentDetails.isEmpty()) {
                            studentDetails.add("No students in this room");
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(viewroom.this, android.R.layout.simple_spinner_item, studentDetails);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        studentDropdown.setAdapter(adapter);

                        // Add the room details and dropdown to the layout
                        LinearLayout roomLayout = new LinearLayout(viewroom.this);
                        roomLayout.setOrientation(LinearLayout.VERTICAL);
                        roomLayout.setPadding(16, 16, 16, 16);

                        roomLayout.addView(roomDetailsTextView);
                        roomLayout.addView(studentDropdown);

                        roomsContainer.addView(roomLayout);
                    }
                } else {
                    Toast.makeText(viewroom.this, "No rooms found for the user!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(viewroom.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
