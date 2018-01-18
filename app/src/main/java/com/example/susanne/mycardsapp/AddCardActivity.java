package com.example.susanne.mycardsapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.vision.barcode.Barcode;
import android.util.SparseArray;
import android.widget.Toast;
import info.androidhive.barcode.BarcodeReader;

import java.util.List;


public class AddCardActivity extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener{
    private int myCameraRequestCode = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        if (ActivityCompat.checkSelfPermission(AddCardActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddCardActivity.this, new String[]{Manifest.permission.CAMERA}, myCameraRequestCode);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == myCameraRequestCode) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(AddCardActivity.this, AddCardActivity.class);
                startActivity(intent);
                finish();

            } else {

                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();

            }

        }
    }

    public void goToSaveCard(Barcode barcode) {
        Intent intent = new Intent(AddCardActivity.this, SaveCardActivity.class);
        intent.putExtra("barcode", barcode);
        startActivity(intent);
        finish();
    }

    @Override
    public void onScanned(Barcode barcode) {
        Log.d("Barcode", barcode.rawValue);
        goToSaveCard(barcode);
    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String errorMessage) {

    }

    @Override
    public void onCameraPermissionDenied() {

    }
}
