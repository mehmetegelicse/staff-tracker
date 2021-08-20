package com.eralpsoftware.stafftracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.stafftracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    public FirebaseUser mUser;
    private FirebaseFirestore db;
    EditText email, password;
    boolean success = false;
    Button login, forgotPasswordButton;
    ProgressBar progressBar;
    boolean isEmailValid, isPasswordValid;
    TextInputLayout emailError, passError;
    private String m_Text = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        email = findViewById(R.id.email);
        password =findViewById(R.id.password);
        login = findViewById(R.id.login);
        progressBar = findViewById(R.id.pbar);
        emailError =findViewById(R.id.emailError);
        passError =  findViewById(R.id.passError);
        forgotPasswordButton = findViewById(R.id.forgotButton);


        if(mAuth.getCurrentUser() != null){
            goMainActivity();
        }
        if(success){
            progressBar.setVisibility(View.GONE);
            login.setVisibility(View.VISIBLE);
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                SetValidation();


            }

        });
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(getString(R.string.forgot_password_alert));

// Set up the input
                final EditText input = new EditText(LoginActivity.this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton(getString(R.string.send), (dialog, which) -> {
                    forgotPassword(input.getText().toString());
                    dialog.dismiss();
                });
                builder.setNegativeButton(getString(R.string.cancel_text), (dialog, which) -> dialog.cancel());

                builder.show();
            }
        });


    }
    void login(String email, String password)
      {  mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                private static final String TAG = "1";

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCustomToken:success");

                        FirebaseUser mUser = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, getString(R.string.login_success),
                                Toast.LENGTH_SHORT).show();

                        db.collection("user").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                success = true;

                                goMainActivity();
                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {

                                success = false;
                            }
                        });

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        login.setVisibility(View.VISIBLE);

                        // updateUI(null);

                    }
                }
            });}


    void createUser(){
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email );
        userData.put("name", "mehmet");
        userData.put("id" , mUser.getUid());

        db.collection("user").add(userData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(LoginActivity.this, "Authentication fail." + e,
                        Toast.LENGTH_SHORT).show();

            }
        });
    }
    public void SetValidation() {
        // Check for a valid email address.
        if (email.getText().toString().isEmpty()) {
            emailError.setError("email boş bırakılamaz.");
            isEmailValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            emailError.setError("geçersiz e-mail");
            isEmailValid = false;
        } else  {
            isEmailValid = true;
            emailError.setErrorEnabled(false);
        }

        // Check for a valid password.
        if (password.getText().toString().isEmpty()) {
            passError.setError("parola boş bırakılamaz");
            isPasswordValid = false;
        } else if (password.getText().length() < 6) {
            passError.setError("geçersiz parola");
            isPasswordValid = false;
        } else  {
            isPasswordValid = true;
            passError.setErrorEnabled(false);
        }

        if (isEmailValid && isPasswordValid) {
            login(email.getText().toString(), password.getText().toString() );
            progressBar.setVisibility(View.VISIBLE);
            login.setVisibility(View.GONE);
        }

    }
    void goMainActivity(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
    void forgotPassword(String email){
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Parolanı sıfırlamak için " + email + " adresini kontrol et. ", Toast.LENGTH_LONG).show();
                    }
                });
    }

}