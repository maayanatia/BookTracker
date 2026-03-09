package com.example.booktracker.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.booktracker.R;

public class ChangePasswordFragment extends Fragment {

    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;
    private Button changePasswordButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        oldPasswordEditText = view.findViewById(R.id.oldPasswordEditText);
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        confirmNewPasswordEditText = view.findViewById(R.id.confirmNewPasswordEditText);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        return view;
    }

    private void changePassword() {
        String oldPassword = oldPasswordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(oldPassword)) {
            oldPasswordEditText.setError("Old password is required");
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError("New password is required");
            return;
        }

        if (TextUtils.isEmpty(confirmNewPassword)) {
            confirmNewPasswordEditText.setError("Confirm new password is required");
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            confirmNewPasswordEditText.setError("Passwords do not match");
            return;
        }

        // Perform the password change operation (this is a placeholder logic)
        boolean isPasswordChanged = performPasswordChange(oldPassword, newPassword);

        if (isPasswordChanged) {
            Toast.makeText(getActivity(), "Password changed successfully", Toast.LENGTH_SHORT).show();
            // Navigate back or perform other actions as needed
            getActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(getActivity(), "Failed to change password", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean performPasswordChange(String oldPassword, String newPassword) {
        // Placeholder logic for changing password
        // Replace this with actual password change logic (e.g., API call)
        // For demonstration, we'll just return true to indicate success
        return true;
    }
}
