package com.example.susanne.mycardsapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Susanne on 10-1-2018.
 */

public class User {
    public ArrayList<Card> cards;

    public User() {}

    public User(ArrayList<Card> nCards){
        this.cards = nCards;
    }

    public ArrayList<String> getCardNames(){
        List<Card> allCards = this.cards;
        ArrayList<String> allNames = new ArrayList<>() ;
        for (int i = 0; i < allCards.size(); i++){
            String name = allCards.get(i).getName();
            allNames.add(name);
        }
        return allNames;
    }

    public String getCardBarcode(String name){
        String barcode = "";
        for (int i = 0; i < cards.size(); i++){
            Card store = cards.get(i);
            if (store.getName().equals(name)){
                barcode = store.getBarcode();
            }
        }
        return barcode;
    }

    public void addCard(Card aCard){
        this.cards.add(aCard);
    }

    public boolean checkCard(String cardName){
        List<Card> allCards = this.cards;
        for (int i = 0; i < allCards.size(); i++){
            String name = allCards.get(i).getName();
            if (name.equals(cardName)){
                return true;
            }
        }
        return false;
    }

    public void updateCard(String cardName, String newBarcode){
        List<Card> allCards = this.cards;
        for (int i = 0; i < allCards.size(); i++){
            Card currentCard = allCards.get(i);
            String name = currentCard.getName();
            if (name.equals(cardName)){
                currentCard.barcode = newBarcode;
            }
        }
    }
}
