package com.example.googlesigninapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class HomeActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView welcomeText = findViewById(R.id.welcome_text);
        Button logoutBtn = findViewById(R.id.logout_button);

        String name = getIntent().getStringExtra("name");
        welcomeText.setText("Welcome, " + name + "!");

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        logoutBtn.setOnClickListener(view -> {
            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                    task -> {
                        startActivity(new Intent(HomeActivity.this, MainActivity.class));
                        finish();
                    });
        });
    }
}
