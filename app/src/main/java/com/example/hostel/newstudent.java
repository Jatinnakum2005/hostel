package com.example.hostel;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class newstudent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newstudent);

        // You can add the functionality for adding a new student here.
        // For now, just show a Toast message to confirm the activity opens.
        Toast.makeText(this, "New Student Activity Opened", Toast.LENGTH_SHORT).show();
    }
}
