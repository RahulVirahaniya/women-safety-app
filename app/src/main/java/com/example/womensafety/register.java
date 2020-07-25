package com.example.womensafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class register extends AppCompatActivity {
    EditText mName,mEmail,mPassword,mPhone;
    Button mRegister;
    Button mlogin;
    FirebaseAuth fAuth;
    ProgressBar pBar;
    String verificationId;
    FirebaseFirestore fstore;
    String userId;

    PhoneAuthProvider.ForceResendingToken Token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPhone = findViewById(R.id.phone);
        mRegister = findViewById(R.id.register1);
        mlogin = findViewById(R.id.login1);
        fstore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        pBar  = findViewById(R.id.progressBar);

        if(fAuth.getCurrentUser()!= null)
        {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                final String password  = mPassword.getText().toString().trim();
                final String phoneno = mPhone.getText().toString();
                final String name = mName.getText().toString();

                if(TextUtils.isEmpty(email))
                {
                    mEmail.setError("Please Enter Email");
                    return;
                }
                if(TextUtils.isEmpty(password))
                {
                    mPassword.setError("Please Enter Password");
                    return;
                }
                if(TextUtils.isEmpty(phoneno))
                {
                    mPassword.setError("Please Enter PhoneNo");
                    return;
                }
                if(password.length()<6)
                {
                    mPassword.setError("Enter Password > 6");
                    return;
                }
                if(phoneno.length()!=10) {
                    mPhone.setError("Enter Valid Phone Number");
                    return;
                }
                pBar.setVisibility(View.VISIBLE);

                /////////// Online Registration///////////////////
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(register.this,"user created",Toast.LENGTH_SHORT).show();
                        if(task.isSuccessful())
                        {
                            Toast.makeText(register.this,"user created",Toast.LENGTH_SHORT).show();
                            userId = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fstore.collection("users").document(userId);
                            Map<String,Object>  user =  new HashMap<>();
                            user.put("fullName" , name);
                            user.put("email" , email);
                            user.put("phoneNumber" , phoneno);

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG" , "success" );
                                }
                            });
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }
                        else
                        {
                            Toast.makeText(register.this,"user cant be created" + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                

            }


        });



        mlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),login.class));
            }
        });
    }
//    private void requestOTP(String phoneno) {
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneno, 60L, TimeUnit.SECONDS, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//            @Override
//            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                super.onCodeSent(s, forceResendingToken);
//                verificationId =s;
//                Token = forceResendingToken;
//                startActivity(new Intent(getApplicationContext(),OTP.class));
//            }
//
//            @Override
//            public void onCodeAutoRetrievalTimeOut(String s) {
//                super.onCodeAutoRetrievalTimeOut(s);
//            }
//
//            @Override
//            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
//
//            }
//
//            @Override
//            public void onVerificationFailed(FirebaseException e) {
//                Toast.makeText(register.this,"Task Failed "+  e.getMessage(),Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}
