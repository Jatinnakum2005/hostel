package com.example.hostel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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

public class MainActivity extends AppCompatActivity {

    public TextView registerBtn;
    public EditText loginUsername, loginPassword;
    public Button loginBtn;
    public ProgressBar progressBarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the correct layout
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        registerBtn = findViewById(R.id.registerbtn);
        loginUsername = findViewById(R.id.loginusername);
        loginPassword = findViewById(R.id.loginpass);
        loginBtn = findViewById(R.id.loginbtn);
        progressBarLogin = findViewById(R.id.progrssbarlogin);

        // Check if the views are initialized
        if (registerBtn == null || loginUsername == null || loginPassword == null) {
            throw new IllegalStateException("One or more views not found in the layout");
        }

        // Set click listeners
        registerBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, registrationpage.class);
            startActivity(intent);
        });

        loginBtn.setOnClickListener(view -> {
            String userUsername = loginUsername.getText().toString().trim();
            String userPassword = loginPassword.getText().toString().trim();

            if (validateInputs(userUsername, userPassword)) {
                checkUser(userUsername, userPassword);
            }
        });
    }

    // Validate inputs
    private boolean validateInputs(String username, String password) {
        if (TextUtils.isEmpty(username)) {
            loginUsername.setError("Username is required");
            loginUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 8) {
            loginPassword.setError("Password must be at least 8 characters");
            loginPassword.requestFocus();
            return false;
        }
        return true;
    }

    // Check User in Firebase
    private void checkUser(String username, String password) {
        progressBarLogin.setVisibility(View.VISIBLE);
        loginBtn.setVisibility(View.INVISIBLE);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(username); // Ensure field matches Firebase key

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBarLogin.setVisibility(View.GONE);
                loginBtn.setVisibility(View.VISIBLE);

                if (snapshot.exists()) {
                    DataSnapshot userSnapshot = snapshot.getChildren().iterator().next();
                    String passwordFromDB = userSnapshot.child("password").getValue(String.class);

                    if (passwordFromDB != null && passwordFromDB.equals(password)) {
                        // Save login state
                        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("username", username);
                        editor.apply();

                        Toast.makeText(MainActivity.this, "Welcome " + username, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, mainpage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        loginPassword.setError("Invalid Credentials");
                        loginPassword.requestFocus();
                    }
                } else {
                    loginUsername.setError("User Does Not Exist");
                    loginUsername.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarLogin.setVisibility(View.GONE);
                loginBtn.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
