package com.example.hostel; // Replace with your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 3000; // 3 seconds delay
    private Handler handler;
    private Runnable navigateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Log.d(TAG, "Splash screen started");

        // Initialize the handler with the main looper for reliability
        handler = new Handler(Looper.getMainLooper());

        // Create a runnable to navigate to the next activity
        navigateRunnable = () -> {
            try {
                // Replace loginpage.class with your actual main activity
                Intent intent = new Intent(SplashActivity.this, loginpage.class);
                startActivity(intent);
                finish(); // Close the splash activity
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to next activity", e);
            }
        };

        // Post the runnable with a delay
        handler.postDelayed(navigateRunnable, SPLASH_DELAY);
    }

    @Override
    protected void onDestroy() {
        // Remove callbacks to prevent memory leaks
        if (handler != null && navigateRunnable != null) {
            handler.removeCallbacks(navigateRunnable);
        }
        super.onDestroy();
    }
}
