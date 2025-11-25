package com.example.pelagiahotelapp;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashScreen extends AppCompatActivity {
String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        new Handler().postDelayed(() -> {

            FirebaseAuth mAuth;
            mAuth = FirebaseAuth.getInstance();

            if (mAuth.getCurrentUser() != null) {
                // User is logged in, check their role
                String userId = mAuth.getCurrentUser().getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("users")
                        .document(userId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                                // User document exists, check role
                                String role = task.getResult().getString("role");
                                name=task.getResult().getString("name");
                                if ("admin".equals(role)) {
                                    // User is admin, go to AdminMainActivity
                                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                } else {
                                    // User is regular user, go to regular MainActivity
                                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                }
                                finish();
                            } else {
                                // User document doesn't exist or error, treat as regular user
                                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Error fetching user data, treat as regular user
                            Toast.makeText(this,"Logged in as "+name,LENGTH_LONG).show();
                            startActivity(new Intent(SplashScreen.this, MainActivity.class));
                            finish();
                        });
            } else {
                // User not logged in, go to login/guest activity
                startActivity(new Intent(SplashScreen.this, com.example.pelagiahotelapp.Login.class));
                finish();
            } }, 3000);

    }
}