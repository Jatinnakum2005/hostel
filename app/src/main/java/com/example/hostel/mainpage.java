package com.example.hostel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class mainpage extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for the main page
        setContentView(R.layout.activity_mainpage);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Set click listeners for each button
        LinearLayout manageRoomButton = findViewById(R.id.img_room);
        manageRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the ManageRoom activity
                Intent intent = new Intent(mainpage.this, ManageRoom.class);
                startActivity(intent);
            }
        });

        LinearLayout newStudentButton = findViewById(R.id.img_student);
        newStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the NewStudent activity
                Intent intent = new Intent(mainpage.this, newstudent.class);
                startActivity(intent);
            }
        });

        LinearLayout updateDeleteStudentButton = findViewById(R.id.img_undesignated_student);
        updateDeleteStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the UpdateAndDeleteStudent activity
                Intent intent = new Intent(mainpage.this, updateanddelete.class);
                startActivity(intent);
            }
        });

        LinearLayout studentFeesButton = findViewById(R.id.img_student_fees);
        studentFeesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the StudentFees activity
                Intent intent = new Intent(mainpage.this, studentfees.class);
                startActivity(intent);
            }
        });

        LinearLayout allStudentLivingButton = findViewById(R.id.img_living_student);
        allStudentLivingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the AllStudentLiving activity
                Intent intent = new Intent(mainpage.this, allstudentliving.class);
                startActivity(intent);
            }
        });

        LinearLayout leavedStudentsButton = findViewById(R.id.img_leaved_student);
        leavedStudentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the LeavedStudents activity
                Intent intent = new Intent(mainpage.this, leavedstudent.class);
                startActivity(intent);
            }
        });

        LinearLayout feedbackButton = findViewById(R.id.img_feedback);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the Feedback activity
                Intent intent = new Intent(mainpage.this, feedback.class);
                startActivity(intent);
            }
        });

        LinearLayout logoutButton = findViewById(R.id.img_logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    private void logoutUser() {
        // Clear SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear all stored preferences
        editor.apply();

        // Redirect to LoginActivity
        Intent intent = new Intent(mainpage.this, loginpage.class); // Replace with your login activity name
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear activity stack
        startActivity(intent);

        // Finish the current activity
        finish();

        // Show a toast message
        Toast.makeText(mainpage.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
    }
}
