package com.example.susanne.mycardsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShowCardActivity extends AppCompatActivity implements OnMapReadyCallback{
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    ImageView barcode;
    TextView cardNumber;
    TextView storeName;
    TextView showRadius;
    SeekBar radius;
    GoogleMap mMap;
    Location myLocation;
    Float DEFAULT_ZOOM = 13f;
    private FusedLocationProviderClient mClient;
    private Boolean locationPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_card);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        barcode = findViewById(R.id.cardBarcode);
        storeName = findViewById(R.id.storeName);
        cardNumber = findViewById(R.id.barcodeNumber);
        radius = findViewById(R.id.mRadius);
        showRadius = findViewById(R.id.showRadius);

        String store = getIntent().getStringExtra("store");
        storeName.setText(store);
        showRadius.setText("0 km");
        getStoreCard(store);
        setSeekBar();
//        serviceOK();
        getLocationPermission();

    }

    private void setSeekBar() {
        radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                showRadius.setText(progress + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void getStoreCard(final String name){
        final String id = mAuth.getUid();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User thisUser = dataSnapshot.child("Users").child(id).getValue(User.class);
                String barcode = thisUser.getCardBarcode(name);
                cardNumber.setText(barcode);
                createImage(barcode);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ShowCardActivity.this, "Kon gegevens niet opvragen",Toast.LENGTH_LONG).show();
            }
        });
    }


    public void createImage(String thisBarcode){
        try {
            barcode.setImageBitmap(SaveCardActivity.createBarcode(thisBarcode));
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(ShowCardActivity.this, "Barcode kon niet geladen worden", Toast.LENGTH_LONG).show();
        }
    }

//    public boolean serviceOK(){
//        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(ShowCardActivity.this);
//
//        if (available == ConnectionResult.SUCCESS){
//            return true;
//        }
//        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
//            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(ShowCardActivity.this, available, ERROR_DIALOG_REQUEST);
//            dialog.show();
//        }
//        else {
//            Toast.makeText(ShowCardActivity.this, "Kan geen verzoeken doen voor map", Toast.LENGTH_LONG).show();
//        }
//        return false;
//    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){
                locationPermission = true;
                initMap();
            }
            else {
                ActivityCompat.requestPermissions(ShowCardActivity.this, permissions, 1111);
            }
        }
        else {
            ActivityCompat.requestPermissions(ShowCardActivity.this, permissions, 1111);
        }
    }
    private void getUserLocation(){
        mClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(locationPermission){

                final Task location = mClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            myLocation = (Location) task.getResult();

                            moveCamera(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), DEFAULT_ZOOM);
                            Log.d("location", "This is your location:" + myLocation.getLatitude() +"," + myLocation.getLongitude());

                        }else{
                            Toast.makeText(ShowCardActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e("ShowCardActivity", "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d("ShowCardActivity", "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        getNearestStore();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermission = false;
        switch (requestCode){
            case 1111:{
                if (grantResults.length > 0){
                    for (int i=0; i < grantResults.length; i++){
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                            locationPermission = false;
                            return;
                        }
                    }
                    locationPermission = true;
                    initMap();
                }
            }
        }
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.myMap);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (locationPermission){
            getUserLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    public void getNearestStore() {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+
                myLocation.getLatitude() +"," + myLocation.getLongitude() +
                "&radius=2000&type=store&name="+ storeName.getText().toString() + "&key=AIzaSyC4vb2hh0SG8dPo1UuFnCxnE3D4Uk2fm3E";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++){
                                JSONObject thisStore = (JSONObject) results.get(i);
                                JSONObject storeLocation = thisStore.getJSONObject("geometry").getJSONObject("location");
                                String title = thisStore.getString("name");
                                LatLng thisLocation = new LatLng(storeLocation.getDouble("lat"), storeLocation.getDouble("lng"));
                                Log.d("thisstorelocation", thisLocation.toString());

                                MarkerOptions options = new MarkerOptions()
                                        .position(thisLocation);
                                mMap.addMarker(options);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ShowCardActivity.this, "That didn't work", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(request);
    }
}
