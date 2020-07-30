package com.example.womensafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddNumber extends AppCompatActivity {
    EditText number1,number2,number3;
    Button submit;
    FirebaseFirestore fstore;
    String userId;
    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_number);

        number1 = findViewById(R.id.number1);
        number2 = findViewById(R.id.number2);
        number3 = findViewById(R.id.number3);
        submit = findViewById(R.id.addNumber);
        fstore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String n1 = number1.getText().toString();
                String n2 = number2.getText().toString();
                String n3 = number3.getText().toString();
                if(n1.length()!= 10)
                {
                    number1.setError("Enter valid Number");
                    return;
                }
//                if(n2.length()!= 10)
//                {
//                    number2.setError("Enter valid Number");
//                    return;
//                }
//                if(n3.length()!= 10)
//                {
//                    number3.setError("Enter valid Number");
//                    return;
//                }
                userId =  fAuth.getCurrentUser().getUid();

                DocumentReference documentReference = fstore.collection("user").document(userId);
                Map<String,Object> user =  new HashMap<>();
                user.put("number1" , n1);
                user.put("number2" , n2);
                user.put("number3" , n3);

                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddNumber.this,"Numbers are Stored" , Toast.LENGTH_SHORT).show();
                    }
                });
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });
    }
}