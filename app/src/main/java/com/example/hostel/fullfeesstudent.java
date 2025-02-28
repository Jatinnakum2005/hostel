package com.example.hostel;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
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

public class fullfeesstudent extends AppCompatActivity {

    private ListView fullFeesListView;
    private CustomAdapter adapter;
    private ArrayList<String> studentList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullfeesstudent);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("StudentFees");

        // Initialize ListView and ArrayList
        fullFeesListView = findViewById(R.id.fullfeesListView);
        studentList = new ArrayList<>();
        adapter = new CustomAdapter(this, studentList);
        fullFeesListView.setAdapter(adapter);

        // Fetch students with "Full Fees Paid" status
        fetchFullFeesStudents();
    }

    private void fetchFullFeesStudents() {
        String loggedInUsername = getLoggedInUsername();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    String feesStatus = studentSnapshot.child("feesStatus").getValue(String.class);
                    String hostelName = studentSnapshot.child("hostelName").getValue(String.class);

                    // Check both conditions: fees status is "Full Fees Paid" AND hostelName matches
                    if ("Full Fees Paid".equals(feesStatus) &&
                            loggedInUsername != null &&
                            loggedInUsername.equals(hostelName)) {

                        String name = studentSnapshot.child("name").getValue(String.class);
                        String room = studentSnapshot.child("room").getValue(String.class);
                        String mobile = studentSnapshot.child("mobile").getValue(String.class);

                        String studentInfo = "Name: " + name + "\nRoom: " + room + "\nMobile: " + mobile;
                        studentList.add(studentInfo);
                    }
                }
                adapter.notifyDataSetChanged();

                if (studentList.isEmpty()) {
                    Toast.makeText(fullfeesstudent.this,
                            "No students found. " + loggedInUsername,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(fullfeesstudent.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getLoggedInUsername() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        return prefs.getString("username", ""); // "" is the default if not found
    }



    private static class CustomAdapter extends android.widget.ArrayAdapter<String> {

        public CustomAdapter(Context context, ArrayList<String> studentList) {
            super(context, android.R.layout.simple_list_item_1, studentList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            TextView textView = view.findViewById(android.R.id.text1);
            textView.setTextColor(Color.BLACK); // Set text color to black
            textView.setPadding(32, 32, 32, 32); // Add padding for better spacing

            // Add horizontal line
            View separator = new View(getContext());
            separator.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    2
            ));
            separator.setBackgroundColor(android.graphics.Color.GRAY);

            // Create a container for text and separator
            LinearLayout container = new LinearLayout(getContext());
            container.setOrientation(LinearLayout.VERTICAL);
            container.addView(textView);
            container.addView(separator);

            return container;
        }
    }
}