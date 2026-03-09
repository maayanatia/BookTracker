package com.example.booktracker.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.booktracker.BookResponse;
import com.example.booktracker.Constants;
import com.example.booktracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BookFragment extends Fragment {

    private SeekBar sbReadingProgress;
    private TextView tvProgressPercentage;
    private BookResponse.VolumeInfo currentBook;
    private Button goBack;
    private TextView tvBookTitle, tvAuthor, tvPublicationDate, tvGenre, tvDescription;
    private ImageView ivBookCover;
    private SharedPreferences sharedPreferences;
    private SharedPreferences shared2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_book_activty, container, false);

        sbReadingProgress = view.findViewById(R.id.sbReadingProgress);
        tvProgressPercentage = view.findViewById(R.id.tvProgressPercentage);
        tvBookTitle = view.findViewById(R.id.tvBookTitle);
        tvAuthor = view.findViewById(R.id.tvAuthor);
        tvPublicationDate = view.findViewById(R.id.tvPublicationDate);
        tvGenre = view.findViewById(R.id.tvGenre);
        tvDescription = view.findViewById(R.id.tvDescription);
        ivBookCover = view.findViewById(R.id.ivBookCover);
        goBack = view.findViewById(R.id.goBackButton);
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", requireContext().MODE_PRIVATE);
        shared2 = requireActivity().getSharedPreferences("ReadingProgress", requireContext().MODE_PRIVATE);

        // Retrieve the book details from Constants
        currentBook = Constants.CurrentlyReading;

        sbReadingProgress.setProgress(currentBook.getReadingProgress());
        tvProgressPercentage.setText(currentBook.getReadingProgress() + "%");
        tvBookTitle.setText(currentBook.getTitle());
        tvAuthor.setText(currentBook.getAuthors() != null ? String.join(", ", currentBook.getAuthors()) : "Unknown Author");
        tvPublicationDate.setText(currentBook.getPublishedDate());
        tvGenre.setText(currentBook.getCategories() != null ? String.join(", ", currentBook.getCategories()) : "Unknown Genre");
        tvDescription.setText(currentBook.getDescription());

        if (Constants.CurrentlyReading != null && Constants.CurrentlyReading.getImageLinks() != null) {
            Glide.with(this)
                    .load(Constants.CurrentlyReading.getImageLinks().getThumbnail())
                    .into(ivBookCover);
        } else {
            ivBookCover.setImageResource(R.drawable.placeholder);
        }

        goBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        sbReadingProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvProgressPercentage.setText(progress + "%");
                if (progress == 100) {
                    checkIfBookFinished(currentBook.getTitle());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        return view;
    }

    private void checkIfBookFinished(String bookId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("books")
                .document(bookId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean isFinished = documentSnapshot.getBoolean("isFinished");
                        if (isFinished) {
                            String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                            String bookName = documentSnapshot.getString("bookName");
                            notifyFollowers(username, bookName);
                        }
                    }
                });
    }

    private void notifyFollowers(String username, String bookName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();

        // Fetch followers
        db.collection("users")
                .document(currentUserId)
                .collection("followers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String followerId = document.getId();
                            sendNotificationToUser(followerId, username, bookName);
                        }
                    }
                });
    }

    private void sendNotificationToUser(String userId, String username, String bookName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String token = documentSnapshot.getString("fcmToken");
                        if (token != null) {
                            sendFCMNotification(token, username, bookName);
                        }
                    }
                });
    }

    private void sendFCMNotification(String token, String username, String bookName) {
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            notificationBody.put("title", username + " just finished a book!");
            notificationBody.put("body", username + " just finished reading " + bookName);
            notification.put("to", token);
            notification.put("notification", notificationBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send", notification,
                response -> Log.d("FCM Response", "FCM Response: " + response.toString()),
                error -> Log.d("FCM Error", "FCM Error: " + error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "key=" + "BL4UaAh7No-B6VIndzonjmKdzNEIYF_xQ1IWw7ZVYgDblUeFV1X9dTD0Yl7Y71IWJn7L7w4O9Xsdi91IJ93Fvj0"); // SERVER_KEY from Firebase Console
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest);
    }
}
