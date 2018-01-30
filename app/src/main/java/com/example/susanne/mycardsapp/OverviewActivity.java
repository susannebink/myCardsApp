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
    static String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        id = mAuth.getUid();

        setListener();
        showCardsOverview();
    }

    /**
     * Check if user is signed in, if not, go to LoginActivity.
      */
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

    /**
     * If user navigates back via the back-button, check if user is still signed in and
     * set the list with the cards again.
     */
    @Override
    protected void onResume() {
        super.onResume();
        setListener();
        showCardsOverview();
    }
    /**
     * Check if user is signed in (non-null) and update UI accordingly.
     */
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    /**
     * This function opens a barcodescanner in AdCardActivity. If a barcode is returned, start
     * the SaveCardActivity and give the barcode as an extra with the intent.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int REQUEST_CODE = 0;
        if (requestCode == REQUEST_CODE && resultCode == CommonStatusCodes.SUCCESS && data != null) {
            Barcode barcode = data.getParcelableExtra("barcode");
            Intent intent = new Intent(this, SaveCardActivity.class);
            intent.putExtra("barcode", barcode);
            startActivity(intent);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Function to get the user's information (cards) from the firebase database. If the information
     * is received, set the cards list.
     */
    public void showCardsOverview(){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User nUser = dataSnapshot.child("Users").child(id).getValue(User.class);
                if (nUser == null){
                    Toast.makeText(OverviewActivity.this, "Uw lijst is nog leeg, voeg een kaart toe door " +
                            "op de 'Voeg een kaart toe' button te klikken", Toast.LENGTH_LONG).show();
                }
                else {
                    setList(nUser);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OverviewActivity.this, "Kon gevraagde gegevens niet opvragen", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This function sets to kinds of lists in one list in one listview, sets the adapter of the
     * listview, the onItemClickListener and the onItemLongClickListener.
     * @param nUser is the User class that was received from the database
     */
    public void setList(User nUser){
        // Get the arraylists with the cards from the user class.
        final ArrayList<String> mFavoCards = nUser.getFavorites();
        final ArrayList<String> mCards = nUser.getCardNames();

        // Define the length of the lists.
        numberOfFavorites = mFavoCards.size();
        numberOfNormal = mCards.size();

        // Make an new arraylist which will be used for the listview.
        ArrayList<String> allCards = new ArrayList<>();
        final ListView myCards = findViewById(R.id.myCards);

        /*
        * If the number of favorite cards is greater than zero, add this to allCards.
        * If the number of normal cards is also creater than zero, add this too to allCards.
         */
        if (numberOfFavorites > 0) {
            allCards.add("Favoriete Kaarten:");
            allCards.addAll(mFavoCards);
            if (numberOfNormal > 0) {
                allCards.add("Overige Kaarten:");
                allCards.addAll(mCards);
            }
        }
        // If the user has no favorite cards, just add the normal cards to allCards.
        else{
            allCards.addAll(mCards);
        }

        // Set adapter and item click listeners for the listview.
        myCards.setAdapter(makeCardsAdapter(allCards));
        myCards.setOnItemClickListener(new ListOnItemClickListener());
        myCards.setOnItemLongClickListener(new ListOnItemLongClickListener());
    }

    /**
     * Make the adapter for the listview with cards. Disable the headers for click listeners.
     */
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

    /**
     * Define the on item long click listener for the myCards listview. The card that was clicked
     * will be deleted from the database.
     */
    private class ListOnItemLongClickListener implements AdapterView.OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            String thisCard = parent.getItemAtPosition(position).toString();
            removeCardFromDB(thisCard);
            return true;
        }
    }

    /**
     * Define the on item click listener for the myCards listview. The card that was clicked
     * will be opened in ShowCardActivity.
     */
    private class ListOnItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String store = adapterView.getItemAtPosition(i).toString();
            Intent intent = new Intent(OverviewActivity.this, ShowCardActivity.class);
            intent.putExtra("store", store);

            // Add the boolean favorite as an extra to the intent.
            if (i < numberOfFavorites+1 && numberOfFavorites != 0){
                intent.putExtra("favorite", true);
            }
            else {
                intent.putExtra("favorite", false);
            }

            startActivity(intent);
        }
    }

    /**
     * On click for a button. Starts AddCardActivity.
     */
    public void goToAddCard(View view) {
        Intent intent = new Intent(OverviewActivity.this, AddCardActivity.class);
        startActivityForResult(intent, 0);
    }

    /**
     * On click for a button. Signs out the user and starts LoginActivity.
     */
    public void logOut(View view) {
        mAuth.signOut();
        Intent intent = new Intent(OverviewActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Function to remove the given card (parameter name) from the user class and database.
     */
    public void removeCardFromDB(final String name) {
       databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User thisUser = dataSnapshot.child("Users").child(id).getValue(User.class);
                createDialog(thisUser, name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OverviewActivity.this, "Kon gegevens niet opvragen",Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Creates an dialog that asks the user if he/she is sure to want to delete the selected card.
     * If the user is sure to delete the card, the card is deleted from the user class and
     * the user is updated in the database.
     */
    public void createDialog(final User nUser, final String name){
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
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
