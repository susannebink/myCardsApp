package com.example.susanne.mycardsapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Susanne on 10-1-2018.
 */

public class User {
    public ArrayList<Card> cards;

    public User() {}

    public void User(){
        this.cards = new ArrayList<>();
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

    public void addCard(Card aCard){
        cards.add(aCard);
    }


}
