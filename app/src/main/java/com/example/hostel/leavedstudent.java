package com.example.hostel;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.model.LivedDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class leavedstudent extends AppCompatActivity {

    private static final String TAG = "LeavedStudentActivity";
    private LinearLayout studentContainer;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leavedstudent);

        studentContainer = findViewById(R.id.student_container);

        // Fetch username (e.g., "Jatin") from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username != null) {
            // Correct Firebase path with dynamic username
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(username)
                    .child("LivedDetails");

            // Fetch data from Firebase
            fetchStudentDetails();
        } else {
            Toast.makeText(this, "User not logged in. Please login first.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchStudentDetails() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    studentContainer.removeAllViews(); // Clear container before adding new data

                    for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                        // Convert each child to LivedDetails model
                        LivedDetails details = studentSnapshot.getValue(LivedDetails.class);

                        if (details != null) {
                            // Assuming PRN is fetched as the key of the studentSnapshot
                            String prn = studentSnapshot.getKey();

                            // Create summary view (PRN, Name, and Mobile)
                            TextView summaryView = new TextView(leavedstudent.this);
                            summaryView.setText("PRN: " + prn + "\n" + "Name: " + details.getName());
                            summaryView.setTextSize(16);
                            summaryView.setTextColor(Color.BLACK);

                            summaryView.setPadding(10, 10, 10, 10);

                            // Create detailed view
                            TextView fullDetailsView = new TextView(leavedstudent.this);
                            String fullDetails = "Name: " + details.getName() + "\n" +
                                    "Address: " + details.getAddress() + "\n" +
                                    "College: " + details.getCollegeName() + "\n" +
                                    "Room: " + details.getRoomNumber() + "\n" +
                                    "Email: " + details.getEmail() + "\n" +
                                    "Father's Name: " + details.getFatherName() + "\n" +
                                    "Mother's Name: " + details.getMotherName() + "\n" +
                                    "Mobile: " + details.getMobile();
                            fullDetailsView.setText(fullDetails);
                            fullDetailsView.setTextSize(14);
                            fullDetailsView.setTextColor(Color.BLUE);
                            fullDetailsView.setPadding(10, 10, 10, 20);
                            fullDetailsView.setVisibility(View.GONE); // Hide by default

                            // Add click listener for toggling details
                            summaryView.setOnClickListener(v -> {
                                if (fullDetailsView.getVisibility() == View.GONE) {
                                    fullDetailsView.setVisibility(View.VISIBLE);
                                } else {
                                    fullDetailsView.setVisibility(View.GONE);
                                }
                            });

                            // Add both views to the container
                            studentContainer.addView(summaryView);
                            studentContainer.addView(fullDetailsView);
                        }
                    }
                } else {
                    Log.e(TAG, "Snapshot does not exist!");
                    Toast.makeText(leavedstudent.this, "No data found in Firebase!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "DatabaseError: " + error.getMessage());
                Toast.makeText(leavedstudent.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
