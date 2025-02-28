package com.example.hostel;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private Button halfFeesButton, fullFeesButton;
    private TextView nameTextView, roomTextView, mobileTextView, feesStatusTextView, hostelNameTextView;
    private Spinner feesSpinner;

    private DatabaseReference databaseReference;

    private ListView studentListView;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> studentList;

    private String hostelName = ""; // Variable to store hostel name

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
        hostelNameTextView = findViewById(R.id.textViewHostelName);
        feesSpinner = findViewById(R.id.spinnerFees);

        // Initialize ListView and adapter
        studentListView = findViewById(R.id.studentListView);
        studentList = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentList);
        studentListView.setAdapter(listAdapter);

        // Set up Spinner with custom styling
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.fees_options)) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.BLACK);
                text.setTypeface(null, Typeface.BOLD);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                // Create a LinearLayout to hold the text view and divider
                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                // Create and style the text view
                TextView textView = new TextView(getContext());
                textView.setId(android.R.id.text1);
                textView.setText(getItem(position));
                textView.setTextColor(Color.BLACK);
                textView.setTypeface(null, Typeface.BOLD);

                // Increased padding (Left, Top, Right, Bottom)
                textView.setPadding(30, 30, 30, 30);

                textView.setTextSize(16);

                // Add the text view to the layout
                layout.addView(textView);

                // Add a divider except for the last item
                if (position < getCount() - 1) {
                    View divider = new View(getContext());
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            3 // Increased height of the divider
                    );
                    dividerParams.setMargins(0, 5, 0, 5); // Add margin above and below the divider
                    divider.setLayoutParams(dividerParams);
                    divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
                    layout.addView(divider);
                }

                return layout;
            }
        };
        feesSpinner.setAdapter(adapter);

        // Set click listeners
        searchButton.setOnClickListener(v -> fetchStudentDetails());
        updateFeesButton.setOnClickListener(v -> saveDataToStudentFeesNode());
        checkFeesButton.setOnClickListener(v -> fetchFeesStatus());
        clearButton.setOnClickListener(v -> clearFields());

        // Navigate to Half Fees Activity
        halfFeesButton.setOnClickListener(v -> {
            Intent intent = new Intent(studentfees.this, halffeesstudent.class);
            intent.putExtra("username", hostelName); // Pass the hostel name
            startActivity(intent);
        });

        // Navigate to Full Fees Activity
        fullFeesButton.setOnClickListener(v -> {
            Intent intent = new Intent(studentfees.this, fullfeesstudent.class);
            intent.putExtra("username", hostelName); // Pass the hostel name
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
                    DataSnapshot livingStudentSnapshot = userSnapshot.child("HostelStudent").child(prn);

                    if (livingStudentSnapshot.exists()) {
                        hostelName = userSnapshot.getKey(); // Get Hostel Name
                        String name = livingStudentSnapshot.child("name").getValue(String.class);
                        String room = livingStudentSnapshot.child("room").getValue(String.class);
                        String mobile = livingStudentSnapshot.child("mobile").getValue(String.class);

                        hostelNameTextView.setText("Hostel Name: " + (hostelName != null ? hostelName : "Not available"));
                        nameTextView.setText("Name: " + (name != null ? name : "Not available"));
                        roomTextView.setText("Room: " + (room != null ? room : "Not available"));
                        mobileTextView.setText("Mobile: " + (mobile != null ? mobile : "Not available"));

                        // Clear previous fees status when a new student is searched
                        feesStatusTextView.setText("Fees Status: ");

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

    private void fetchFeesStatus() {
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
                    String feesStatus = snapshot.child("feesStatus").getValue(String.class);
                    feesStatusTextView.setText("Fees Status: " + (feesStatus != null ? feesStatus : "Not available"));
                } else {
                    Toast.makeText(studentfees.this, "No fees data found for this PRN", Toast.LENGTH_SHORT).show();
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
        dataMap.put("mobile", mobileTextView.getText().toString().replace("Mobile: ", ""));
        dataMap.put("feesStatus", selectedFeesOption);
        dataMap.put("hostelName", hostelName); // Add hostel name

        studentFeesRef.updateChildren(dataMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(studentfees.this, "Data stored/updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(studentfees.this, "Failed to update data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        prnEditText.setText("");
        nameTextView.setText("Name: ");
        roomTextView.setText("Room: ");
        mobileTextView.setText("Mobile: ");
        feesStatusTextView.setText("Fees Status: ");
        hostelNameTextView.setText("Hostel Name: ");
        feesSpinner.setSelection(0);
        Toast.makeText(this, "Fields cleared", Toast.LENGTH_SHORT).show();
    }
}
