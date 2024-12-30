package com.example.hostel;

import android.content.Intent;
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

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class registrationpage extends AppCompatActivity {

    EditText username, email, phone, password, conformpassword;
    Button register;
    ProgressBar progrssbarsubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG); // Enable Firebase debugging

        setContentView(R.layout.activity_registrationpage);

        // Initialize views
        username = findViewById(R.id.usernameInput);
        email = findViewById(R.id.emailInput);
        phone = findViewById(R.id.phoneInput);
        password = findViewById(R.id.registration_pass);
        conformpassword = findViewById(R.id.registration_confirm_pass);
        register = findViewById(R.id.submitButton);
        progrssbarsubmit = findViewById(R.id.progrssbarsubmit);

        // Set a global exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();
            Toast.makeText(this, "An unexpected error occurred.", Toast.LENGTH_LONG).show();
        });

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
                // Check if username and phone exist in Firebase
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
                                sendOtpWithRetry(User_name, E_mail, Phone_no, Pass, 3); // Retry OTP sending up to 3 times
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

    private void sendOtpWithRetry(String User_name, String E_mail, String Phone_no, String Confirm_pass, int retries) {
        if (retries <= 0) {
            Toast.makeText(this, "Failed to send OTP after multiple attempts.", Toast.LENGTH_LONG).show();
            progrssbarsubmit.setVisibility(View.GONE);
            register.setVisibility(View.VISIBLE);
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + Phone_no,
                60,
                TimeUnit.SECONDS,
                registrationpage.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        progrssbarsubmit.setVisibility(View.GONE);
                        register.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        e.printStackTrace();
                        sendOtpWithRetry(User_name, E_mail, Phone_no, Confirm_pass, retries - 1); // Retry
                    }

                    @Override
                    public void onCodeSent(@NonNull String backendotp, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        progrssbarsubmit.setVisibility(View.GONE);
                        register.setVisibility(View.VISIBLE);
                        Toast.makeText(registrationpage.this, "OTP Sent Successfully", Toast.LENGTH_SHORT).show();

                        // Navigate to OTP verification page
                        Intent intent = new Intent(registrationpage.this, otppage.class);
                        intent.putExtra("otp", backendotp);
                        intent.putExtra("mobile", Phone_no);
                        intent.putExtra("username", User_name);
                        intent.putExtra("email", E_mail);
                        intent.putExtra("password", Confirm_pass);
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }
}
