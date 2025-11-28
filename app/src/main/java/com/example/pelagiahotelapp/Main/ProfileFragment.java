package com.example.pelagiahotelapp.Main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.pelagiahotelapp.Login;
import com.example.pelagiahotelapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private View rootView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        initViews();
        loadUserData();
        setupClickListeners();

        return rootView;
    }

    private void initViews() {
        tvProfileName = rootView.findViewById(R.id.tv_profile_name);
        tvProfileEmail = rootView.findViewById(R.id.tv_profile_email);

        // Always set placeholder for profile image
        ImageView profileImage = rootView.findViewById(R.id.profile_image);
        profileImage.setImageResource(R.drawable.ic_profile_placeholder);
    }

    private void loadUserData() {
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            if (name != null && !name.isEmpty()) {
                tvProfileName.setText(name);
            } else {
                tvProfileName.setText("User");
            }

            if (email != null && !email.isEmpty()) {
                tvProfileEmail.setText(email);
            } else {
                tvProfileEmail.setText("No email provided");
            }
        }
    }

    private void setupClickListeners() {
        rootView.findViewById(R.id.option_change_name).setOnClickListener(v -> showChangeNameDialog());
        rootView.findViewById(R.id.option_change_password).setOnClickListener(v -> showChangePasswordDialog());
        rootView.findViewById(R.id.option_terms).setOnClickListener(v -> showTermsDialog());
        rootView.findViewById(R.id.option_help).setOnClickListener(v -> showHelpDialog());
        rootView.findViewById(R.id.btn_logout).setOnClickListener(v -> showLogoutDialog());
    }

    private void showChangeNameDialog() {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Change Name");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_name, null);
        EditText etNewName = dialogView.findViewById(R.id.et_new_name);
        etNewName.setText(tvProfileName.getText().toString());

        builder.setView(dialogView);
        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = etNewName.getText().toString().trim();
            if (!newName.isEmpty()) {
                updateUserName(newName);
            } else {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showChangePasswordDialog() {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Change Password");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        EditText etConfirmPassword = dialogView.findViewById(R.id.et_confirm_password);

        builder.setView(dialogView);
        builder.setPositiveButton("Change Password", (dialog, which) -> {
            String newPassword = etNewPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            if (validatePasswordInput(newPassword, confirmPassword)) {
                changePassword(newPassword);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean validatePasswordInput(String newPass, String confirm) {
        if (newPass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPass.equals(confirm)) {
            Toast.makeText(requireContext(), "New passwords don't match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPass.length() < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateUserName(String newName) {
        if (currentUser == null) return;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tvProfileName.setText(newName);
                        Toast.makeText(requireContext(), "Name updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update name: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void changePassword(String newPassword) {
        if (currentUser == null) return;

        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = "Failed to change password";
                        if (task.getException() != null) {
                            errorMessage += ": " + task.getException().getMessage();
                            // Handle re-authentication requirement
                            if (task.getException().getMessage().contains("requires recent authentication")) {
                                errorMessage += "\nPlease re-login to change password";
                            }
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        mAuth.signOut();
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to Login Activity
        Intent intent = new Intent(requireContext(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showTermsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Terms & Conditions");

        String termsText = "TERMS AND CONDITIONS\n\n" +
                "Last Updated: " + new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date()) + "\n\n" +
                "1. ACCEPTANCE OF TERMS\n" +
                "By accessing and using this application, you accept and agree to be bound by the terms and provision of this agreement.\n\n" +
                "2. USER RESPONSIBILITIES\n" +
                "You are responsible for maintaining the confidentiality of your account and password and for restricting access to your device.\n\n" +
                "3. PRIVACY POLICY\n" +
                "Your privacy is important to us. Please read our Privacy Policy which explains how we collect, use, and protect your personal information.\n\n" +
                "4. INTELLECTUAL PROPERTY\n" +
                "All content included in this app is the property of our company and protected by international copyright laws.\n\n" +
                "5. LIMITATION OF LIABILITY\n" +
                "We shall not be liable for any indirect, incidental, special, consequential or punitive damages resulting from your use of the app.\n\n" +
                "6. GOVERNING LAW\n" +
                "These terms shall be governed by and construed in accordance with the laws of your country/region.\n\n" +
                "For any questions about these Terms, please contact us at: legal@example.com";

        ScrollView scrollView = new ScrollView(requireContext());
        TextView textView = new TextView(requireContext());
        textView.setText(termsText);
        textView.setPadding(50, 30, 50, 30);
        textView.setTextSize(14);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        scrollView.addView(textView);

        builder.setView(scrollView);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Help & Support");

        String helpText = "HELP & SUPPORT\n\n" +
                "We're here to help you! Please check the following information:\n\n" +
                "📞 CONTACT SUPPORT\n" +
                "Email: support@example.com\n" +
                "Phone: +1 (555) 123-4567\n" +
                "Hours: Mon-Fri, 9AM-6PM EST\n\n" +
                "🔧 COMMON ISSUES\n" +
                "• Forgot Password: Use the 'Forgot Password' feature on login screen\n" +
                "• App Crashes: Try restarting the app or reinstalling\n" +
                "• Performance Issues: Clear app cache in settings\n\n" +
                "📱 APP FEATURES\n" +
                "• Change your name in Profile settings\n" +
                "• Update password regularly for security\n" +
                "• Contact support for any technical issues\n\n" +
                "🆘 EMERGENCY SUPPORT\n" +
                "For urgent issues affecting your account security, please contact us immediately at security@example.com";

        ScrollView scrollView = new ScrollView(requireContext());
        TextView textView = new TextView(requireContext());
        textView.setText(helpText);
        textView.setPadding(50, 30, 50, 30);
        textView.setTextSize(14);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        scrollView.addView(textView);

        builder.setView(scrollView);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}