package com.example.womensafety;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.telephony.SmsManager;
import android.util.Log;
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
    ResultReceiver resultReceiver;
    int REQUEST_CODE_LOCATION_PERMISSION = 1;
    String addressToSend;
    private SensorManager sensorManager;
    String currentLocation1 ,currentLocation2,currentLocation;


    private float acelVal;
    private float acelLast;
    private float shake;

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
        resultReceiver = new AddressResultReceiver(new Handler());

        ///////////////////////////////       Sensor/Shake Detector        ////////////////////////////////////

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        acelVal = SensorManager.GRAVITY_EARTH;
        acelLast = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;


        //////////////////////             Retrieving data from FireStore             //////////////////////////////////

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
            @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            @Override
            public void onClick(View v) {
                final boolean b1 = shareLocation.callOnClick();

                String sosMsg = " Danger SOS!!!!!!!!!!"  + "\n" +  currentLocation ;

                SmsManager mySmsManager = SmsManager.getDefault();
                mySmsManager.sendTextMessage(phone1, null, sosMsg, null, null);

//                SmsManager mySmsManager2 = SmsManager.getDefault();
//                mySmsManager2.sendTextMessage(phone2, null, sosMsg, null, null);

//                SmsManager mySmsManager3 = SmsManager.getDefault();
//               mySmsManager3.sendTextMessage(phone3,null, sosMsg,null,null);
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
//            Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }



    //////////////////////////////////   location provider ///////////////////////////////////////


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
                                                 "Latitude: %s\nLongitude: %s",latitude,longitude
                                        )
                                    );
//                                    currentLocation = "hello";
                                    currentLocation1  = String.valueOf(latitude );
                                    currentLocation2 = String.valueOf(longitude);
                                    currentLocation = currentLocation1 + "\n"  + currentLocation2;

                                    Location location = new Location("providerNA");
                                    location.setLatitude(latitude);
                                    location.setLatitude(longitude);
//                                    fetchAddressFromLatLong(location);
                                }
                            }
                        }, Looper.getMainLooper());

    }
    private void fetchAddressFromLatLong(Location location){
        Intent intent = new Intent(getApplicationContext(),fetchAddressIntentService.class);
        intent.putExtra(constants.RECEIVER,resultReceiver);
        intent.putExtra(constants.LOCATION_DATA_EXTRA,location);
        startService(intent);
    }

    private class AddressResultReceiver extends ResultReceiver{

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if(resultCode == constants.SUCCESS_RESULT)
            {
                addressToSend = resultData.getString(constants.RESULT_DATA_KEY);
                Log.d("MyLocation" , addressToSend);
                textLatLong.setText(addressToSend);
//                Toast.makeText(MainActivity.this, "yayyy" ,Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(MainActivity.this,"NOOOOO",Toast.LENGTH_SHORT).show();
//                resultData.getString(constants.RESULT_DATA_KEY)
            }
        }
    }

    ///////////////////////////////   Logout ////////////////////////////////////

    public void Logout(View view)
    {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),login.class));
        finish();
    }


    ////////////////////////////////      Shake Function      ///////////////////////////////


    private final SensorEventListener sensorListener = new SensorEventListener() {
        @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        @Override
        public void onSensorChanged(SensorEvent event) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            acelLast = acelVal;
            acelVal = (float) Math.sqrt( (double) (x*x + y*y + z*z));
            float delta = acelVal - acelLast;
            shake = shake * 0.9f + delta;
             if (shake > 12)
             {

                 final boolean b1 = shareLocation.callOnClick();
                 Toast.makeText(MainActivity.this,currentLocation, Toast.LENGTH_LONG).show();
//                 final boolean b2 = sos.callOnClick();
             }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

}
