package com.example.hostel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
        Button manageRoomButton = findViewById(R.id.button8);
        manageRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the ManageRoom activity
                Intent intent = new Intent(mainpage.this, ManageRoom.class);
                startActivity(intent);
            }
        });

        Button newStudentButton = findViewById(R.id.button9);
        newStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the NewStudent activity
                Intent intent = new Intent(mainpage.this, newstudent.class);
                startActivity(intent);
            }
        });

        Button updateDeleteStudentButton = findViewById(R.id.button10);
        updateDeleteStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the UpdateAndDeleteStudent activity
                Intent intent = new Intent(mainpage.this, updateanddelete.class);
                startActivity(intent);
            }
        });

        Button studentFeesButton = findViewById(R.id.button11);
        studentFeesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the StudentFees activity
                Intent intent = new Intent(mainpage.this, studentfees.class);
                startActivity(intent);
            }
        });

        Button allStudentLivingButton = findViewById(R.id.button12);
        allStudentLivingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the AllStudentLiving activity
                Intent intent = new Intent(mainpage.this, allstudentliving.class);
                startActivity(intent);
            }
        });

        Button leavedStudentsButton = findViewById(R.id.button13);
        leavedStudentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the LeavedStudents activity
                Intent intent = new Intent(mainpage.this, leavedstudent.class);
                startActivity(intent);
            }
        });

        Button logoutButton = findViewById(R.id.button);
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

        // Redirect to MainActivity
        Intent intent = new Intent(mainpage.this, MainActivity.class); // Replace with your main activity name
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear activity stack
        startActivity(intent);

        // Finish the current activity
        finish();

        // Show a toast message
        Toast.makeText(mainpage.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
    }
}
