package com.example.susanne.mycardsapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OverviewActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        mAuth = FirebaseAuth.getInstance();
        setListener();

        ListView myCards = findViewById(R.id.myCards);
        myCards.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String store = adapterView.getItemAtPosition(i).toString();
                Intent intent = new Intent(OverviewActivity.this, ShowCardActivity.class);
                intent.putExtra("store", store);
            }
        });
    }

    // Check if user is already signed in
    public void setListener(){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    Log.d("signed in", "OnAuthStateChanged:signed_in" + user.getUid());
                }
                else {
                    Intent intent = new Intent(OverviewActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    Log.d("signed out", "OnAuthStateChanged:signed_out" );
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void goToAddCard(View view) {
        Intent intent = new Intent(OverviewActivity.this, AddCardActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == CommonStatusCodes.SUCCESS && data != null){
            Barcode barcode = data.getParcelableExtra("barcode");
            Intent intent = new Intent(this, SaveCardActivity.class);
            intent.putExtra("barcode", barcode);
            startActivity(intent);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void goToShowCard(View view) {
        Intent intent = new Intent(OverviewActivity.this, ShowCardActivity.class);
        startActivity(intent);
    }
}
