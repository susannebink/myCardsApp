package com.example.susanne.mycardsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
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

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.susanne.mycardsapp.OverviewActivity.id;

public class SaveCardActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    ImageView barcodeView;
    Barcode barcode;
    Spinner spinner;
    String barcodeNumber;
    EditText getStoreName;
    String storeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_card);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Set the spinner with store names.
        setSpinner();

        barcodeView = findViewById(R.id.barcode);
        getStoreName = findViewById(R.id.storeName);

        // Get the barcode from AddCardActivity.
        barcode = getIntent().getParcelableExtra("barcode");
        barcodeNumber = barcode.rawValue;

        // Try to create the barcode image.
        try {
            barcodeView.setImageBitmap(createBarcode(barcodeNumber));
        } catch (WriterException e) {
            Toast.makeText(this, "Barcode kon niet geladen worden", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets the adapter for the spinner and the spinner itself with the store names.
     */
    private void setSpinner() {
        spinner = findViewById(R.id.storeSpinner);

        // Set the adapter witb store array with a number of standard store names.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.store_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Function to recreate the barcode with a given barcode number (data).
     */
    static Bitmap createBarcode(String data) throws WriterException {
        int size_l = 800;
        int size_w = 500;
        MultiFormatWriter barcodeWriter = new MultiFormatWriter();
        Bitmap barcodeBitmap;

        // Try to create the barcode with type EAN 13.
        try {
            BitMatrix barcodeBitMatrix = barcodeWriter.encode(data, BarcodeFormat.EAN_13, size_l, size_w);
            barcodeBitmap = Bitmap.createBitmap(size_l, size_w, Bitmap.Config.ARGB_8888);

            // Iterate over the width and length and set each pixel correctly.
            for (int x = 0; x < size_l; x++) {
                for (int y = 0; y < size_w; y++) {
                    barcodeBitmap.setPixel(x, y, barcodeBitMatrix.get(x, y) ?
                            Color.BLACK : Color.WHITE);
                }
            }
        }
        // Try to create the barcode with type CODE 39.
        catch (Exception e){
            BitMatrix barcodeBitMatrix = barcodeWriter.encode(data, BarcodeFormat.CODE_39, size_l, size_w);
            barcodeBitmap = Bitmap.createBitmap(size_l, size_w, Bitmap.Config.ARGB_8888);

            // Iterate over the width and length and set each pixel correctly.
            for (int x = 0; x < size_l; x++) {
                for (int y = 0; y < size_w; y++) {
                    barcodeBitmap.setPixel(x, y, barcodeBitMatrix.get(x, y) ?
                            Color.BLACK : Color.WHITE);
                }
            }
        }
        return barcodeBitmap;
    }

    /**
     * On click listener for add card button. Checks the filled/selected in store name and adds the
     * information to the user's database.
     */
    public void checkStoreName(View view){
        final String store = getStoreName.getText().toString();
        String chosen = spinner.getSelectedItem().toString();

        // Check whether a name is chosen.
        if (chosen.equals("Selecteer de winkel") && store.equals("") ||
                (!chosen.equals("Selecteer de winkel") && !store.equals(""))){
            Toast.makeText(this, "Selecteer een winkel", Toast.LENGTH_LONG).show();
        }
        // If the user has filled in the store name in the edit text view.
        else if (!store.equals("")){

            // Check if the filled in name is also in the spinner, else create a dialog.
            String[] storeArray = getResources().getStringArray(R.array.store_array);
            if (Arrays.asList(storeArray).contains(store)){
                storeName = store;
                addCardToDB();
            }else{
                setNameDialog(store);
            }
        }
        // If the user had chosen the store name from the spinner, add the card to the database.
        else{
            storeName = chosen;
            addCardToDB();
        }
    }

    /**
     * This function builds a dialog to alert the user about the given store name.
     */
    public void setNameDialog(final String store){
        AlertDialog.Builder builder = new AlertDialog.Builder(SaveCardActivity.this);
        builder.setMessage("Let op. Google map services werken niet bij een verkeerde winkelnaam")
                .setTitle("Winkelnaam invoeren");

        // If user agrees with the chosen name, add the card to the database and cancel dialog.
        builder.setPositiveButton("Ik ga akkoord met deze naam",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        storeName = store;
                        dialog.cancel();
                        addCardToDB();
                    }
                });

        // If user does not agree with the chosen name, cancel dialog.
        builder.setNegativeButton("Verander deze naam", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Function to add the chosen card to the user's database.
     */
    public void addCardToDB() {
        final Card nCard = new Card(storeName, barcodeNumber);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User thisUser = dataSnapshot.child("Users").child(id).getValue(User.class);

                /* If this is the first card the user adds, the user will be added to the database
                 * with the card.
                 */
                if (thisUser == null){
                    User nUser = new User(new ArrayList<Card>());
                    nUser.addCard(nCard);
                    databaseReference.child("Users").child(id).setValue(nUser);
                    goToOverview();
                }
                // If user is already in database, check if the chosen card was not already added.
                else {
                    // If card was already added, make a dialog.
                    if (thisUser.checkCard(storeName)){
                        makeDialog(thisUser, storeName);
                    }
                    // If card was not added yet, add it to the database.
                    else {
                        thisUser.addCard(nCard);
                        databaseReference.child("Users").child(id).setValue(thisUser);
                        goToOverview();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SaveCardActivity.this, "Kon gegevens niet opvragen",Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Make a dialog for the user to whether update the chosen card or cancel the action.
     */
    public void makeDialog(final User nUser, final String chosen){
        AlertDialog.Builder builder = new AlertDialog.Builder(SaveCardActivity.this);
        builder.setMessage("U heeft deze kaart al toegevoegd. Wilt u de barcode updaten?")
                .setTitle("Barcode updaten");
        // User wants to update the barcode in the database, update this barcode.
        builder.setPositiveButton("Ja, update deze barcode", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                nUser.updateCard(chosen, barcodeNumber);
                databaseReference.child("Users").child(id).setValue(nUser);
                dialog.cancel();
                goToOverview();
            }
        });
        // Cancel the dialog and let the user chose an other card name.
        builder.setNegativeButton("Annuleren", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Start OverviewActivity.
     */
    public void goToOverview(){
        Intent intent = new Intent(SaveCardActivity.this, OverviewActivity.class);
        startActivity(intent);
        finish();
    }
}
