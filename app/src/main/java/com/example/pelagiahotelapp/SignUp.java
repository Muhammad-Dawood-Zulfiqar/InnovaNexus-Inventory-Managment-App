package com.example.pelagiahotelapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    private EditText inputName, inputEmail, inputPassword, confirmPassword;
    private Button signupBtn; // Note: XML ID is 'loginBtn'
    private TextView loginText;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        signupBtn.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            String confirmPswd = confirmPassword.getText().toString().trim();

            // --- Validation Checks ---
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPswd.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPswd)) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_LONG).show();
                return;
            }

            // --- 3. Firebase Registration ---
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveNewUserData(user, name);
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed.";
                            Toast.makeText(this, "Sign Up Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        loginText.setOnClickListener(v -> {
            startActivity(new Intent(SignUp.this, com.example.pelagiahotelapp.Login.class));
            finish();
        });
    }
    private void saveNewUserData(FirebaseUser user, String name) {
        String userId = user.getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", user.getEmail());
        userData.put("role", "admin"); // The default role

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUp.this, com.example.pelagiahotelapp.MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving profile data. Please try again.", Toast.LENGTH_LONG).show();
                });
    }
    public void init()
    {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Link UI Elements to XML IDs 🔗
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        signupBtn = findViewById(R.id.btnSignup); // This acts as the sign-up button
        loginText = findViewById(R.id.loginText);
    }
}