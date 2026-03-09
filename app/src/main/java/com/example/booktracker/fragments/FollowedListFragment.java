package com.example.booktracker.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booktracker.Constants;
import com.example.booktracker.Adapters.FollowedListAdapter;
import com.example.booktracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class FollowedListFragment extends Fragment {

    private RecyclerView followedRecyclerView;
    private FollowedListAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.followed_list_fragment, container, false);

        followedRecyclerView = view.findViewById(R.id.followed_list_view);
        followedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get userId: Priority order -> Fragment argument > SharedPreferences > CurrentUser
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", 0);
        userId = sharedPreferences.getString("userId", Objects.requireNonNull(mAuth.getCurrentUser()).getUid());

        if (getArguments() != null && getArguments().containsKey("userId")) {
            userId = getArguments().getString("userId");
        }

        // Load followed users list for the given user ID
        loadFollowedUsers();

        // Back button functionality
        view.findViewById(R.id.back_button).setOnClickListener(v -> goBackToUserFragment());

        return view;
    }

    private void goBackToUserFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack();
    }

    private void loadFollowedUsers() {
        if (userId == null || userId.isEmpty()) {
            Log.e("FollowedListFragment", "Invalid user ID.");
            return;
        }

        db.collection("users").document(userId).collection("followed")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Constants.Followed.clear();

                        if (task.getResult().isEmpty()) {
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                            Toast.makeText(getActivity(), "This user is not following anyone.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Constants.Followed.add(document.getId());
                        }

                        // Set or update the adapter
                        if (adapter == null) {
                            adapter = new FollowedListAdapter(getContext(), R.layout.followed_user_item, Constants.Followed);
                            followedRecyclerView.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.w("FollowedListFragment", "Error fetching followed users", task.getException());
                        Toast.makeText(getActivity(), "Failed to load followed users.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
