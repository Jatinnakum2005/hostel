package com.example.hostel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class mainpage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a LinearLayout to hold the buttons
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add buttons for each activity
        addButton(layout, "Manage Room", ManageRoom.class);

        // Set the layout as the content view
        setContentView(layout);
    }

    // Helper method to create and add buttons
    private void addButton(LinearLayout layout, String text, final Class<?> activityToOpen) {
        Button button = new Button(this);
        button.setText(text);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Correct the Intent creation
                Intent intent = new Intent(mainpage.this, activityToOpen);
                startActivity(intent);
            }
        });

        // Add the button to the layout
        layout.addView(button);
    }
}
