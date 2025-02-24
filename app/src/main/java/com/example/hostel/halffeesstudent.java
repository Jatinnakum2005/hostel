package com.example.hostel;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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

public class halffeesstudent extends AppCompatActivity {

    private ListView halfFeesListView;
    private CustomAdapter adapter;
    private ArrayList<String> studentList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halffeesstudent);  // Make sure you create this XML layout

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("StudentFees");

        // Initialize ListView and ArrayList
        halfFeesListView = findViewById(R.id.halfFeesListView);
        studentList = new ArrayList<>();
        adapter = new CustomAdapter(this, studentList);
        halfFeesListView.setAdapter(adapter);

        // Fetch students with "Half Fees Paid" status
        fetchHalfFeesStudents();
    }

    private void fetchHalfFeesStudents() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    String feesStatus = studentSnapshot.child("feesStatus").getValue(String.class);

                    // Check if the fees status is "Half Fees Paid"
                    if ("Half Fees Paid".equals(feesStatus)) {
                        String name = studentSnapshot.child("name").getValue(String.class);
                        String room = studentSnapshot.child("room").getValue(String.class);
                        String mobile = studentSnapshot.child("mobile").getValue(String.class);

                        String studentInfo = "Name: " + name + "\nRoom: " + room + "\nMobile: " + mobile;
                        studentList.add(studentInfo);
                    }
                }
                adapter.notifyDataSetChanged();

                if (studentList.isEmpty()) {
                    Toast.makeText(halffeesstudent.this, "No students found with 'Half Fees Paid'.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(halffeesstudent.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class CustomAdapter extends android.widget.ArrayAdapter<String> {

        public CustomAdapter(Context context, ArrayList<String> studentList) {
            super(context, android.R.layout.simple_list_item_1, studentList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            TextView textView = view.findViewById(android.R.id.text1);
            textView.setTextColor(Color.WHITE); // Set text color to black
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
