package com.example.pelagiahotelapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Import ContextCompat for color
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    EditText inputEmail, inputPassword;
    Button btnLogin;
    TextView signupText;
    FirebaseAuth mAuth;

    private FirebaseFirestore db;

    // Store the original button text and background color (or rely on XML default)
    private CharSequence originalBtnText;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();

        // Capture original button text after init()
        originalBtnText = btnLogin.getText();

        signupText.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, SignUp.class));
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- Start of added UI feedback code ---
            setButtonProcessingState(true);
            // --- End of added UI feedback code ---

            signInUser(email, password);
        });
    }

    public void init()
    {
        // Removed: Toast.makeText(this, "In init", Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        signupText = findViewById(R.id.signupText);
    }

    // --- Added Helper Method for UI State Management ---
    private void setButtonProcessingState(boolean isProcessing) {
        if (isProcessing) {
            // Change text and disable button
            btnLogin.setText("Processing...");
            btnLogin.setEnabled(false);
            // Optionally change background color (requires a color resource, assuming R.color.colorPrimary or similar exists)
            // If you don't have a specific color, you can skip this part, but it's good practice for feedback.
            // Example: btnLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));
        } else {
            // Restore original text and enable button
            btnLogin.setText(originalBtnText);
            btnLogin.setEnabled(true);
            // Optionally restore background color to its original state (if changed in 'isProcessing' block)
            // Example: btnLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
    }
    // --- End of Added Helper Method ---


    public void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String userId = user.getUid();

                        db.collection("users").document(userId).get()
                                .addOnCompleteListener(dbTask -> {
                                    // --- Restore button state after database check ---
                                    setButtonProcessingState(false);
                                    // ---------------------------------------------------
                                    if (dbTask.isSuccessful()) {
                                        DocumentSnapshot document = dbTask.getResult();

                                        if (document.exists()) {
                                            String role = document.getString("role");

                                            if ("admin".equals(role)) {
                                                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(Login.this, MainActivity.class));
                                                finish();
                                            } else {
                                                mAuth.signOut(); // Sign out the authenticated user immediately
                                                Toast.makeText(this, "Access Denied: You must use the Admin portal to sign in.", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            mAuth.signOut();
                                            Toast.makeText(this, "Login failed: User profile data missing.", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        mAuth.signOut();
                                        Toast.makeText(this, "Login failed: Database error occurred.", Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        // --- Restore button state on Firebase authentication failure ---
                        setButtonProcessingState(false);
                        // -----------------------------------------------------------------
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                        Toast.makeText(this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}