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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class studentfees extends AppCompatActivity {

    private EditText prnEditText;
    private Button searchButton, updateFeesButton, checkFeesButton, clearButton, halfFeesButton, fullFeesButton;
    private TextView nameTextView, roomTextView, mobileTextView, hostelTextView, feesStatusTextView;
    private Spinner feesSpinner;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studentfees);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        prnEditText = findViewById(R.id.editTextPrn);
        searchButton = findViewById(R.id.buttonSearch);
        updateFeesButton = findViewById(R.id.buttonUpdateFees);
        checkFeesButton = findViewById(R.id.buttonCheckFees);
        clearButton = findViewById(R.id.buttonClear);
        halfFeesButton = findViewById(R.id.buttonHalfFees);
        fullFeesButton = findViewById(R.id.buttonFullFees);
        nameTextView = findViewById(R.id.textViewName);
        roomTextView = findViewById(R.id.textViewRoom);
        mobileTextView = findViewById(R.id.textViewMobile);
        hostelTextView = findViewById(R.id.hostelName);
        feesStatusTextView = findViewById(R.id.textViewFeesStatus);
        feesSpinner = findViewById(R.id.spinnerFees);

        // Set up Spinner with custom item layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                getResources().getStringArray(R.array.fees_options)
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feesSpinner.setAdapter(adapter);

        // Set click listeners
        searchButton.setOnClickListener(v -> fetchStudentDetails());
        updateFeesButton.setOnClickListener(v -> saveDataToStudentFeesNode());
        checkFeesButton.setOnClickListener(v -> fetchFeesStatus());
        clearButton.setOnClickListener(v -> clearFields());
        halfFeesButton.setOnClickListener(v -> fetchStudentsByFeesStatus("Half Fees Paid"));
        fullFeesButton.setOnClickListener(v -> fetchStudentsByFeesStatus("Full Fees Paid"));
    }

    private void fetchStudentDetails() {
        String prn = prnEditText.getText().toString().trim();

        if (TextUtils.isEmpty(prn)) {
            Toast.makeText(this, "Please enter a PRN", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference studentFeesRef = databaseReference.child("StudentFees").child(prn);

        studentFeesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String room = snapshot.child("room").getValue(String.class);
                    String mobile = snapshot.child("mobile").getValue(String.class);
                    String hostel = snapshot.child("hostel").getValue(String.class);

                    nameTextView.setText("Name: " + (name != null ? name : "Not available"));
                    roomTextView.setText("Room: " + (room != null ? room : "Not available"));
                    mobileTextView.setText("Mobile: " + (mobile != null ? mobile : "Not available"));
                    hostelTextView.setText("Hostel: " + (hostel != null ? hostel : "Not available"));

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

        DatabaseReference studentFeesRef = databaseReference.child("StudentFees").child(prn);

        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", nameTextView.getText().toString().replace("Name: ", ""));
        dataMap.put("room", roomTextView.getText().toString().replace("Room: ", ""));
        dataMap.put("hostel", hostelTextView.getText().toString().replace("Hostel: ", ""));
        dataMap.put("mobile", mobileTextView.getText().toString().replace("Mobile: ", ""));
        dataMap.put("feesStatus", selectedFeesOption);

        studentFeesRef.updateChildren(dataMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(studentfees.this, "Data updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(studentfees.this, "Failed to update data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFeesStatus() {
        String prn = prnEditText.getText().toString().trim();

        if (TextUtils.isEmpty(prn)) {
            Toast.makeText(this, "Please enter a PRN", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference feesRef = databaseReference.child("StudentFees").child(prn);

        feesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String room = snapshot.child("room").getValue(String.class);
                    String mobile = snapshot.child("mobile").getValue(String.class);
                    String hostel = snapshot.child("hostel").getValue(String.class);
                    String feesStatus = snapshot.child("feesStatus").getValue(String.class);

                    nameTextView.setText("Name: " + (name != null ? name : "Not available"));
                    roomTextView.setText("Room: " + (room != null ? room : "Not available"));
                    mobileTextView.setText("Mobile: " + (mobile != null ? mobile : "Not available"));
                    hostelTextView.setText("Hostel: " + (hostel != null ? hostel : "Not available"));
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

    private void fetchStudentsByFeesStatus(String feesStatus) {
        DatabaseReference studentFeesRef = databaseReference.child("StudentFees");

        studentFeesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder result = new StringBuilder();

                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    String studentFeesStatus = studentSnapshot.child("feesStatus").getValue(String.class);

                    if (feesStatus.equals(studentFeesStatus)) {
                        String name = studentSnapshot.child("name").getValue(String.class);
                        String room = studentSnapshot.child("room").getValue(String.class);
                        String mobile = studentSnapshot.child("mobile").getValue(String.class);
                        String hostel = studentSnapshot.child("hostel").getValue(String.class);

                        result.append("Name: ").append(name != null ? name : "N/A").append("\n")
                                .append("Room: ").append(room != null ? room : "N/A").append("\n")
                                .append("Mobile: ").append(mobile != null ? mobile : "N/A").append("\n")
                                .append("Hostel: ").append(hostel != null ? hostel : "N/A").append("\n\n");
                    }
                }

                if (result.length() == 0) {
                    result.append("No students found with ").append(feesStatus);
                }

                showResultDialog(result.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(studentfees.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResultDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Students List")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void clearFields() {
        prnEditText.setText("");
        nameTextView.setText("Name: ");
        roomTextView.setText("Room: ");
        mobileTextView.setText("Mobile: ");
        hostelTextView.setText("Hostel: ");
        feesStatusTextView.setText("Fees Status: ");
        feesSpinner.setSelection(0);
        Toast.makeText(this, "Fields cleared", Toast.LENGTH_SHORT).show();
    }
}
