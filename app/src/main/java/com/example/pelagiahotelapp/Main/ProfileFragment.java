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

        String termsText = "Terms & Conditions\n" +
                "These terms and conditions apply to the InnovaNexus app (hereby referred to as \"Application\") for mobile devices that was created by Ch Muhammad Dawood (hereby referred to as \"Service Provider\") as a Free service.\n" +
                "\n" +
                "\n" +
                "Upon downloading or utilizing the Application, you are automatically agreeing to the following terms. It is strongly advised that you thoroughly read and understand these terms prior to using the Application.\n" +
                "\n" +
                "\n" +
                "Unauthorized copying, modification of the Application, any part of the Application, or our trademarks is strictly prohibited. Any attempts to extract the source code of the Application, translate the Application into other languages, or create derivative versions are not permitted. All trademarks, copyrights, database rights, and other intellectual property rights related to the Application remain the property of the Service Provider.\n" +
                "\n" +
                "\n" +
                "The Service Provider is dedicated to ensuring that the Application is as beneficial and efficient as possible. As such, they reserve the right to modify the Application or charge for their services at any time and for any reason. The Service Provider assures you that any charges for the Application or its services will be clearly communicated to you.\n" +
                "\n" +
                "\n" +
                "The Application stores and processes personal data that you have provided to the Service Provider in order to provide the Service. It is your responsibility to maintain the security of your phone and access to the Application. The Service Provider strongly advise against jailbreaking or rooting your phone, which involves removing software restrictions and limitations imposed by the official operating system of your device. Such actions could expose your phone to malware, viruses, malicious programs, compromise your phone's security features, and may result in the Application not functioning correctly or at all.\n" +
                "\n" +
                "Please note that the Application utilizes third-party services that have their own Terms and Conditions. Below are the links to the Terms and Conditions of the third-party service providers used by the Application:\n" +
                "\n" +
                "Google Play Services\n" +
                "\n" +
                "Please be aware that the Service Provider does not assume responsibility for certain aspects. Some functions of the Application require an active internet connection, which can be Wi-Fi or provided by your mobile network provider. The Service Provider cannot be held responsible if the Application does not function at full capacity due to lack of access to Wi-Fi or if you have exhausted your data allowance.\n" +
                "\n" +
                "\n" +
                "If you are using the application outside of a Wi-Fi area, please be aware that your mobile network provider's agreement terms still apply. Consequently, you may incur charges from your mobile provider for data usage during the connection to the application, or other third-party charges. By using the application, you accept responsibility for any such charges, including roaming data charges if you use the application outside of your home territory (i.e., region or country) without disabling data roaming. If you are not the bill payer for the device on which you are using the application, they assume that you have obtained permission from the bill payer.\n" +
                "\n" +
                "\n" +
                "Similarly, the Service Provider cannot always assume responsibility for your usage of the application. For instance, it is your responsibility to ensure that your device remains charged. If your device runs out of battery and you are unable to access the Service, the Service Provider cannot be held responsible.\n" +
                "\n" +
                "\n" +
                "In terms of the Service Provider's responsibility for your use of the application, it is important to note that while they strive to ensure that it is updated and accurate at all times, they do rely on third parties to provide information to them so that they can make it available to you. The Service Provider accepts no liability for any loss, direct or indirect, that you experience as a result of relying entirely on this functionality of the application.\n" +
                "\n" +
                "\n" +
                "The Service Provider may wish to update the application at some point. The application is currently available as per the requirements for the operating system (and for any additional systems they decide to extend the availability of the application to) may change, and you will need to download the updates if you want to continue using the application. The Service Provider does not guarantee that it will always update the application so that it is relevant to you and/or compatible with the particular operating system version installed on your device. However, you agree to always accept updates to the application when offered to you. The Service Provider may also wish to cease providing the application and may terminate its use at any time without providing termination notice to you. Unless they inform you otherwise, upon any termination, (a) the rights and licenses granted to you in these terms will end; (b) you must cease using the application, and (if necessary) delete it from your device.\n" +
                "\n" +
                "\n" +
                "Changes to These Terms and Conditions\n" +
                "The Service Provider may periodically update their Terms and Conditions. Therefore, you are advised to review this page regularly for any changes. The Service Provider will notify you of any changes by posting the new Terms and Conditions on this page.\n" +
                "\n" +
                "\n" +
                "These terms and conditions are effective as of 2025-11-30\n" +
                "\n" +
                "\n" +
                "Contact Us\n" +
                "If you have any questions or suggestions about the Terms and Conditions, please do not hesitate to contact the Service Provider at chdawood.zulfiqar@gmail.com.";

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