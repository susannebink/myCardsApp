package com.example.susanne.mycardsapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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
import com.google.zxing.WriterException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ShowCardActivity extends AppCompatActivity implements OnMapReadyCallback{
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    ImageView barcode;
    TextView cardNumber;
    TextView storeName;
    TextView showRadius;
    ProgressDialog progressDialog;
    SeekBar radius;
    String store;
    GoogleMap mMap;
    Boolean isFavorite;
    ImageButton favoriteButton;
    Location myLocation;
    String id;
    private static final int REQUEST_CODE = 1111;
    private Float DEFAULT_ZOOM = 13f;
    private Boolean locationPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_card);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        id = mAuth.getUid();

        // Get the card information from the intent.
        isFavorite = getIntent().getBooleanExtra("favorite", false);
        store = getIntent().getStringExtra("store");

        // Make a progress dialog while the information is loading.
        progressDialog = new ProgressDialog(ShowCardActivity.this, R.style.MyDialogTheme);
        progressDialog.setTitle("Kaart aan het laden");
        progressDialog.setMessage("Even geduld aub");
        progressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progressDialog.show();

        // Set all necessary views for activity.
        setViews();

        // Set the seekbar to set radius of nearest store.
        setSeekBar();

        // Get the card information.
        getStoreCard();

        // Get permission for user's location.
        getLocationPermission();
    }

    /**
     * Function to find all necessary views and set some of them.
     */
    public void setViews(){
        barcode = findViewById(R.id.cardBarcode);
        storeName = findViewById(R.id.storeName);
        cardNumber = findViewById(R.id.barcodeNumber);
        radius = findViewById(R.id.mRadius);
        showRadius = findViewById(R.id.showRadius);
        favoriteButton = findViewById(R.id.favorite);
        storeName.setText(store);

        showRadius.setText(R.string.initial_radius);

        // Set the corresponding favorite image button.
        if (isFavorite){
            favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
        } else{
            favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    /**
     * Function to listen to changes on the seekbar. If something has changed, update the nearest
     * stores.
     */
    private void setSeekBar() {
        radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Set the progress of the seekbar in a textview so the user can keep track.
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                showRadius.setText(progress + " m");
            }
            // Alert the user that stores will be searched.
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(ShowCardActivity.this, "Zoeken naar winkels...",
                        Toast.LENGTH_SHORT).show();
            }
            /*
             * When user had stopped shifting the seekbar, search for the nearest store in the
             * chosen radius.
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                getNearestStore(progress);
            }
        });
    }

    /**
     * Get the current card's barcode from the user's firebase database and create the barcode
     * image.
     */
    public void getStoreCard(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Get the information of the selected card.
                User thisUser = dataSnapshot.child("Users").child(id).getValue(User.class);
                String barcode = null;
                if (thisUser != null) {
                    barcode = thisUser.getCardBarcode(store);
                }

                // Set the barcode information on the views.
                cardNumber.setText(barcode);
                createImage(barcode);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ShowCardActivity.this, "Kon gegevens niet opvragen",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Try to create the image of the given barcode.
     */
    public void createImage(String thisBarcode){
        try {
            // Set the image of the barcode.
            barcode.setImageBitmap(SaveCardActivity.createBarcode(thisBarcode));

            // Dismiss the progress dialog when the barcode image was set.
            progressDialog.dismiss();
        } catch (WriterException e) {
            Toast.makeText(ShowCardActivity.this, "Barcode kon niet geladen worden",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Check if the user has given permission for fine location and for coarse location. If not, ask
     * these permissions. If the user has given the permission, initialize the map.
     */
    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        // Check if all location permissions where given, if true, initialize the map.
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED){
                locationPermission = true;
                initMap();
            }

            // Request permission.
            else {
                ActivityCompat.requestPermissions(ShowCardActivity.this, permissions,
                        REQUEST_CODE);
            }
        }
        // Request permission.
        else {
            ActivityCompat.requestPermissions(ShowCardActivity.this, permissions,
                    REQUEST_CODE);
        }
    }

    /**
     * If the user has given the location permissions, try to find the current location of the user
     * and move the camera to this location.
     */
    private void getUserLocation(){
        FusedLocationProviderClient mClient =
                LocationServices.getFusedLocationProviderClient(this);
        try{
            // If user has given location permission, get the user's last known location.
            if(locationPermission){
                final Task location = mClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){

                            // Move the map's camera to the user's location.
                            myLocation = (Location) task.getResult();
                            moveCamera(new LatLng(myLocation.getLatitude(),
                                    myLocation.getLongitude()), DEFAULT_ZOOM);
                        }
                        else{
                            // Alert user that his location could not be found.
                            Toast.makeText(ShowCardActivity.this,
                                    "Kon huidige locatie niet vinden", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
            }
        } catch (SecurityException e){
            Log.e("ShowCardActivity", e.getMessage());
        }
    }

    /**
     * Move the map's camera to the given position and zoom. After that, get the nearest store of
     * the selected card.
     */
    private void moveCamera(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        getNearestStore(radius.getProgress());
    }

    /**
     * Get the result for the permission requests for user's the fine location and the coarse
     * location.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermission = false;
        switch (requestCode){
            case REQUEST_CODE:{
                /* Check all the permission request results if one of them is false,
                 * set the boolean locationPermission false.
                 */
                if (grantResults.length > 0){
                    for (int grantResult : grantResults) {
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            locationPermission = false;
                            return;
                        }
                    }

                    // If all permissions are granted, initialize the map.
                    locationPermission = true;
                    initMap();
                }
            }
        }
    }

    /**
     * Initialize the map and display the map on the map fragment.
     */
    private void initMap(){
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.myMap);
        mapFragment.getMapAsync(this);
    }

    /**
     * If map is ready, show user's location. Enable my location button and zoom controls for the
     * map. Check the permissions once again.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set the loaded google maps map on the map fragment.
        mMap = googleMap;
        if (locationPermission){
            // Get the user's location.
            getUserLocation();

            // Check the fine location and coarse location permission again.
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED){
                return;
            }
            // Enable my location, my location button and zoom controls.
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    /**
     * Get the nearest of the selected store in a given radius. Stores are received via a
     * JSONRequest to google maps places.
     */
    public void getNearestStore(int radius) {
        /*
         * Url for json request to google maps places. The request consists of the current location
         * of the user, the chosen radius, the name of the store and the API key for google places.
         */
        String url =
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                        + myLocation.getLatitude() + "," + myLocation.getLongitude()
                        + "&radius=" + radius
                        + "&type=point_of_interest&name="
                        + storeName.getText().toString()
                        + "&key=AIzaSyC4vb2hh0SG8dPo1UuFnCxnE3D4Uk2fm3E";

        // Clear current markers from the map.
        mMap.clear();

        // Make the request and add it to the queue.
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(makeRequest(url));
    }

    /**
     * This function will make the json request and return this.
     */
    public JsonObjectRequest makeRequest(String url){
        return new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            /*
                             * If request gets response, set the markers of the stores from the
                             * results.
                             */
                            JSONArray results = response.getJSONArray("results");
                            setMarkers(results);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ShowCardActivity.this, "Kon gegevens niet opvragen",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets the markers of the stores from the json request
     */
    public void setMarkers(JSONArray results) throws JSONException {
        // Check if the results aren't empty.
        if (results.length() == 0){
            Toast.makeText(ShowCardActivity.this,
                    "Kon gevraagde winkel niet vinden in dit gebied", Toast.LENGTH_SHORT).show();
        } else {
            for (int i = 0; i < results.length(); i++) {
                JSONObject thisStore = (JSONObject) results.get(i);

                // Get the location and the opening hours of the store.
                JSONObject storeLocation =
                        thisStore.getJSONObject("geometry").getJSONObject("location");
                Boolean openNow =
                        thisStore.getJSONObject("opening_hours").getBoolean("open_now");

                // Set if the store is opened as the title of the marker.
                String title;
                if (openNow) {
                    title = "Nu geopend";
                } else {
                    title = "Nu gesloten";
                }

                // Get latitude and longitude from store location.
                LatLng thisLocation = new LatLng(storeLocation.getDouble("lat"),
                        storeLocation.getDouble("lng"));

                // Make marker and add marker to map.
                MarkerOptions options = new MarkerOptions()
                        .position(thisLocation)
                        .title(title);
                mMap.addMarker(options);
            }
        }
    }

    /**
     * On click method for favorite image button. Update the current state of favorite of the card.
     */
    public void setFavorites(View view) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Update the boolean favorite of the card in the database.
                User thisUser = dataSnapshot.child("Users").child(id).getValue(User.class);
                assert thisUser != null;
                thisUser.updateFavorite(storeName.getText().toString());

                /*
                 * If card was favorite, set not favorite and if card was not favorite,
                 * set favorite.
                 */
                if (isFavorite){
                    favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
                    isFavorite = false;
                } else{
                    favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
                    isFavorite = true;
                }

                // Add the new value to the database.
                databaseReference.child("Users").child(id).setValue(thisUser);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ShowCardActivity.this, "Kon gegevens niet opvragen",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
