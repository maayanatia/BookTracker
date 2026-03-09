package com.example.booktracker.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.booktracker.Activities.LoginActivity;
import com.example.booktracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {


    private Switch notificationsSwitch;
    private Button changePasswordButton;
    private Button logoutButton;
    private TextView emailTextView;
    private static final String TAG = "SettingsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_main, container, false);

        notificationsSwitch = view.findViewById(R.id.notificationsSwitch);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        emailTextView = view.findViewById(R.id.email);

        Log.d(TAG, "notificationsSwitch: " + (notificationsSwitch != null));
        Log.d(TAG, "changePasswordButton: " + (changePasswordButton != null));
        Log.d(TAG, "logoutButton: " + (logoutButton != null));

        // Fetch and display user email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            emailTextView.setText(email);
        }

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleNotifications(isChecked));
        changePasswordButton.setOnClickListener(v -> changePassword());
        logoutButton.setOnClickListener(v -> logout());

        return view;
    }

    private void toggleNotifications(boolean isChecked) {
        Log.d(TAG, "toggleNotifications called with isChecked: " + isChecked);
        requireContext();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notifications", isChecked);
        editor.apply();
    }

    private void changePassword() {
        Log.d(TAG, "changePassword called");
        Toast.makeText(requireContext(), "Change Password clicked", Toast.LENGTH_SHORT).show();
        // Implement change password logic
    }

    private void logout() {
        Log.d(TAG, "logout called");
        requireContext();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
