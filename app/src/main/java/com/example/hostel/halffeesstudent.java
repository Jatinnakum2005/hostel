package com.example.hostel;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> studentList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halffeesstudent);

        halfFeesListView = findViewById(R.id.halfFeesListView);
        studentList = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentList);
        halfFeesListView.setAdapter(listAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        fetchStudentsByFeesStatus("Half Fees Paid");
    }

    private void fetchStudentsByFeesStatus(String status) {
        DatabaseReference studentFeesRef = databaseReference.child("StudentFees");

        studentFeesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();

                for (DataSnapshot prnSnapshot : snapshot.getChildren()) {
                    String feesStatus = prnSnapshot.child("feesStatus").getValue(String.class);

                    if (status.equals(feesStatus)) {
                        String name = prnSnapshot.child("name").getValue(String.class);
                        String room = prnSnapshot.child("room").getValue(String.class);
                        String mobile = prnSnapshot.child("mobile").getValue(String.class);

                        String studentData = "Name: " + (name != null ? name : "N/A") + "\n"
                                + "Room: " + (room != null ? room : "N/A") + "\n"
                                + "Mobile: " + (mobile != null ? mobile : "N/A");

                        studentList.add(studentData);
                    }
                }

                if (studentList.isEmpty()) {
                    studentList.add("No students found with " + status + " fees");
                }

                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(halffeesstudent.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
