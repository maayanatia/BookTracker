package com.example.booktracker.Activities;

import static androidx.core.app.PendingIntentCompat.getActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.booktracker.Constants;
import com.example.booktracker.R;
import com.example.booktracker.fragments.MainFragment;
import com.example.booktracker.fragments.SettingsFragment;
import com.example.booktracker.fragments.UserFragment;
import com.example.booktracker.fragments.search_fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("users")
                .document(userId)
                .update("booksRead" , Constants.ReadBooks.size())
                .addOnSuccessListener(null)
                .addOnFailureListener(null);

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
                Log.d("Notification Data", "Key: " + key + " Value: " + value);
            }
        }

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); // 💣 clear old fragments

                if (item.getItemId() == R.id.nav_home) {
                    loadFragment("HOME_FRAGMENT", new MainFragment(), false);
                } else if (item.getItemId() == R.id.nav_search) {
                    loadFragment("SEARCH_FRAGMENT", new search_fragment(), false);
                } else if (item.getItemId() == R.id.nav_user) {
                    loadFragment("USER_FRAGMENT", new UserFragment(), false);
                } else if (item.getItemId() == R.id.nav_settings) {
                    loadFragment("SETTINGS_FRAGMENT", new SettingsFragment(), false);
                }
                return true;
            }
        });




        loadFragment("HOME_FRAGMENT", new MainFragment(),false);


    }

    private Map<String, Fragment> fragmentMap = new HashMap<>();
    private String currentFragmentTag = null;

    private void loadFragment(String tag, Fragment newFragment, boolean forceUpdate) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Check if the fragment exists in the map
        Fragment fragment = fragmentMap.get(tag);

        if (fragment == null || forceUpdate) {
            // Add or replace the fragment if it's not in the map or update is forced
            if (fragment != null) {
                transaction.remove(fragment);
            }
            fragment = newFragment;
            fragmentMap.put(tag, fragment);
            transaction.add(R.id.fragment_container, fragment, tag);
        }

        // Hide the current fragment if there is one
        if (currentFragmentTag != null && !currentFragmentTag.equals(tag)) {
            Fragment currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag);
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
        }

        // Show the new fragment
        transaction.show(fragment);
        currentFragmentTag = tag;

        transaction.commit();
    }





    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
