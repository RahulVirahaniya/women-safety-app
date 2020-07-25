package com.example.womensafety;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class OTP extends AppCompatActivity {
    EditText fullotp;
    Button verify;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        fullotp = findViewById(R.id.otpid1);
        verify = findViewById(R.id.confirmOTP);


    }
}
