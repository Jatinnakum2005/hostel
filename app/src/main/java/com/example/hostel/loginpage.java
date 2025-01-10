package com.example.hostel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class loginpage extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView registerBtn;
    private EditText loginUsername, loginPassword;
    private Button loginBtn;
    private ProgressBar progressBarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.loginpage);

        // Initialize UI elements
        registerBtn = findViewById(R.id.registerButton);
        loginUsername = findViewById(R.id.usernameInput);
        loginPassword = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginButton);
        progressBarLogin = findViewById(R.id.progrssbarlogin);

        if (registerBtn == null || loginUsername == null || loginPassword == null || loginBtn == null || progressBarLogin == null) {
            Log.e(TAG, "One or more views could not be initialized. Check your XML layout.");
            throw new IllegalStateException("Missing views in layout");
        }

        // Check if user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            Intent intent = new Intent(loginpage.this, mainpage.class);
            startActivity(intent);
            finish();
        }

        // Register button click
        registerBtn.setOnClickListener(view -> {
            Intent intent = new Intent(loginpage.this, registrationpage.class);
            startActivity(intent);
        });

        // Login button click
        loginBtn.setOnClickListener(view -> {
            String userUsername = loginUsername.getText().toString().trim();
            String userPassword = loginPassword.getText().toString().trim();

            if (validateInputs(userUsername, userPassword)) {
                checkUser(userUsername, userPassword);
            }
        });
    }

    private boolean validateInputs(String username, String password) {
        if (TextUtils.isEmpty(username)) {
            loginUsername.setError("Username is required");
            loginUsername.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            loginPassword.setError("Password is required");
            loginPassword.requestFocus();
            return false;
        }
        if (password.length() < 8) {
            loginPassword.setError("Password must be at least 8 characters");
            loginPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void checkUser(String username, String password) {
        progressBarLogin.setVisibility(View.VISIBLE);
        loginBtn.setVisibility(View.INVISIBLE);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(username);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBarLogin.setVisibility(View.GONE);
                loginBtn.setVisibility(View.VISIBLE);

                if (snapshot.exists()) {
                    DataSnapshot userSnapshot = snapshot.getChildren().iterator().next();
                    String passwordFromDB = userSnapshot.child("password").getValue(String.class);

                    if (passwordFromDB != null && passwordFromDB.equals(password)) {
                        // Save login status
                        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("username", username);
                        editor.apply();

                        Toast.makeText(loginpage.this, "Welcome " + username, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(loginpage.this, mainpage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        loginPassword.setError("Invalid credentials");
                        loginPassword.requestFocus();
                    }
                } else {
                    loginUsername.setError("User does not exist");
                    loginUsername.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarLogin.setVisibility(View.GONE);
                loginBtn.setVisibility(View.VISIBLE);
                Toast.makeText(loginpage.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }
}
