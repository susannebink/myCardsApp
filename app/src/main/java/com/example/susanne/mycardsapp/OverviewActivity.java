package com.example.susanne.mycardsapp;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OverviewActivity extends AppCompatActivity {

    public DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        setListener();
        showCardsOverview();
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

    public void showCardsOverview(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String id = mAuth.getUid();
                User nUser = dataSnapshot.child(id).getValue(User.class);
                if (nUser == null){
                    Toast.makeText(OverviewActivity.this, "Uw lijst is nog leeg, voeg een kaart toe door " +
                            "op de 'Voeg een kaart toe' button te klikken", Toast.LENGTH_LONG).show();
                }
                else {
                    ArrayList<String> cardNames= nUser.getCardNames();
                    setList(cardNames);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OverviewActivity.this, "Kon gevraagde gegevens niet opvragen", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setList(ArrayList<String> mCards){
        ListView myCards = findViewById(R.id.myCards);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, mCards);
        myCards.setAdapter(adapter);

        myCards.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String store = adapterView.getItemAtPosition(i).toString();
                Intent intent = new Intent(OverviewActivity.this, ShowCardActivity.class);
                intent.putExtra("store", store);
                Log.d("StoreName", store);
                startActivity(intent);
            }
        });
    }
}
