package com.example.hostel;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class feedback extends AppCompatActivity {

    private EditText nameEditText, emailEditText, feedbackEditText;
    private Button submitButton;

    // Admin email credentials
    private static final String ADMIN_EMAIL = "hostelhome665@gmail.com";  // Replace with admin's email
    private static final String ADMIN_PASSWORD = "Hostel@1001"; // Replace with admin's app password

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Initialize views
        nameEditText = findViewById(R.id.editTextName);
        emailEditText = findViewById(R.id.editTextEmail);
        feedbackEditText = findViewById(R.id.editTextFeedback);
        submitButton = findViewById(R.id.buttonSubmitFeedback);

        // Set click listener for the submit button
        submitButton.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String feedback = feedbackEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(feedback)) {
            Toast.makeText(this, "Please provide your feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send feedback email to admin
        sendFeedbackEmail(name, email, feedback);

        // Clear fields
        clearFields();
        Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
    }

    private void sendFeedbackEmail(String name, String userEmail, String feedback) {
        // Email content
        String subject = "New Feedback from " + name;
        String message = "Name: " + name + "\nEmail: " + userEmail + "\n\nFeedback:\n" + feedback;

        // Set up SMTP properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");                     // Enables SMTP authentication
        properties.put("mail.smtp.starttls.enable", "true");          // Enables STARTTLS
        properties.put("mail.smtp.host", "smtp.gmail.com");           // SMTP Host for Gmail
        properties.put("mail.smtp.port", "587");                      // Port for Gmail SMTP (STARTTLS)

        // Create a mail session with the SMTP properties
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(ADMIN_EMAIL, ADMIN_PASSWORD);
            }
        });

        try {
            // Create email message
            Message emailMessage = new MimeMessage(session);
            emailMessage.setFrom(new InternetAddress(ADMIN_EMAIL));       // Sender email
            emailMessage.setRecipients(Message.RecipientType.TO,          // Recipient(s)
                    InternetAddress.parse(ADMIN_EMAIL));                  // Admin email
            emailMessage.setSubject(subject);                             // Email subject
            emailMessage.setText(message);                                // Email body

            // Send the email on a background thread
            new Thread(() -> {
                try {
                    Transport.send(emailMessage);
                    // Notify user on successful email submission
                    runOnUiThread(() -> Toast.makeText(this, "Feedback sent successfully!", Toast.LENGTH_SHORT).show());
                } catch (MessagingException e) {
                    e.printStackTrace();
                    // Notify user on email send failure
                    runOnUiThread(() -> Toast.makeText(this, "Failed to send feedback. Please try again.", Toast.LENGTH_SHORT).show());
                }
            }).start();

        } catch (MessagingException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to prepare email. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }


    private void clearFields() {
        nameEditText.setText("");
        emailEditText.setText("");
        feedbackEditText.setText("");
    }
}
