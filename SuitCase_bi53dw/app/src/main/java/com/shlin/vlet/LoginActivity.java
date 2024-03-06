package com.shlin.vlet;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

        // Constants for intent keys
        public static final String EXTRA_EMAIL = "extra_email";
        public static final String EXTRA_PASSWORD = "extra_password";

        Button  loginButton;
        FirebaseAuth firebaseAuth;

        TextView registerButton,tvForgotPass;

    @Override
        public void onStart() {
            super.onStart();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                // User is already authenticated, navigate to the main activity
                startMainActivity();
            }
        }

        @SuppressLint("MissingInflatedId")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);
            firebaseAuth = FirebaseAuth.getInstance();

            loginButton = findViewById(R.id.Login_button);
            registerButton = findViewById(R.id.register_Button);
            EditText editTextEmail = findViewById(R.id.login_email);
            EditText editTextPassword = findViewById(R.id.login_password);
            tvForgotPass = findViewById(R.id.forgotpassword);

            ImageView imageView = findViewById(R.id.login_image);
            imageView.setImageResource(R.drawable.picture);
            loginButton.setOnClickListener(v -> {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Disable the button during authentication
                loginButton.setEnabled(false);

                // Perform login logic in a separate method
                performLogin(email, password);
            });

            tvForgotPass.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this,ForgotPasswordActivity.class);
                startActivity(intent);
            });


            registerButton.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

        });
        }

        private void performLogin(String email, String password) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Re-enable the login button
                            loginButton.setEnabled(true);
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "LoginActivity Successful", Toast.LENGTH_SHORT).show();
                                startMainActivity();
                            } else {
                                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }



        private void startMainActivity() {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
