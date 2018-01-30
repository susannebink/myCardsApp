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

        // Check permission for camera use, if permission not yet granted, ask permission.
        if (ActivityCompat.checkSelfPermission(AddCardActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddCardActivity.this,
                    new String[]{Manifest.permission.CAMERA}, myCameraRequestCode);
        }

    }

    /**
     * Get result from permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == myCameraRequestCode) {

            // If camera permission is granted, restart activity, now the camera can be used.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera toegang geaccepteerd", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(AddCardActivity.this, AddCardActivity.class);
                startActivity(intent);
                finish();

            // If camera permission is denied, go back to OverviewActivity.
            } else {
                Toast.makeText(this, "Camera toegang afgewezen", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(AddCardActivity.this, OverviewActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    /**
     * Go to the SaveCardActivity with the scanned barcode.
     */
    public void goToSaveCard(Barcode barcode) {
        Intent intent = new Intent(AddCardActivity.this, SaveCardActivity.class);
        intent.putExtra("barcode", barcode);
        startActivity(intent);
        finish();
    }

    /**
     * Result from the barcode scanner, start SaveCardActivity.
     */
    @Override
    public void onScanned(Barcode barcode) {
        Log.d("Barcode", barcode.rawValue);
        goToSaveCard(barcode);
    }

    /**
     * Do nothing if multiple barcodes are scanned.
     */
    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {
    }

    /**
     * Do nothing if bitmap is scanned.
     */
    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String errorMessage) {
        Toast.makeText(AddCardActivity.this, "Barcode kon niet gescand worden", Toast.LENGTH_SHORT).show();
    }

    /**
     * If camera permission was denied, go back to OverviewActivity.
     */
    @Override
    public void onCameraPermissionDenied() {
        Intent intent = new Intent(AddCardActivity.this, OverviewActivity.class);
        startActivity(intent);
        finish();
    }
}
