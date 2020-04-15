package com.carpineti.qrcori;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    // needed variables
    private static final int GOOGLE_SIGN_IN = 5555;
    private static final String[] FB_PERMISSIONS = {"email", "public_profile"};

    // auth stuff
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CallbackManager fbCallbackManager;

    // UI components
    private Button stdLoginButton;
    private EditText email;
    private EditText password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).hide();

        // standard login logic
        email = findViewById(R.id.email_edittext);
        password = findViewById(R.id.password_edittext);

        stdLoginButton = findViewById(R.id.stdLoginButton);
        stdLoginButton.setOnClickListener(v -> {
            String emailString = email.getText().toString().trim();
            String passwordString = password.getText().toString().trim();

            if (TextUtils.isEmpty(emailString)){
                Toast.makeText(getApplicationContext(), "Empty email", Toast.LENGTH_LONG).show();
                return;
            }

            if (TextUtils.isEmpty(passwordString)){
                Toast.makeText(getApplicationContext(), "Empty password", Toast.LENGTH_LONG).show();
                return;
            }

            if (!isValidEmail(emailString)){
                Toast.makeText(getApplicationContext(), "The e-mail address you entered is not valid",
                                    Toast.LENGTH_LONG).show();
                return;
            }

            if (passwordString.contains(" ")){
                Toast.makeText(getApplicationContext(), "Password cannot contain whitespaces",
                        Toast.LENGTH_LONG).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(emailString, passwordString)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d("SUCCESS", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user.isEmailVerified()){
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(getApplicationContext(), "Please verify your email address",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("ERROR", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });


        // Google related stuff
        // request account info
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // setting Sign In intent when pressing google sign in button
        SignInButton googleSignInButton = findViewById(R.id.google_sign_in_button);
        googleSignInButton.setSize(SignInButton.SIZE_WIDE);
        findViewById(R.id.google_sign_in_button).setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN);

        });

        // Facebook related stuff
        // request account info
        LoginButton fbLoginButton = findViewById(R.id.fb_login_button);
        fbLoginButton.setPermissions(Arrays.asList(FB_PERMISSIONS));

        // Callback registration
        fbCallbackManager = CallbackManager.Factory.create();
        fbLoginButton.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("facebookLogin", "FB Login Success" + loginResult.getAccessToken());
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Operation cancelled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // registering an account from scratch
        final TextView signUpLabel = findViewById(R.id.signup_label);
        signUpLabel.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);
        });
    }

    // handle the step from fb login to firebase auth
    private void firebaseAuthWithFacebook(AccessToken accessToken) {
        Log.d("firebaseFacebook", "firebaseAuthWithFacebook token:" + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()){
                Log.d("firebaseFacebook", "signInWithFacebookCredential:success");
                // Write account info to Firebase DB
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Profile profile = Profile.getCurrentProfile();
                createUserDbData(user, profile.getName());
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                Exception e = task.getException();
                Log.d("firebaseFacebook", "signInWithFacebookCredential:failure", e);
                if (e instanceof FirebaseAuthUserCollisionException){
                    Toast.makeText(LoginActivity.this, "The Fb-related email address is already in use",
                                                Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Fb auth failed", Toast.LENGTH_LONG).show();
                }
                // the following line is needed to log out with FB in the case Firebase returns an error
                LoginManager.getInstance().logOut();
            }
        });
    }

    // handle the step from google sign in to firebase auth
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("firebaseGoogle", "firebaseAuthWithGoogle id:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success
                Log.d("firebaseGoogle", "signInWithGoogleCredential:success");
                // Write account info to Firebase DB
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                createUserDbData(user, acct.getDisplayName());
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                // If sign in fails, display a message to the user.
                Log.d("firebaseGoogle", "signInWithGoogleCredential:failure", task.getException());

            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null){
                    // firebase request
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e){
                e.printStackTrace();
            }
        }
        else {
            // Pass the activity result back to the Facebook SDK
            fbCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void createUserDbData(FirebaseUser user, String completeName){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users_info");
        ref.child(user.getUid()).setValue(new User(user.getEmail(), completeName));
    }

    private boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
