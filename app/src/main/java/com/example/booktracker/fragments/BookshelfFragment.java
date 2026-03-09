package com.example.booktracker.fragments;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.airbnb.lottie.LottieAnimationView;
import com.example.booktracker.BookResponse;
import com.example.booktracker.Adapters.BookShelfAdapter;
import com.example.booktracker.Constants;
import com.example.booktracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.Objects;

public class BookshelfFragment extends Fragment {

    private GridView gridViewBookshelf;
    private Button backButton;
    private BookShelfAdapter bookShelfAdapter;
    private FirebaseFirestore db;
    private LottieAnimationView animationView;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bookshelf, container, false);

        // Initialize UI components
        gridViewBookshelf = view.findViewById(R.id.gridViewBookshelf);
        backButton = view.findViewById(R.id.Back);
        animationView = view.findViewById(R.id.bookshelfAnimation);
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", 0);

        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", 0);
        String userId = sharedPreferences.getString("userId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        Bundle args1 = getArguments();
        if (args1 != null && args1.getString("userId") != null) {
            userId = args1.getString("userId"); // Override with argument if available
        }

        Log.d("BookshelfFragment", "Loading bookshelf for userId: " + userId);


        // Load bookshelf for the specified user
        loadBookshelf(userId);

        // Animation fade-out effect
        animationView.postDelayed(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    animationView.animate()
                            .alpha(0f)
                            .setDuration(1000)
                            .withEndAction(() -> {
                                animationView.setVisibility(View.GONE);
                                gridViewBookshelf.setVisibility(View.VISIBLE);
                                gridViewBookshelf.animate().alpha(1f).setDuration(500).start();
                            })
                            .start();
                }
            }
        }, 4000);

        // Handle back button press
        backButton.setOnClickListener(v -> handleBackButton());

        return view;
    }

    private void handleBackButton() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.popBackStack(); // Go back to the previous fragment
    }

    private void loadBookshelf(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e("Bookshelf", "Invalid user ID.");
            return;
        }

        db.collection("users").document(userId).collection("bookshelf")
                .get(Source.SERVER)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // No books found
                            Constants.ReadBooks.clear();
                            if (bookShelfAdapter != null) {
                                bookShelfAdapter.notifyDataSetChanged();
                            }
                            Toast.makeText(getActivity(), "No books in this user's bookshelf.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Constants.ReadBooks.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BookResponse.VolumeInfo book = document.toObject(BookResponse.VolumeInfo.class);
                            Constants.ReadBooks.add(book);
                        }

                        // Set or update the adapter
                        if (bookShelfAdapter == null) {
                            bookShelfAdapter = new BookShelfAdapter(Constants.ReadBooks, R.layout.booksehld_book, getActivity());
                            gridViewBookshelf.setAdapter(bookShelfAdapter);
                        } else {
                            bookShelfAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.w("Bookshelf", "Error getting documents", task.getException());
                    }
                });
    }
}
