package com.example.booktracker.fragments;

import static java.lang.String.*;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.booktracker.BookResponse;
import com.example.booktracker.Constants;
import com.example.booktracker.R;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserFragment extends Fragment {
    private TextView username;
    private TextView followersCount;
    private TextView followedCount;
    private TextView bookReadCount;
    private ImageView profileImg, bookshelf;
    private Button btnMarked, btnProgress;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        followersCount = view.findViewById(R.id.followerCount);
        followedCount = view.findViewById(R.id.followedCount);
        profileImg = view.findViewById(R.id.profileImg);
        bookshelf = view.findViewById(R.id.bookshelf);
        bookReadCount = view.findViewById(R.id.bookReadCount);
        username = view.findViewById(R.id.username);
        btnMarked = view.findViewById(R.id.btnMarked);
        btnProgress = view.findViewById(R.id.btnProgress);
        BottomNavigationView bottomNavigation = view.findViewById(R.id.bottom_navigation);
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", 0);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        followedCount.setOnClickListener(v -> {
            FollowedListFragment followedListFragment = new FollowedListFragment();
            Bundle args = new Bundle();
            args.putString("userId", userId); // Pass the correct user ID
            followedListFragment.setArguments(args);
            loadFragment("FOLLOWED_LIST_FRAGMENT", followedListFragment);
        });


        followersCount.setOnClickListener(v -> {
            FollowersListFragment followersListFragment = new FollowersListFragment();

            Bundle args = new Bundle();
            args.putString("userId", userId); // Pass userId to the fragment
            followersListFragment.setArguments(args);

            loadFragment("FOLLOWERS_LIST_FRAGMENT", followersListFragment);
        });


        db.collection("users")
                .document(userId)
                .update("booksRead" , Constants.ReadBooks.size())
                .addOnSuccessListener(null)
                .addOnFailureListener(null);


        bookReadCount.setText(format("%d", Constants.ReadBooks.size()));

        // Load user data
        loadUserData();



        bookshelf.setOnClickListener(v -> openBookshelfFragment(userId));

        btnMarked.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userDocRef = db.collection("users").document(userId);

            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, Object> currentlyReadingMap = (Map<String, Object>) documentSnapshot.get("CurrentlyReading");

                    if (currentlyReadingMap != null) {
                        BookResponse.VolumeInfo volumeInfo = new BookResponse.VolumeInfo();
                        volumeInfo.setTitle((String) currentlyReadingMap.get("title"));
                        volumeInfo.setSubtitle((String) currentlyReadingMap.get("subtitle"));
                        volumeInfo.setAuthors((List<String>) currentlyReadingMap.get("authors"));
                        volumeInfo.setPublisher((String) currentlyReadingMap.get("publisher"));
                        volumeInfo.setPublishedDate((String) currentlyReadingMap.get("publishedDate"));
                        volumeInfo.setDescription((String) currentlyReadingMap.get("description"));
                        volumeInfo.setPageCount(((Long) currentlyReadingMap.get("pageCount")).intValue());
                        volumeInfo.setCategories((List<String>) currentlyReadingMap.get("categories"));
                        volumeInfo.setAverageRating((Double) currentlyReadingMap.get("averageRating"));
                        volumeInfo.setRatingsCount(((Long) currentlyReadingMap.get("ratingsCount")).intValue());
                        volumeInfo.setLanguage((String) currentlyReadingMap.get("language"));
                        volumeInfo.setPreviewLink((String) currentlyReadingMap.get("previewLink"));
                        volumeInfo.setInfoLink((String) currentlyReadingMap.get("infoLink"));

                        Map<String, Object> imageLinksMap = (Map<String, Object>) currentlyReadingMap.get("imageLinks");
                        if (imageLinksMap != null) {
                            BookResponse.VolumeInfo.ImageLinks imageLinks = new BookResponse.VolumeInfo.ImageLinks();
                            imageLinks.setSmallThumbnail((String) imageLinksMap.get("smallThumbnail"));
                            imageLinks.setThumbnail((String) imageLinksMap.get("thumbnail"));
                            volumeInfo.setImageLinks(imageLinks);
                        }

                        volumeInfo.setReadingProgress(((Long) Objects.requireNonNull(currentlyReadingMap.get("readingProgress"))).intValue());

                        Constants.CurrentlyReading = volumeInfo;

                        // ✅ Navigate after data is ready
                        loadFragment("EDIT_BOOK_FRAGMENT", new EditBookFragment());

                    } else {
                        Toast.makeText(getContext(), "אין ספר שנבחר כרגע", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "לא נמצאו פרטי משתמש", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "אירעה שגיאה בעת טעינת הספר", Toast.LENGTH_SHORT).show();
                Log.e("Firestore", "Error fetching CurrentlyReading", e);
            });
        });

        btnProgress.setOnClickListener(v -> {
            if (Constants.CurrentlyReading != null) {
                loadFragment("BOOK_FRAGMENT", new BookFragment());
            } else {
                Toast.makeText(getContext(), "לא נבחר ספר להצגה", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    private void loadUserData() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DocumentReference userRef = db.collection("users").document(userId);
        DocumentReference CurrentlyReadingRef = db.collection("users").document(userId).collection("currentlyReading").document("currentlyReading");

        userRef.get().addOnCompleteListener(this::onComplete);
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

    private void onComplete(Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document.exists()) {
                String usernameValue = document.getString("username");
                int followers = Objects.requireNonNull(document.getLong("followersCount")).intValue();
                int followed = Objects.requireNonNull(document.getLong("followedCount")).intValue();

                username.setText(usernameValue);
                followersCount.setText(format("%d", followers));
                followedCount.setText(format("%d", followed));
            } else {
                Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
        }
    }
    private void loadFollowData() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("users").document(userId).collection("followers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String username = doc.getString("username");
                        if (username != null) {
                            Constants.Followers.add(username);
                        }
                    }
                });

        db.collection("users").document(userId).collection("followed")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String username = doc.getString("username");
                        if (username != null) {
                            Constants.Followed.add(username);
                        }
                    }
                });
    }

    private void openBookshelfFragment(String userId) {
        BookshelfFragment bookshelfFragment = new BookshelfFragment();

        Bundle args = new Bundle();
        args.putString("userId", userId);
        bookshelfFragment.setArguments(args);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace current fragment and add to back stack
        transaction.replace(R.id.fragment_container, bookshelfFragment, "BOOKSHELF_FRAGMENT");
        transaction.addToBackStack("BOOKSHELF_FRAGMENT");
        transaction.commit();
    }

    public void CheckCurrentlyReading() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        // Fetch the main document once
        // FirebaseFirestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

// Reference to the specific user's document
        DocumentReference userDocRef = db.collection("users").document(userId);

// Log the path for debugging purposes
        Log.d("Firestore", "Attempting to get document from path: users/" + userId);

// Retrieve the document containing the 'CurrentlyReading' map field
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            // Log whether the document exists
            if (documentSnapshot.exists()) {
                Log.d("Firestore", "Document found!");

                // Retrieve the 'CurrentlyReading' map field from the document
                Map<String, Object> currentlyReadingMap = (Map<String, Object>) documentSnapshot.get("CurrentlyReading");

                if (currentlyReadingMap != null) {
                    Log.d("Firestore", "'CurrentlyReading' field found!");

                    // Initialize the VolumeInfo object
                    BookResponse.VolumeInfo volumeInfo = new BookResponse.VolumeInfo();

                    // Map the fields from the Firestore document map to the VolumeInfo object
                    volumeInfo.setTitle((String) currentlyReadingMap.get("title"));
                    volumeInfo.setSubtitle((String) currentlyReadingMap.get("subtitle"));
                    volumeInfo.setAuthors((List<String>) currentlyReadingMap.get("authors"));
                    volumeInfo.setPublisher((String) currentlyReadingMap.get("publisher"));
                    volumeInfo.setPublishedDate((String) currentlyReadingMap.get("publishedDate"));
                    volumeInfo.setDescription((String) currentlyReadingMap.get("description"));
                    volumeInfo.setPageCount(((Long) Objects.requireNonNull(currentlyReadingMap.get("pageCount"))).intValue());
                    volumeInfo.setCategories((List<String>) currentlyReadingMap.get("categories"));
                    volumeInfo.setAverageRating((Double) currentlyReadingMap.get("averageRating"));
                    volumeInfo.setRatingsCount(((Long) Objects.requireNonNull(currentlyReadingMap.get("ratingsCount"))).intValue());
                    volumeInfo.setLanguage((String) currentlyReadingMap.get("language"));
                    volumeInfo.setPreviewLink((String) currentlyReadingMap.get("previewLink"));
                    volumeInfo.setInfoLink((String) currentlyReadingMap.get("infoLink"));

                    // Handle imageLinks as a nested map
                    Map<String, Object> imageLinksMap = (Map<String, Object>) currentlyReadingMap.get("imageLinks");
                    if (imageLinksMap != null) {
                        BookResponse.VolumeInfo.ImageLinks imageLinks = new BookResponse.VolumeInfo.ImageLinks();
                        imageLinks.setSmallThumbnail((String) imageLinksMap.get("smallThumbnail"));
                        imageLinks.setThumbnail((String) imageLinksMap.get("thumbnail"));
                        volumeInfo.setImageLinks(imageLinks);
                    }

                    volumeInfo.setReadingProgress(((Long) Objects.requireNonNull(currentlyReadingMap.get("readingProgress"))).intValue());

                    // Set the currentlyReading variable
                    Constants.CurrentlyReading = volumeInfo;

                    // Handle the loaded data here
                } else {
                    Log.d("Firestore", "CurrentlyReading field is null or does not exist.");
                }
            } else {
                Log.d("Firestore", "No such document found!");
            }
        }).addOnFailureListener(e -> {
            // Log error if the document retrieval fails
            Log.w("Firestore", "Error getting document.", e);
        });

    }

}
