package com.example.susanne.mycardsapp;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class ShowCardActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    ImageView barcode;
    TextView cardNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_card);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        barcode = findViewById(R.id.cardBarcode);
        TextView storeName = findViewById(R.id.storeName);
        cardNumber = findViewById(R.id.barcodeNumber);

        String store = getIntent().getStringExtra("store");
        storeName.setText(store);
        getStoreCard(store);
    }

    public void getStoreCard(final String name){
        final String id = mAuth.getUid();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User thisUser = dataSnapshot.child(id).getValue(User.class);
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
            barcode.setImageBitmap(createBarcode(thisBarcode));
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(ShowCardActivity.this, "Barcode kon niet geladen worden", Toast.LENGTH_LONG).show();
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
}
