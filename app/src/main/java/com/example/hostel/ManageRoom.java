package com.example.hostel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ManageRoom extends AppCompatActivity {

    private EditText etRoomNumber, etSearchRoom;
    private TextView tvRoomDetails;
    private CheckBox cbact;
    private Button btnSave, btnSearch, btnDelete, btnview;

    private DatabaseReference databaseReference;

    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_room);

        // Initialize Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize Views
        etRoomNumber = findViewById(R.id.etRoomNumber);
        etSearchRoom = findViewById(R.id.etSearchRoom);
        tvRoomDetails = findViewById(R.id.tvRoomDetails);
        cbact = findViewById(R.id.cbact);
        btnSave = findViewById(R.id.btnSave);
        btnSearch = findViewById(R.id.btnSearch);
        btnDelete = findViewById(R.id.btnDelete);
        btnview = findViewById(R.id.btnViewRooms);

        // Retrieve currentUsername from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", null);

        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(ManageRoom.this, "No user is logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save Room Details
        btnSave.setOnClickListener(v -> {
            String roomNumber = etRoomNumber.getText().toString().trim();

            if (roomNumber.isEmpty()) {
                etRoomNumber.setError("Please enter a room number!");
                etRoomNumber.requestFocus();
                return;
            }

            boolean isActive = cbact.isChecked();

            databaseReference.child(currentUsername).child("Rooms").child(roomNumber)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                etRoomNumber.setError("Room number already exists!");
                                etRoomNumber.requestFocus();
                            } else {
                                databaseReference.child(currentUsername).child("Rooms").child(roomNumber).child("Students")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot studentsSnapshot) {
                                                if (studentsSnapshot.getChildrenCount() >= 4) {
                                                    Toast.makeText(ManageRoom.this, "Cannot add more than 4 students!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Room room = new Room(isActive, isActive ? "Active" : "Inactive");
                                                    databaseReference.child(currentUsername).child("Rooms").child(roomNumber)
                                                            .setValue(room)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(ManageRoom.this, "Room saved successfully!", Toast.LENGTH_SHORT).show();
                                                                etRoomNumber.setText("");
                                                                cbact.setChecked(false);

                                                                databaseReference.child(currentUsername).child("Rooms").child(roomNumber).child("Students")
                                                                        .setValue(null);
                                                            })
                                                            .addOnFailureListener(e -> Toast.makeText(ManageRoom.this, "Failed to save room!", Toast.LENGTH_SHORT).show());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Toast.makeText(ManageRoom.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(ManageRoom.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Search Room Details
        btnSearch.setOnClickListener(v -> {
            String searchRoomNumber = etSearchRoom.getText().toString().trim();

            if (searchRoomNumber.isEmpty()) {
                etSearchRoom.setError("Please enter a room number to search!");
                etSearchRoom.requestFocus();
                return;
            }

            databaseReference.child(currentUsername).child("Rooms").child(searchRoomNumber)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Room room = dataSnapshot.getValue(Room.class);
                                if (room != null) {
                                    tvRoomDetails.setText(String.format("Room: %s\nStatus: %s",
                                            searchRoomNumber, room.status));
                                }
                            } else {
                                tvRoomDetails.setText("Room not found!");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(ManageRoom.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Delete Room
        btnDelete.setOnClickListener(v -> {
            String deleteRoomNumber = etSearchRoom.getText().toString().trim();

            if (deleteRoomNumber.isEmpty()) {
                etSearchRoom.setError("Please enter a room number to delete!");
                etSearchRoom.requestFocus();
                return;
            }

            databaseReference.child(currentUsername).child("Rooms").child(deleteRoomNumber)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                databaseReference.child(currentUsername).child("Rooms").child(deleteRoomNumber)
                                        .removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(ManageRoom.this, "Room deleted successfully!", Toast.LENGTH_SHORT).show();
                                            tvRoomDetails.setText("");
                                            etSearchRoom.setText("");
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(ManageRoom.this, "Failed to delete room!", Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(ManageRoom.this, "Room not found!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(ManageRoom.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // View Rooms
        btnview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageRoom.this, viewroom.class);
                startActivity(intent);
            }
        });
    }

    // Room Data Model
    public static class Room {
        public boolean isActive;
        public String status;

        public Room() {
            // Default constructor required for Firebase
        }

        public Room(boolean isActive, String status) {
            this.isActive = isActive;
            this.status = status;
        }
    }
}
