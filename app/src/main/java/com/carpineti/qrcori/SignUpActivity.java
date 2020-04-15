package com.carpineti.qrcori;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        EditText emailField = findViewById(R.id.email);
        EditText firstnameField = findViewById(R.id.firstname);
        EditText lastnameField = findViewById(R.id.lastname);
        EditText passwordField = findViewById(R.id.password);

        Button signUpButton = findViewById(R.id.sign_up_button);

        signUpButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String firstName = firstnameField.getText().toString().trim();
            String lastName = lastnameField.getText().toString().trim();
            String completeName = firstName + " " + lastName;
            String password = passwordField.getText().toString().trim();

            if (TextUtils.isEmpty(email)){
                Toast.makeText(getApplicationContext(), "Email is empty", Toast.LENGTH_LONG).show();
                return;
            }

            if (!isValidEmail(email)){
                Toast.makeText(getApplicationContext(), "The e-mail address you entered is not valid",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)){
                Toast.makeText(getApplicationContext(), "First or Last Name are empty",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (password.length() < 8){
                Toast.makeText(getApplicationContext(), "Password too short. At least 8 digits needed",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (password.contains(" ")){
                Toast.makeText(getApplicationContext(), "Password cannot contain whitespaces",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // register a user, also asking for email verification
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignUpActivity.this, task -> {
                        if (task.isSuccessful()) {
                            // Sign up success
                            Log.d("SUCCESS", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(SignUpActivity.this,
                                    task1 -> {
                                        if (task1.isSuccessful()){
                                            Toast.makeText(getApplicationContext(),
                                                    "Verification e-mail sent! Click on the link inside it",
                                                    Toast.LENGTH_LONG).show();
                                            createUserDbData(user, completeName);
                                            FirebaseAuth.getInstance().signOut();
                                            finish();
                                        }
                                        else {
                                            Log.e("ERROR", "sendEmailVerification:failure", task1.getException());
                                            Toast.makeText(SignUpActivity.this,
                                                    "Failed to send verification email",
                                                    Toast.LENGTH_LONG).show();
                                            FirebaseAuth.getInstance().signOut();
                                            finish();
                                        }
                                    });
                        } else {
                            // If sign up fails, display a message to the user.
                            Exception e = task.getException();
                            Log.w("ERROR", "createUserWithEmail:failure", e);
                            if (e instanceof FirebaseAuthUserCollisionException){
                                Toast.makeText(SignUpActivity.this, "Email address already in use",
                                        Toast.LENGTH_LONG).show();
                                finish();
                            }
                            else {
                                Toast.makeText(SignUpActivity.this, "Registration failed",
                                        Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }
                    });
        });
    }

    private boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void createUserDbData(FirebaseUser user, String completeName){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users_info");
        ref.child(user.getUid()).setValue(new User(user.getEmail(), completeName));
    }

}
