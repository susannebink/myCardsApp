package com.example.susanne.mycardsapp;


/**
 * Created by Susanne on 10-1-2018.
 */

public class Card {
    public String storeName;
    public String barcode;

    public Card() {}

    public Card(String aName, String aBarcode){
        this.storeName = aName;
        this.barcode = aBarcode;
    }

    public String getName(){
        return this.storeName;
    }
}
