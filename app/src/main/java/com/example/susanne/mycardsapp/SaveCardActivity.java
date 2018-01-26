package com.example.susanne.mycardsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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

import static com.example.susanne.mycardsapp.OverviewActivity.id;

public class SaveCardActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    ImageView barcodeView;
    Barcode barcode;
    Spinner spinner;
    String barcodeNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_card);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        setSpinner();

        barcodeView = findViewById(R.id.barcode);
        barcode = getIntent().getParcelableExtra("barcode");
        barcodeNumber = barcode.rawValue;

        try {
            barcodeView.setImageBitmap(createBarcode(barcodeNumber));
        } catch (WriterException e) {
            Toast.makeText(this, "Barcode kon niet gegenereerd worden", Toast.LENGTH_SHORT).show();
        }
    }

    private void setSpinner() {
        spinner = findViewById(R.id.store_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.store_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    static Bitmap createBarcode(String data) throws WriterException {
        int size_l = 800;
        int size_w = 500;
        MultiFormatWriter barcodeWriter = new MultiFormatWriter();
        Bitmap barcodeBitmap;

        try {
            BitMatrix barcodeBitMatrix = barcodeWriter.encode(data, BarcodeFormat.EAN_13, size_l, size_w);
            barcodeBitmap = Bitmap.createBitmap(size_l, size_w, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < size_l; x++) {
                for (int y = 0; y < size_w; y++) {
                    barcodeBitmap.setPixel(x, y, barcodeBitMatrix.get(x, y) ?
                            Color.BLACK : Color.WHITE);
                }
            }
        }
        catch (Exception e){
            BitMatrix barcodeBitMatrix = barcodeWriter.encode(data, BarcodeFormat.CODE_39, size_l, size_w);
            barcodeBitmap = Bitmap.createBitmap(size_l, size_w, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < size_l; x++) {
                for (int y = 0; y < size_w; y++) {
                    barcodeBitmap.setPixel(x, y, barcodeBitMatrix.get(x, y) ?
                            Color.BLACK : Color.WHITE);
                }
            }
        }
        return barcodeBitmap;
    }
    public void addCardToDB(View view) {
        final String chosen = spinner.getSelectedItem().toString();
        if (chosen.equals("Selecteer de winkel")){
            Toast.makeText(this, "Selecteer een winkel", Toast.LENGTH_LONG).show();
        }
        else {
            final Card nCard = new Card(chosen, barcodeNumber);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final User thisUser = dataSnapshot.child("Users").child(id).getValue(User.class);
                    if (thisUser == null){
                        User nUser = new User(new ArrayList<Card>());
                        nUser.addCard(nCard);
                        databaseReference.child("Users").child(id).setValue(nUser);
                        goToOverview();
                    } else {
                        if (thisUser.checkCard(chosen)){
                            makeDialog(thisUser, chosen);
                        } else {
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
    }
    public void makeDialog(final User nUser, final String chosen){
        AlertDialog.Builder builder = new AlertDialog.Builder(SaveCardActivity.this);
        builder.setMessage("U heeft deze kaart al toegevoegd. Wilt u de barcode updaten?").setTitle("Barcode updaten");
        builder.setPositiveButton("Ja, update deze barcode", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                nUser.updateCard(chosen, barcodeNumber);
                databaseReference.child("Users").child(id).setValue(nUser);
                dialog.cancel();
                goToOverview();
            }
        });
        builder.setNegativeButton("Annuleren", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                goToOverview();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void goToOverview(){
        Intent intent = new Intent(SaveCardActivity.this, OverviewActivity.class);
        startActivity(intent);
        finish();
    }
}
