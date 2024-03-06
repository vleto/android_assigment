package com.shlin.vlet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText forgotPassEmail;
    Button requestBtn;
    TextView goback;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        firebaseAuth = FirebaseAuth.getInstance();

        forgotPassEmail = findViewById(R.id.forgotpass_email);
        requestBtn = findViewById(R.id.request_btn);
        progressBar = findViewById(R.id.progressbar);
        goback = findViewById(R.id.goback);

        forgotPassEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        requestBtn.setOnClickListener(v -> {
            firebaseAuth.sendPasswordResetEmail(forgotPassEmail.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ForgotPasswordActivity.this, "Check Your Email Inbox to Reset Password", Toast.LENGTH_SHORT).show();
                            }else {
                                String error = task.getException().getMessage();
                                requestBtn.setEnabled(true);
                                forgotPassEmail.setError(error);
                            }
                        }
                    });
        });

        goback.setOnClickListener(v -> {
            Intent gobacklogin = new Intent(ForgotPasswordActivity.this,LoginActivity.class);
            startActivity(gobacklogin);
            finish();
        });

    }

    private void checkInputs() {
        String email = forgotPassEmail.getText().toString();
        if (TextUtils.isEmpty(email)){
            requestBtn.setEnabled(false);
        }else {
            requestBtn.setEnabled(true);
        }
    }
}