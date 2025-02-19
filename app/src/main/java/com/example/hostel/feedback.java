package com.example.hostel;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    private EditText feedbackTitleEditText, feedbackMessageEditText;
    private LinearLayout ratingStarsLayout;
    private Button submitFeedbackButton;
    private ProgressBar progressBar;
    private ImageView[] stars = new ImageView[5];
    private ImageView backButton;
    private int selectedRating = 0; // Initial rating value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Initialize views
        feedbackTitleEditText = findViewById(R.id.FeedbackTitle);
        feedbackMessageEditText = findViewById(R.id.FeedbackMessage);
        submitFeedbackButton = findViewById(R.id.submit_feedback_button);
        progressBar = findViewById(R.id.progressbarfeedback);
        ratingStarsLayout = findViewById(R.id.rating_stars_layout);
        backButton = findViewById(R.id.back_button_feedback);

        // Initialize star views
        stars[0] = findViewById(R.id.star_1);
        stars[1] = findViewById(R.id.star_2);
        stars[2] = findViewById(R.id.star_3);
        stars[3] = findViewById(R.id.star_4);
        stars[4] = findViewById(R.id.star_5);

        // Set click listeners for stars
        for (int i = 0; i < stars.length; i++) {
            int index = i;
            stars[i].setOnClickListener(v -> updateStarRating(index + 1));
        }

        // Submit feedback button
        submitFeedbackButton.setOnClickListener(v -> validateAndSubmitFeedback());

        // Back button click listener
        backButton.setOnClickListener(v -> onBackPressed());
    }


    private void updateStarRating(int rating) {
        selectedRating = rating;
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.filled_backgroundd); // Replace with your filled star icon
            } else {
                stars[i].setImageResource(R.drawable.empty_backgroundd); // Replace with your outline star icon
            }
        }
    }

    private void validateAndSubmitFeedback() {
        String title = feedbackTitleEditText.getText().toString().trim();
        String message = feedbackMessageEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            feedbackTitleEditText.setError("Title is required");
            return;
        }

        if (TextUtils.isEmpty(message)) {
            feedbackMessageEditText.setError("Message is required");
            return;
        }

        if (selectedRating == 0) {
            Toast.makeText(this, "Please provide a rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("username", null);

        if (userName != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userName);
            databaseReference.child("email").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String userEmail = task.getResult().getValue(String.class);
                    if (userEmail != null) {
                        // Disable button and show progress bar here
                        progressBar.setVisibility(View.VISIBLE);
                        submitFeedbackButton.setVisibility(View.GONE);

                        handleFeedback(userName, userEmail, title, message, selectedRating);
                    } else {
                        Toast.makeText(this, "Unable to retrieve user email.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to fetch user email from database.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFeedback(String userName, String userEmail, String title, String message, int rating) {
        // Show progress bar and disable button
        progressBar.setVisibility(View.VISIBLE);
        submitFeedbackButton.setEnabled(false);

        final String senderEmail = "hostelhome665@gmail.com";
        final String appPassword = "zcxk ejns mimn wmaj";

        // Feedback email to admin
        String adminEmailSubject = "New Feedback Received: " + title;
        String adminEmailBody = "User Name: " + userName +
                "\nUser Email: " + userEmail +
                "\n\nFeedback:\n" + message +
                "\n\nRating: " + rating + " stars";

        // Thank-you email to user
        String userEmailSubject = "Thank You for Your Feedback!";
        String userEmailBody = "Dear " + userName + ",\n\nThank you for your valuable feedback.\n\nBest Regards,\nHLH Team";

        // Send emails
        new Thread(() -> {
            sendEmail(senderEmail, appPassword, "hostelhome665@gmail.com", adminEmailSubject, adminEmailBody); // To Admin
            sendEmail(senderEmail, appPassword, userEmail, userEmailSubject, userEmailBody); // To User

            // Hide progress bar and re-enable button after sending emails
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                submitFeedbackButton.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Feedback sent successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Close the feedback activity
            });
        }).start();
    }

    private void sendEmail(String senderEmail, String appPassword, String recipientEmail, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}