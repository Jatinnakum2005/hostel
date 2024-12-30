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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;

import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    TextView registerbtn,forpass;
    EditText loginusername, loginpass;
    Button loginbtn;
    String userUsername, userPassword;
    ProgressBar progrssbarlogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Check login state
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            Intent intent = new Intent(MainActivity.this, mainpage.class);
            startActivity(intent);
            finish(); // Close this activity
        } else {
            setContentView(R.layout.activity_mainpage);
        }

        registerbtn = findViewById(R.id.registerbtn);
        loginusername = findViewById(R.id.loginusername);
        loginpass = findViewById(R.id.loginpass);
        loginbtn = findViewById(R.id.loginbtn);
        progrssbarlogin=findViewById(R.id.progrssbarlogin);
        forpass=findViewById(R.id.forpass);

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this , registrationpage.class);
                startActivity(intent);
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Get Input
                userUsername = loginusername.getText().toString().trim();
                userPassword = loginpass.getText().toString().trim();
                if (!validateUsername() | !validatePassword()) {

                } else {
                    checkUser();
                }
            }
        });

        forpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,forogotpassword.class);
                startActivity(intent);
            }
        });

    }

    //Validate UserName
    public Boolean validateUsername() {
        String val = loginusername.getText().toString();
        if (TextUtils.isEmpty(val)) {
            loginusername.setError("Username is required");
            loginusername.requestFocus();
            return false;
        } else if (val.contains(" ")) {
            loginusername.setError("Username cannot contain spaces");
            loginusername.requestFocus();
            return false;
        } else {
            loginusername.setError(null);
            return true;
        }
    }

    //Validate Password
    public Boolean validatePassword() {
        String val = loginpass.getText().toString();
        if (TextUtils.isEmpty(val) || val.length() < 8) {
            loginpass.setError("Password must be at least 8 characters");
            loginpass.requestFocus();
            return false;
        } else {
            loginpass.setError(null);
            return true;
        }
    }

    //Check User
    public void checkUser() {
        progrssbarlogin.setVisibility(View.VISIBLE);
        loginbtn.setVisibility(View.INVISIBLE);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query checkUserDatabase = reference.orderByChild("userName").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    loginusername.setError(null);
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);
                    if (passwordFromDB.equals(userPassword)) {
                        loginusername.setError(null);
                        // Save login state
                        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("username", userUsername); // Optional, for later use
                        editor.apply();

                        Toast.makeText(MainActivity.this, "Welcome " + userUsername, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, mainpage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                        loginpass.requestFocus();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "User Does Not Exist", Toast.LENGTH_SHORT).show();
                    loginusername.requestFocus();
                }
                progrssbarlogin.setVisibility(View.INVISIBLE);
                loginbtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });



    }


}