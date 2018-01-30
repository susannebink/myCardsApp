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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    EditText get_email;
    EditText get_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        get_email = findViewById(R.id.email);
        get_password = findViewById(R.id.password);
        setListener();
    }

    /**
     * Check if user is already signed in.
      */
    public void setListener(){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    goToOverview();
                }
                else {
                    Log.d("signed out", "OnAuthStateChanged:signed_out" );
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
    }

    /**
     * Function for the log in of a user, validates email and password.
      */
    public void signIn(View view) {
        String email = get_email.getText().toString();
        String password = get_password.getText().toString();

        if(checkEditText(email, password)){
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("sign in success", "signInWithEmail:success");
                                goToOverview();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("sign in fail", "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Ongeldige login",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
    * Function to check if the filled in email and password aren't empty.
     */
    public boolean checkEditText(String email, String password){
        if (email.equals("")){
            Toast.makeText(this, "Voer uw e-mail in", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (password.equals("")) {
            Toast.makeText(this, "Voer uw wachtwoord in", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
    * On click function for an intent to go to the register activity.
     */
    public void goToRegister(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
    * Function for an intent to go to the overview activity.
     */
    public void goToOverview() {
        Intent intent = new Intent(this, OverviewActivity.class);
        startActivity(intent);
        finish();
    }
}
