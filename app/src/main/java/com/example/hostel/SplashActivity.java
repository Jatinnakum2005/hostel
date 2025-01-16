package com.example.hostel;  // Replace with your package name

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Use a Handler to delay the transition to the main screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the main activity after a delay
                Intent intent = new Intent(SplashActivity.this, loginpage.class); // Change MainActivity if needed
                startActivity(intent);
                finish();  // Close the splash screen
            }
        }, 5000); // 3 seconds delay
    }
}
