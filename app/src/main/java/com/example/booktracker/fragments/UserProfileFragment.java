package com.example.booktracker.fragments;

import static java.lang.String.format;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.booktracker.Constants;
import com.example.booktracker.R;
import com.example.booktracker.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserProfileFragment extends Fragment {

    private ImageView profileImageView, bookshelf;
    private SharedPreferences sharedPreferences;
    private TextView usernameTextView;
    private TextView followersCountTextView;
    private TextView followedCountTextView;
    private TextView booksReadCountTextView;
    private Button followButton, backButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String currentUserId;
    private String viewedUserId;

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.other_user_fragment, container, false);

        profileImageView = view.findViewById(R.id.profileImg);
        usernameTextView = view.findViewById(R.id.username);
        followersCountTextView = view.findViewById(R.id.followerCount);
        followedCountTextView = view.findViewById(R.id.followedCount);
        booksReadCountTextView = view.findViewById(R.id.bookReadCount);
        followButton = view.findViewById(R.id.followUnfollow);
        backButton = view.findViewById(R.id.back_button);
        bookshelf = view.findViewById(R.id.bookshelf);
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", 0);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        backButton.setOnClickListener(v -> goBackToLastFragment());

        sharedPreferences.edit().putString("userId", viewedUserId).apply();
        bookshelf.setOnClickListener(v -> openBookshelfFragment(viewedUserId));

        followedCountTextView.setOnClickListener(v -> {
            FollowedListFragment followedListFragment = new FollowedListFragment();
            Bundle args = new Bundle();
            args.putString("userId", viewedUserId); // Pass the correct user ID
            followedListFragment.setArguments(args);
            loadFragment("FOLLOWED_LIST_FRAGMENT", followedListFragment);
        });

        followersCountTextView.setOnClickListener(v -> {
            FollowersListFragment followersListFragment = new FollowersListFragment();
            Bundle args = new Bundle();
            args.putString("userId", viewedUserId); // Pass userId to the fragment
            followersListFragment.setArguments(args);
            loadFragment("FOLLOWERS_LIST_FRAGMENT", followersListFragment);
        });

        Bundle args1 = getArguments();
        if (args1 != null) {
            String searchedUsername = args1.getString("username");
            if (searchedUsername != null && !searchedUsername.isEmpty()) {
                getUserData(searchedUsername, new UserCallback() {
                    @Override
                    public void onSuccess(User user, String documentId) {
                        if (user != null) {
                            viewedUserId = documentId;
                            loadUser(viewedUserId);
                            // Save viewedUserId to SharedPreferences
                            sharedPreferences.edit().putString("userId", viewedUserId).apply();
                            Log.d("UserProfileFragment", "Saved viewedUserId: " + viewedUserId);

                            // Now set bookshelf click listener
                            bookshelf.setOnClickListener(v -> openBookshelfFragment(viewedUserId));
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("UserProfileFragment", "Error fetching user data", e);
                    }
                });
            }
        }

        Bundle args = getArguments();
        if (args != null) {
            String searchedUsername = args.getString("username");
            if (searchedUsername != null && !searchedUsername.isEmpty()) {
                getUserData(searchedUsername, new UserCallback() {
                    @Override
                    public void onSuccess(User user, String documentId) {
                        if (user != null) {
                            db.collection("users")
                                    .document(currentUserId)
                                    .update("followedCount", Constants.Followed.size())
                                    .addOnSuccessListener(aVoid -> Log.d("UserProfileFragment", "Followed count updated"))
                                    .addOnFailureListener(e -> Log.e("UserProfileFragment", "Error updating followed count", e));

                            db.collection("users")
                                    .document(currentUserId)
                                    .update("followersCount", Constants.Followers.size())
                                    .addOnSuccessListener(aVoid -> Log.d("UserProfileFragment", "Followers count updated"))
                                    .addOnFailureListener(e -> Log.e("UserProfileFragment", "Error updating followers count", e));
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("UserProfileFragment", "Error fetching user data", e);
                    }
                });
            }
        }

        followButton.setOnClickListener(v -> {
            if (args != null) {
                String searchedUsername = args.getString("username");
                if (searchedUsername != null && !searchedUsername.isEmpty()) {
                    getUserData(searchedUsername, new UserCallback() {
                        @Override
                        public void onSuccess(User user, String documentId) {
                            if (user != null) {
                                viewedUserId = documentId;
                                FindUsernameByID(viewedUserId, new UsernameCallback() {
                                    @Override
                                    public String onSuccess(String ViewedUsername) {
                                        if (!Constants.Followed.contains(ViewedUsername)) {
                                            Constants.Followed.add(ViewedUsername);
                                            followButton.setText("Unfollow");

                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("username", ViewedUsername);  // Storing the username, not the userId

                                            // Add the username to the "followed" field in the current user document
                                            db.collection("users")
                                                    .document(currentUserId)
                                                    .collection("followed")
                                                    .document(ViewedUsername)  // Use the username instead of userId here
                                                    .set(userData)
                                                    .addOnSuccessListener(aVoid -> Log.d("UserProfileFragment", "User followed successfully"))
                                                    .addOnFailureListener(e -> Log.e("UserProfileFragment", "Error following user", e));

                                            // Add the current user's username to the "followers" field in the viewed user document
                                            FindUsernameByID(currentUserId, new UsernameCallback() {
                                                @Override
                                                public String onSuccess(String currentUsername) {
                                                    Map<String, Object> followerData = new HashMap<>();
                                                    followerData.put("username", currentUsername);

                                                    db.collection("users")
                                                            .document(viewedUserId)
                                                            .collection("followers")
                                                            .document(currentUsername)
                                                            .set(followerData)
                                                            .addOnSuccessListener(aVoid -> Log.d("UserProfileFragment", "Follower added successfully"))
                                                            .addOnFailureListener(e -> Log.e("UserProfileFragment", "Error adding follower", e));

                                                    return currentUsername;
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                    Log.e("UserProfileFragment", "Error fetching current username", e);
                                                }
                                            });

                                            // Update the followed count
                                            db.collection("users")
                                                    .document(currentUserId)
                                                    .update("followedCount", Constants.Followed.size())
                                                    .addOnSuccessListener(aVoid -> Log.d("UserProfileFragment", "Followed count updated"))
                                                    .addOnFailureListener(e -> Log.e("UserProfileFragment", "Error updating followed count", e));
                                        } else {
                                            Constants.Followed.remove(ViewedUsername);
                                            followButton.setText("Follow");

                                            // Remove the username from the "followed" field in the current user document
                                            db.collection("users")
                                                    .document(currentUserId)
                                                    .collection("followed")
                                                    .document(ViewedUsername)  // Use the username to remove
                                                    .delete()
                                                    .addOnSuccessListener(aVoid -> Log.d("UserProfileFragment", "User unfollowed successfully"))
                                                    .addOnFailureListener(e -> Log.e("UserProfileFragment", "Error unfollowing user", e));

                                            // Remove the current user's username from the "followers" field in the viewed user document
                                            FindUsernameByID(currentUserId, new UsernameCallback() {
                                                @Override
                                                public String onSuccess(String currentUsername) {
                                                    db.collection("users")
                                                            .document(viewedUserId)
                                                            .collection("followers")
                                                            .document(currentUsername)
                                                            .delete()
                                                            .addOnSuccessListener(aVoid -> Log.d("UserProfileFragment", "Follower removed successfully"))
                                                            .addOnFailureListener(e -> Log.e("UserProfileFragment", "Error removing follower", e));

                                                    return currentUsername;
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                    Log.e("UserProfileFragment", "Error fetching current username", e);
                                                }
                                            });

                                            // Update the followed count
                                            db.collection("users")
                                                    .document(currentUserId)
                                                    .update("followedCount", Constants.Followed.size())
                                                    .addOnSuccessListener(aVoid -> Log.d("UserProfileFragment", "Followed count updated"))
                                                    .addOnFailureListener(e -> Log.e("UserProfileFragment", "Error updating followed count", e));
                                        }
                                        return ViewedUsername;
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(getContext(), "Error fetching username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("UserProfileFragment", "Error fetching username", e);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("UserProfileFragment", "Error fetching user data", e);
                        }
                    });
                }
            }
        });

        return view;
    }

    private void getUserData(String username, final UserCallback callback) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);

                            String usernameFetched = document.getString("username");
                            String profileImageUrl = document.getString("profileImageUrl");
                            Long booksReadCountLong = document.getLong("booksReadCount");
                            int booksReadCount = (booksReadCountLong != null) ? booksReadCountLong.intValue() : 0;

                            List<String> followers = extractList(document.get("followers"));
                            List<String> followed = extractList(document.get("followed"));

                            int followersCount = followers.size();
                            int followedCount = followed.size();

                            User user = new User(usernameFetched, profileImageUrl, followersCount, followedCount, booksReadCount, followers, followed);
                            callback.onSuccess(user, document.getId());
                        } else {
                            callback.onFailure(new Exception("No such user"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    private List<String> extractList(Object obj) {
        if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            List<String> stringList = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof String) {
                    stringList.add((String) item);
                }
            }
            return stringList;
        }
        return new ArrayList<>();
    }

    private Object FindUsernameByID(String userId, UsernameCallback callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String username = document.getString("username");
                            if (username != null) {
                                callback.onSuccess(username);
                            } else {
                                callback.onFailure(new Exception("Username not found"));
                            }
                        } else {
                            callback.onFailure(new Exception("User not found"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
        return null;
    }

    private void updateFollowCounts() {
        db.collection("users").document(viewedUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int followersCount = documentSnapshot.contains("followers")
                                ? ((List<?>) Objects.requireNonNull(documentSnapshot.get("followers"))).size() : 0;
                        followersCountTextView.setText(MessageFormat.format("Followers: {0}", followersCount));
                    }
                });

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int followedCount = documentSnapshot.contains("followed")
                                ? ((List<?>) Objects.requireNonNull(documentSnapshot.get("followed"))).size() : 0;
                        followedCountTextView.setText(MessageFormat.format("Followed: {0}", followedCount));
                    }
                });
    }

    private void checkFollowStatus() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> followedList = extractList(documentSnapshot.get("followed"));

                        if (followedList.contains(FindUsernameByID(viewedUserId, new UsernameCallback() {
                            @Override
                            public String onSuccess(String username) {
                                return username;
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("UserProfileFragment", "Error checking follow status", e);
                            }
                        }))) {
                            followButton.setText("Unfollow");
                        } else {
                            followButton.setText("Follow");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("UserProfileFragment", "Error checking follow status", e));
    }

    private void goBackToLastFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.popBackStack();
    }

    public interface UserCallback {
        void onSuccess(User user, String documentId);
        void onFailure(Exception e);
    }

    public interface UsernameCallback {
        String onSuccess(String username);
        void onFailure(Exception e);
    }

    private void loadFragment(String tag, Fragment newFragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);

        if (existingFragment == null) {
            transaction.add(R.id.fragment_container, newFragment, tag);
        } else {
            transaction.show(existingFragment);
        }

        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment != null && currentFragment != existingFragment) {
            transaction.hide(currentFragment);
        }

        transaction.addToBackStack(tag); // Add to back stack
        transaction.commit();
    }

    private void openBookshelfFragment(String userId) {
        BookshelfFragment bookshelfFragment = new BookshelfFragment();

        sharedPreferences.edit().putString("userId", userId).apply();

        loadFragment("BOOKSHELF_FRAGMENT", new BookshelfFragment());
    }

    private void loadUser(String userId) {
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            if (document.exists()) {
                String usernameValue = document.getString("username");
                int followers = (document.getLong("followersCount")).intValue();
                int followed = (document.getLong("followedCount")).intValue();

                usernameTextView.setText(usernameValue);
                followersCountTextView.setText(format("%d", followers));
                followedCountTextView.setText(format("%d", followed));
            } else {
                Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
