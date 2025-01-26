package com.example.hostel;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.HashMap;

public class studentfees extends AppCompatActivity {

    private EditText prnEditText;
    private Button searchButton, updateFeesButton, checkFeesButton, clearButton;
    private Button halfFeesButton, fullFeesButton; // Buttons for fetching students based on fees status
    private TextView nameTextView, roomTextView, mobileTextView, feesStatusTextView;
    private Spinner feesSpinner;

    private DatabaseReference databaseReference;

    private ListView studentListView;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> studentList;

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
        feesStatusTextView = findViewById(R.id.textViewFeesStatus);
        feesSpinner = findViewById(R.id.spinnerFees);

        // Initialize ListView and adapter
        studentListView = findViewById(R.id.studentListView);
        studentList = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentList);
        studentListView.setAdapter(listAdapter);

        // Set up Spinner with custom item layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.fees_options));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feesSpinner.setAdapter(adapter);

        // Set click listeners
        searchButton.setOnClickListener(v -> fetchStudentDetails());
        updateFeesButton.setOnClickListener(v -> saveDataToStudentFeesNode());
        checkFeesButton.setOnClickListener(v -> fetchFeesStatus());
        clearButton.setOnClickListener(v -> clearFields());

        // Navigate to Half Fees Activity
        halfFeesButton.setOnClickListener(v -> {
            Intent intent = new Intent(studentfees.this, halffeesstudent.class);
            startActivity(intent);
        });

        // Navigate to Full Fees Activity
        fullFeesButton.setOnClickListener(v -> {
            Intent intent = new Intent(studentfees.this, fullfeesstudent.class);
            startActivity(intent);
        });
    }

    private void fetchStudentDetails() {
        String prn = prnEditText.getText().toString().trim();

        if (TextUtils.isEmpty(prn)) {
            Toast.makeText(this, "Please enter a PRN", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean studentFound = false;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    DataSnapshot livingStudentSnapshot = userSnapshot.child("LivingStudent").child(prn);

                    if (livingStudentSnapshot.exists()) {
                        String name = livingStudentSnapshot.child("name").getValue(String.class);
                        String room = livingStudentSnapshot.child("room").getValue(String.class);
                        String mobile = livingStudentSnapshot.child("mobile").getValue(String.class);

                        nameTextView.setText("Name: " + (name != null ? name : "Not available"));
                        roomTextView.setText("Room: " + (room != null ? room : "Not available"));
                        mobileTextView.setText("Mobile: " + (mobile != null ? mobile : "Not available"));

                        studentFound = true;
                        break;
                    }
                }

                if (!studentFound) {
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

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean prnFound = false;

                for (DataSnapshot hostelSnapshot : snapshot.getChildren()) {
                    DataSnapshot livingStudentSnapshot = hostelSnapshot.child("LivingStudent").child(prn);

                    if (livingStudentSnapshot.exists()) {
                        String hostelName = hostelSnapshot.getKey();
                        String name = nameTextView.getText().toString().replace("Name: ", "");
                        String room = roomTextView.getText().toString().replace("Room: ", "");
                        String mobile = mobileTextView.getText().toString().replace("Mobile: ", "");

                        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(room) || TextUtils.isEmpty(mobile)) {
                            Toast.makeText(studentfees.this, "Please fetch student details before updating fees status", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DatabaseReference studentFeesRef = databaseReference.child("StudentFees").child(prn);
                        HashMap<String, Object> dataMap = new HashMap<>();
                        dataMap.put("name", name);
                        dataMap.put("room", room);
                        dataMap.put("mobile", mobile);
                        dataMap.put("feesStatus", selectedFeesOption);
                        dataMap.put("hostel", hostelName);

                        studentFeesRef.updateChildren(dataMap).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(studentfees.this, "Data stored/updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(studentfees.this, "Failed to update data", Toast.LENGTH_SHORT).show();
                            }
                        });

                        prnFound = true;
                        break;
                    }
                }

                if (!prnFound) {
                    Toast.makeText(studentfees.this, "PRN not found in any hostel", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(studentfees.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                    String feesStatus = snapshot.child("feesStatus").getValue(String.class);

                    nameTextView.setText("Name: " + (name != null ? name : "Not available"));
                    roomTextView.setText("Room: " + (room != null ? room : "Not available"));
                    mobileTextView.setText("Mobile: " + (mobile != null ? mobile : "Not available"));
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
        feesSpinner.setSelection(0);
        Toast.makeText(this, "Fields cleared", Toast.LENGTH_SHORT).show();
    }
}
