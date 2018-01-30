package com.example.susanne.mycardsapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    EditText get_email;
    EditText get_password;
    EditText get_confirmation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        get_email = findViewById(R.id.email);
        get_password = findViewById(R.id.password);
        get_confirmation = findViewById(R.id.confirm);
    }

    /**
     * Function for signing up a user, validates the email and signs up the user if everything is
     * correct.
      */
    public void signUp(View view){
        String email = get_email.getText().toString();
        String password = get_password.getText().toString();
        String confirmation = get_confirmation.getText().toString();

        // Sign user up is everything was correct
        if(checkEditText(email, password, confirmation)) {
            FirebaseAuthentication(email, password);
        }
    }

    /**
     * Function for checking if all the editText fields are (correctly) filled in
      */
    public boolean checkEditText(String email, String password, String confirm){
        if (email.equals("")){
            Toast.makeText(this, "Voer uw e-mail in", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (password.equals("")){
            Toast.makeText(this, "Voer een wachtwoord in", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (password.length() < 6){
            Toast.makeText(this, "Wachtwoord moet langer zijn dan zes tekens", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (!password.equals(confirm)){
            Toast.makeText(this, "De wachtwoorden moeten overeen komen", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Function to sign up the user with Firebase.
      */
    public void FirebaseAuthentication(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            goToOverview();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Authenticatie mislukt",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Function that starts OverviewActivity.
     */
    public void goToOverview() {
        Intent intent = new Intent(this, OverviewActivity.class);
        startActivity(intent);
        finish();
    }
}
