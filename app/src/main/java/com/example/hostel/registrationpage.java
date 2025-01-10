package com.example.hostel;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class registrationpage extends AppCompatActivity {

    EditText username, email, phone, password, conformpassword;
    Button register;
    ProgressBar progrssbarsubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registrationpage);

        // Initialize views
        username = findViewById(R.id.usernameInput);
        email = findViewById(R.id.emailInput);
        phone = findViewById(R.id.phoneInput);
        password = findViewById(R.id.passwordInput);
        conformpassword = findViewById(R.id.confirmPasswordInput);
        register = findViewById(R.id.submitButton);
        progrssbarsubmit = findViewById(R.id.progressBarSubmit);

        register.setOnClickListener(v -> {
            String User_name = username.getText().toString().trim();
            String E_mail = email.getText().toString().trim();
            String Phone_no = phone.getText().toString().trim();
            String Pass = password.getText().toString().trim();
            String Confirm_pass = conformpassword.getText().toString().trim();

            // Input validation
            String mobileRegex = "[6-9][0-9]{9}";
            Pattern mobilePattern = Pattern.compile(mobileRegex);
            Matcher mobileMatcher = mobilePattern.matcher(Phone_no);

            if (TextUtils.isEmpty(User_name)) {
                username.setError("Username is required");
                username.requestFocus();
            } else if (User_name.contains(" ")) {
                username.setError("Username cannot contain spaces");
                username.requestFocus();
            } else if (TextUtils.isEmpty(E_mail) || !Patterns.EMAIL_ADDRESS.matcher(E_mail).matches()) {
                email.setError("Valid Email is required");
                email.requestFocus();
            } else if (Phone_no.length() != 10 || !mobileMatcher.find()) {
                phone.setError("Valid Mobile number is required");
                phone.requestFocus();
            } else if (TextUtils.isEmpty(Pass) || Pass.length() < 8) {
                password.setError("Password must be at least 8 characters");
                password.requestFocus();
            } else if (TextUtils.isEmpty(Confirm_pass) || !Pass.equals(Confirm_pass)) {
                conformpassword.setError("Passwords do not match");
                conformpassword.requestFocus();
            } else {
                // Store user data in Firebase
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                progrssbarsubmit.setVisibility(View.VISIBLE);
                register.setVisibility(View.INVISIBLE);

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            progrssbarsubmit.setVisibility(View.GONE);

                            boolean usernameExists = snapshot.child(User_name).exists();
                            boolean phoneExists = false;

                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String existingPhone = userSnapshot.child("phone").getValue(String.class);
                                if (existingPhone != null && Phone_no.equals(existingPhone)) {
                                    phoneExists = true;
                                    break;
                                }
                            }

                            if (usernameExists) {
                                username.setError("User Name Already Exists");
                                username.requestFocus();
                                progrssbarsubmit.setVisibility(View.INVISIBLE);
                                register.setVisibility(View.VISIBLE);
                            } else if (phoneExists) {
                                phone.setError("Mobile Number Already Exists");
                                phone.requestFocus();
                                progrssbarsubmit.setVisibility(View.INVISIBLE);
                                register.setVisibility(View.VISIBLE);
                            } else {
                                // Save user details to Firebase
                                User newUser = new User(User_name, E_mail, Phone_no, Pass);
                                databaseReference.child(User_name).setValue(newUser)
                                        .addOnCompleteListener(task -> {
                                            progrssbarsubmit.setVisibility(View.GONE);
                                            register.setVisibility(View.VISIBLE);
                                            if (task.isSuccessful()) {
                                                Toast.makeText(registrationpage.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(registrationpage.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(registrationpage.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progrssbarsubmit.setVisibility(View.GONE);
                        Toast.makeText(registrationpage.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // Define User class to represent user data
    public static class User {
        public String username, email, phone, password;

        public User(String username, String email, String phone, String password) {
            this.username = username;
            this.email = email;
            this.phone = phone;
            this.password = password;
        }
    }
}
