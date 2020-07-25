package com.example.womensafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;


public class MainActivity extends AppCompatActivity {
    Button addNo;
    Button sos, shareLocation;
    String phone1, phone2, phone3, userId;
    FirebaseFirestore fstore;
    FirebaseAuth fAuth;
    TextView textLatLong;
    int REQUEST_CODE_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);


        addNo = findViewById(R.id.newNumber);
        textLatLong = findViewById(R.id.text);
        sos = findViewById(R.id.sos);
        shareLocation = findViewById(R.id.shareLocation);
        fstore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        userId = fAuth.getCurrentUser().getUid();
        final DocumentReference documentReference = fstore.collection("user").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                phone1 = documentSnapshot.getString("number1");
                phone2 = documentSnapshot.getString("number2");
                phone3 = documentSnapshot.getString("number3");
            }
        });


        ///////////////////////////////           ADD NEW NUMERS        //////////////////////////////////////
        addNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), addNumber.class));
            }
        });
        /////////////////////////////////   SOS     //////////////////////////////////
        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sosMsg = " Danger SOS!!!!!!!!!!";

                SmsManager mySmsManager = SmsManager.getDefault();
                mySmsManager.sendTextMessage(phone1, null, sosMsg, null, null);

                SmsManager mySmsManager2 = SmsManager.getDefault();
                mySmsManager2.sendTextMessage(phone2, null, sosMsg, null, null);

//                mySmsManager.sendTextMessage(phone3,null, sosMsg,null,null);
                Toast.makeText(MainActivity.this, "Message Sent ", Toast.LENGTH_SHORT).show();
//                Toast.makeText(MainActivity.this, phone1 + phone2 + phone3 , Toast.LENGTH_LONG).show();
            }
        });

        /////////////////////////////// SHARE LOCATION    /////////////////////////////////////////
        shareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );
                } else {
                    getCurrentLocation();
                }


            }
        });


    }
//    public boolean checkPermission(String Permission)
//    {
//        int check = ContextCompat.checkSelfPermission(this,Permission);
//        return (check== PackageManager.PERMISSION_GRANTED);
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            getCurrentLocation();
        } else {
            Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation() {

        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback(){

                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                        .removeLocationUpdates(this);
                                if(locationResult!=null && locationResult.getLocations().size()>0){
                                    int lastestLocationIndex = locationResult.getLocations().size()-1;
                                    double latitude =
                                            locationResult.getLocations().get(lastestLocationIndex).getLatitude();
                                    double longitude =
                                            locationResult.getLocations().get(lastestLocationIndex).getLongitude();
                                    textLatLong.setText(
                                        String.format(
                                                "Latitude: %s/nLongitude: %s",latitude,longitude
                                        )
                                    );
                                }
                            }
                        }, Looper.getMainLooper());

    }
    public void Logout(View view)
    {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),login.class));
        finish();
    }
}
