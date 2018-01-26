package com.example.susanne.mycardsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
    int numberOfFavorites;
    int numberOfNormal;

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
    protected void onResume() {
        super.onResume();
        setListener();
        showCardsOverview();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
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

    public void showCardsOverview(){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String id = mAuth.getUid();
                User nUser = dataSnapshot.child("Users").child(id).getValue(User.class);
                if (nUser == null){
                    Toast.makeText(OverviewActivity.this, "Uw lijst is nog leeg, voeg een kaart toe door " +
                            "op de 'Voeg een kaart toe' button te klikken", Toast.LENGTH_LONG).show();
                }
                else {
                    setList(nUser);}
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OverviewActivity.this, "Kon gevraagde gegevens niet opvragen", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setList(User nUser){
        final ArrayList<String> mFavoCards = nUser.getFavorites();
        final ArrayList<String> mCards = nUser.getCardNames();
        numberOfFavorites = mFavoCards.size();
        numberOfNormal = mCards.size();

        ArrayList<String> allCards = new ArrayList<>();
        final ListView myCards = findViewById(R.id.myCards);

        if (numberOfFavorites > 0) {
            allCards.add("Favoriete Kaarten:");
            allCards.addAll(mFavoCards);
            if (numberOfNormal > 0){
                allCards.add("Overige Kaarten:");
                allCards.addAll(mCards);
            }
        } else{
            allCards.addAll(mCards);
        }
        myCards.setAdapter(makeCardsAdapter(allCards));
        myCards.setOnItemClickListener(new ListOnItemClickListener());
        myCards.setOnItemLongClickListener(new ListOnItemLongClickListener());
    }

    public ArrayAdapter makeCardsAdapter(ArrayList<String> allCards){
        return new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, allCards){
            @Override
            public boolean isEnabled(int position) {
                if ((numberOfFavorites > 0 && numberOfNormal == 0 && position == 0)){
                    return false;
                }
                else if (numberOfNormal > 0 && numberOfFavorites > 0 && (position == numberOfFavorites + 1 || position == 0)){
                    return false;
                }
                return true;
            }
        };
    }

    private class ListOnItemLongClickListener implements AdapterView.OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            String thisCard = parent.getItemAtPosition(position).toString();
            removeCardFromDB(thisCard);
            return true;
        }
    }

    private class ListOnItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String store = adapterView.getItemAtPosition(i).toString();
            Intent intent = new Intent(OverviewActivity.this, ShowCardActivity.class);
            intent.putExtra("store", store);
            if (i < numberOfFavorites+1 && numberOfFavorites != 0){
                intent.putExtra("favorite", true);
            }
            else {
                intent.putExtra("favorite", false);
            }
            Log.d("StoreName", store);
            startActivity(intent);
        }
    }


    public void goToAddCard(View view) {
        Intent intent = new Intent(OverviewActivity.this, AddCardActivity.class);
        startActivityForResult(intent, 0);
    }

    public void logOut(View view) {
        mAuth.signOut();
        Intent intent = new Intent(OverviewActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    public void removeCardFromDB(final String name) {
       databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String id = mAuth.getUid();
                User thisUser = dataSnapshot.child("Users").child(id).getValue(User.class);
                createDialog(thisUser, id, name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OverviewActivity.this, "Kon gegevens niet opvragen",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void createDialog(final User nUser, final String id, final String name){
        AlertDialog.Builder builder = new AlertDialog.Builder(OverviewActivity.this);
        builder.setMessage("Weet u zeker dat u deze kaart wilt verwijderen?").setTitle("Verwijder kaart");
        builder.setPositiveButton("Ja, verwijder deze kaart", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                nUser.deleteCard(name);
                databaseReference.child("Users").child(id).setValue(nUser);
                dialog.cancel();
                setList(nUser);
            }
        });
        builder.setNegativeButton("Annuleren", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
