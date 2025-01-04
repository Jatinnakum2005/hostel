package com.example.hostel;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class viewroom extends AppCompatActivity {

    private TextView tvRoomDetails;
    private DatabaseReference databaseReference;
    private String currentUsername = "Jatin"; // Replace with dynamic username from shared preferences or intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewroom);

        // Initialize views
        tvRoomDetails = findViewById(R.id.tvRoomDetails);

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
                    StringBuilder roomDetails = new StringBuilder();

                    // Loop through all rooms
                    for (DataSnapshot roomSnapshot : dataSnapshot.getChildren()) {
                        String roomNumber = roomSnapshot.getKey();

                        // Get the active status
                        boolean isActive = roomSnapshot.child("isActive").getValue(Boolean.class);
                        String roomStatus = isActive ? "Active" : "Inactive";

                        // Get the number of students
                        DataSnapshot studentsSnapshot = roomSnapshot.child("Students");
                        long studentCount = studentsSnapshot.getChildrenCount();

                        // Calculate available slots
                        int maxSlots = 4;
                        int availableSlots = maxSlots - (int) studentCount;
                        availableSlots = Math.max(availableSlots, 0); // Ensure non-negative

                        // Append room details to the StringBuilder
                        roomDetails.append(String.format("Room Number: %s\n", roomNumber));
                        roomDetails.append(String.format("Status: %s\n", roomStatus));
                        roomDetails.append(String.format("Available Slots: %d/%d\n", availableSlots, maxSlots));
                        roomDetails.append("-----------------------------\n");
                    }

                    // Set the constructed room details to the TextView
                    tvRoomDetails.setText(roomDetails.toString());
                } else {
                    tvRoomDetails.setText("No rooms found!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(viewroom.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}