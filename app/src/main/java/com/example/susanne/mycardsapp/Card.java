package com.example.susanne.mycardsapp;


/**
 * Created by Susanne on 10-1-2018.
 */

public class Card {
    public String storeName;
    public String barcode;
    public Boolean favorite;

    public Card() {}

    public Card(String aName, String aBarcode){
        this.storeName = aName;
        this.barcode = aBarcode;
        this.favorite = false;
    }

    public String getName(){
        return this.storeName;
    }

    public String getBarcode(){
        return this.barcode;
    }

    public void setFavorite(){
        if (this.favorite){
            this.favorite = false;
        }
        else {
            this.favorite = true;
        }

    }

}
