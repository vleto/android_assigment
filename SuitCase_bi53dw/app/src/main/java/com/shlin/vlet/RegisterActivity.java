package com.shlin.vlet;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    EditText editTextname,editTextEmail, editTextPassword;
    EditText editTextConfirmPass;
    Button RegisterBtn;
    TextView tvSigIn;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseDatabase firebaseDatabase;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        RegisterBtn = findViewById(R.id.Register_button);
        editTextname = findViewById(R.id.register_name);
        editTextEmail = findViewById(R.id.register_email);
        editTextPassword = findViewById(R.id.register_pass);
        editTextConfirmPass = findViewById(R.id.register_confirm_pass);
        progressBar = findViewById(R.id.progressbar);
        tvSigIn = findViewById(R.id.sign_in);
        ImageView imageView = findViewById(R.id.reg_img);
        imageView.setImageResource(R.drawable.picture);

        tvSigIn.setOnClickListener((View.OnClickListener) view -> {
            Intent goTologin = new Intent(RegisterActivity.this,LoginActivity.class);
            startActivity(goTologin);
        });

        RegisterBtn.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            createUser();
        });

    }

    private boolean isEmailValid(String email) {
        // Use a regular expression to check if the email is in a valid format
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private boolean isPasswordStrong(String password) {
        // Check if the password is strong enough
        // At least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(passwordPattern);
    }

    private void createUser() {
        String userName = editTextname.getText().toString();
        String userEmail = editTextEmail.getText().toString();
        String userPassword = editTextPassword.getText().toString();
        String userConfirmPassword = editTextConfirmPass.getText().toString();

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userPassword) || TextUtils.isEmpty(userConfirmPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailValid(userEmail)) {
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
            return;
        } else {
            editTextEmail.setError(null);
        }

        if (!isPasswordStrong(userPassword)) {
            // Set an error message for the password EditText
            editTextPassword.setError("Password should be strong (at least 8 characters with one uppercase letter, one lowercase letter, one digit, and one special character)");
            return;
        } else {
            // Clear the error message if the password is strong
            editTextPassword.setError(null);
        }

        if (!userPassword.equals(userConfirmPassword)) {
            // Set an error message for the confirm password EditText
            editTextConfirmPass.setError("Passwords do not match");
            return;
        } else {
            // Clear the error message if passwords match
            editTextPassword.setError(null);
            editTextConfirmPass.setError(null);
        }
        firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            UserModel userModel = new UserModel(userName, userEmail);
                            String uid = Objects.requireNonNull(task.getResult().getUser()).getUid();

                            // Store user data in Firestore
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            CollectionReference usersCollection = db.collection("USERS");
                            DocumentReference userDocument = usersCollection.document(uid);

                            userDocument.set(userModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

}
