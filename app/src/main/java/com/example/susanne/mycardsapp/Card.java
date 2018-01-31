package com.example.susanne.mycardsapp;


/**
 * The Card class defines a specific card. The card class contains the store name, the barcode of
 * the card and whether a card is the user's favorite.
 * The only method of this class is setFavorite, this will update the favorite boolean of the card.
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

    public void setFavorite(){
        this.favorite = !this.favorite;
    }

}
