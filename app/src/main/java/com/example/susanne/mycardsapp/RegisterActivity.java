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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    // Function for signing up a user, validates the email
    public void signUp(View view){
        EditText get_email = findViewById(R.id.email);
        EditText get_password = findViewById(R.id.password);
        EditText get_confirmation = findViewById(R.id.confirm);

        String email = get_email.getText().toString();
        String password = get_password.getText().toString();
        String confirmation = get_confirmation.getText().toString();

        // Check if email and password aren't blank and checks if password is longer than six characters
        if (email.equals("")){
            Toast.makeText(this, "Voer uw e-mail in", Toast.LENGTH_SHORT).show();
        }
        else if (password.equals("")){
            Toast.makeText(this, "Voer een wachtwoord in", Toast.LENGTH_SHORT).show();
        }
        else if (password.length() < 6){
            Toast.makeText(this, "Wachtwoord moet langer zijn dan zes tekens", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(confirmation)){
            Toast.makeText(this, "De wachtwoorden moeten overeen komen", Toast.LENGTH_SHORT).show();
        }

        // Sign user up is everything was correct
        else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("create user", "createUserWithEmail:success");
                                addUserToDatabase();
                                Intent intent = new Intent(RegisterActivity.this, OverviewActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("create user", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, "Authenticatie mislukt",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void addUserToDatabase(){
        FirebaseUser user = mAuth.getCurrentUser();
        String id = user.getUid();
        User nUser = new User();

        databaseReference.child(id).setValue(id);
    }

    public void goToOverview(View view) {
        Intent intent = new Intent(this, OverviewActivity.class);
        startActivity(intent);
    }
}
