package com.example.booktracker.fragments;

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

import com.example.booktracker.Adapters.FollowedListAdapter;
import com.example.booktracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FollowersListFragment extends Fragment {

    private RecyclerView followersRecyclerView;
    private FollowedListAdapter adapter;
    private List<String> followersList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.followers_list_fragment, container, false);

        followersRecyclerView = view.findViewById(R.id.followers_list_view);
        followersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        followersList = new ArrayList<>();

        // Get user ID (either current user or another user)
        if (getArguments() != null && getArguments().containsKey("userId")) {
            userId = getArguments().getString("userId");
        } else {
            userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        }

        loadFollowers(userId);

        view.findViewById(R.id.back_button).setOnClickListener(v -> goBackToUserFragment());

        return view;
    }

    private void goBackToUserFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack();
    }

    private void loadFollowers(String userId) {
        db.collection("users").document(userId).collection("followers")
                .get(Source.SERVER)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followersList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String followerId = document.getId();
                            followersList.add(followerId);
                        }

                        if (!followersList.isEmpty()) {
                            adapter = new FollowedListAdapter(getContext(), R.layout.followed_user_item, followersList);
                            followersRecyclerView.setAdapter(adapter);
                        } else {
                            Toast.makeText(getActivity(), "No followers found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w("FollowersListFragment", "Error getting followers", task.getException());
                    }
                });
    }
}
