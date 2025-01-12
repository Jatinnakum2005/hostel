package com.example.hostel;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class studentfees extends AppCompatActivity {

    private EditText prnEditText;
    private Button searchButton, updateFeesButton, checkFeesButton, clearButton;
    private TextView nameTextView, roomTextView, mobileTextView, feesStatusTextView;
    private Spinner feesSpinner;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studentfees);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize views
        prnEditText = findViewById(R.id.editTextPrn);
        searchButton = findViewById(R.id.buttonSearch);
        updateFeesButton = findViewById(R.id.buttonUpdateFees);
        checkFeesButton = findViewById(R.id.buttonCheckFees);
        clearButton = findViewById(R.id.buttonClear);
        nameTextView = findViewById(R.id.textViewName);
        roomTextView = findViewById(R.id.textViewRoom);
        mobileTextView = findViewById(R.id.textViewMobile);
        feesStatusTextView = findViewById(R.id.textViewFeesStatus);
        feesSpinner = findViewById(R.id.spinnerFees);

        // Set up Spinner with options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.fees_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feesSpinner.setAdapter(adapter);

        // Set click listeners
        searchButton.setOnClickListener(v -> fetchStudentDetails());
        updateFeesButton.setOnClickListener(v -> saveDataToStudentFeesNode());
        checkFeesButton.setOnClickListener(v -> fetchFeesStatus());
        clearButton.setOnClickListener(v -> clearFields());
    }

    private void fetchStudentDetails() {
        String prn = prnEditText.getText().toString().trim();

        if (TextUtils.isEmpty(prn)) {
            Toast.makeText(this, "Please enter a PRN", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate the database based on the correct structure
        DatabaseReference studentRef = databaseReference.child("Jatin").child("LivingStudent").child(prn);

        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve data from the snapshot
                    String name = snapshot.child("name").getValue(String.class);
                    String room = snapshot.child("room").getValue(String.class);
                    String mobile = snapshot.child("mobile").getValue(String.class);

                    // Handle cases where fields might be null
                    nameTextView.setText("Name: " + (name != null ? name : "Not available"));
                    roomTextView.setText("Room: " + (room != null ? room : "Not available"));
                    mobileTextView.setText("Mobile: " + (mobile != null ? mobile : "Not available"));

                    Toast.makeText(studentfees.this, "Student details fetched successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(studentfees.this, "No data found for this PRN", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(studentfees.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDataToStudentFeesNode() {
        String prn = prnEditText.getText().toString().trim();
        String selectedFeesOption = feesSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(prn)) {
            Toast.makeText(this, "Please enter a PRN", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare data to be stored
        String name = nameTextView.getText().toString().replace("Name: ", "");
        String room = roomTextView.getText().toString().replace("Room: ", "");
        String mobile = mobileTextView.getText().toString().replace("Mobile: ", "");

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(room) || TextUtils.isEmpty(mobile)) {
            Toast.makeText(this, "Please fetch student details before updating fees status", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check or create the StudentFees node under the parent Username
        DatabaseReference studentFeesRef = databaseReference.child("Jatin").child("StudentFees").child(prn);

        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", name);
        dataMap.put("room", room);
        dataMap.put("mobile", mobile);
        dataMap.put("feesStatus", selectedFeesOption);

        // Store data in the node
        studentFeesRef.updateChildren(dataMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(studentfees.this, "Data stored/updated in StudentFees successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(studentfees.this, "Failed to update StudentFees node", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFeesStatus() {
        String prn = prnEditText.getText().toString().trim();

        if (TextUtils.isEmpty(prn)) {
            Toast.makeText(this, "Please enter a PRN", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to the StudentFees node
        DatabaseReference feesRef = databaseReference.child("Jatin").child("StudentFees").child(prn);

        feesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve the feesStatus
                    String feesStatus = snapshot.child("feesStatus").getValue(String.class);
                    feesStatusTextView.setText("Fees Status: " + (feesStatus != null ? feesStatus : "Not available"));
                    Toast.makeText(studentfees.this, "Fees status fetched successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(studentfees.this, "No fees status found for this PRN", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(studentfees.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        prnEditText.setText("");
        nameTextView.setText("Name: ");
        roomTextView.setText("Room: ");
        mobileTextView.setText("Mobile: ");
        feesStatusTextView.setText("Fees Status: ");
        feesSpinner.setSelection(0); // Reset the spinner to the first item
        Toast.makeText(this, "Fields cleared", Toast.LENGTH_SHORT).show();
    }
}
