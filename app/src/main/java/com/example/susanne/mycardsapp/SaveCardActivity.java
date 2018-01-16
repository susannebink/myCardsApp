package com.example.susanne.mycardsapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class SaveCardActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    ImageView barcodeView;
    Barcode barcode;
    Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_card);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        spinner = findViewById(R.id.store_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.store_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        barcodeView = findViewById(R.id.barcode);
        barcode = getIntent().getParcelableExtra("barcode");
        try {
            barcodeView.setImageBitmap(createBarcode(barcode.rawValue));
        } catch (WriterException e) {
            Toast.makeText(this, "Barcode kon niet gegenereerd worden", Toast.LENGTH_SHORT).show();
        }
    }

    Bitmap createBarcode(String data) throws WriterException {
        int size_l = 600;
        int size_w = 400;
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
        String chosen = spinner.getSelectedItem().toString();
        if (chosen.equals("Selecteer de winkel")){
            Toast.makeText(this, "Selecteer een winkel", Toast.LENGTH_LONG).show();
        }
        else {
            final Card nCard = new Card(chosen, barcode.rawValue);
            final String id = mAuth.getCurrentUser().getUid();

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User thisUser = dataSnapshot.child(id).getValue(User.class);
                    thisUser.addCard(nCard);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(SaveCardActivity.this, "Kon gegevens niet opvragen",Toast.LENGTH_LONG).show();
                }
            });
            Intent intent = new Intent(SaveCardActivity.this, OverviewActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
