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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.booktracker.BookResponse;
import com.example.booktracker.Constants;
import com.example.booktracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.Objects;

public class EditBookFragment extends Fragment {
    private SeekBar sbReadingProgress;
    private TextView tvProgressPercentage;
    private BookResponse.VolumeInfo currentBook;
    private Button btnDoneEditing;
    private SharedPreferences sharedPreferences;
    private TextView tvBookTitle;
    private TextView tvAuthor;
    private TextView tvPublicationDate;
    private TextView tvGenre;
    private ImageView ivBookCover;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_book, container, false);

        // Initialize UI components
        sbReadingProgress = view.findViewById(R.id.sbReadingProgress);
        tvProgressPercentage = view.findViewById(R.id.tvProgressPercentage);
        tvBookTitle = view.findViewById(R.id.tvBookTitle);
        tvAuthor = view.findViewById(R.id.tvAuthor);
        tvPublicationDate = view.findViewById(R.id.tvPublicationDate);
        tvGenre = view.findViewById(R.id.tvGenre);
        ivBookCover = view.findViewById(R.id.ivBookCover);
        btnDoneEditing = view.findViewById(R.id.btnDoneEditing);

        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();

        // Retrieve the currentBook from the Constants
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        if(db.collection(userId).document("CurrentlyReading").get() != null)
            db.collection(userId).document("CurrentlyReading")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Log.d("CurrentlyReading", "DocumentSnapshot exists: " + documentSnapshot.exists());
                        if (documentSnapshot.exists()) {
                            Log.d("CurrentlyReading", "Document exists");
                            assert Constants.CurrentlyReading != null;
                            Constants.CurrentlyReading= documentSnapshot.toObject(BookResponse.VolumeInfo.class);
                            Log.d("CurrentlyReading", "CurrentlyReading: " + Constants.CurrentlyReading.getTitle());
                        }
                        else {
                            Log.d("CurrentlyReading", "Document does not exist");
                        }
                    });
        currentBook = Constants.CurrentlyReading;
        if (currentBook != null) {
            Log.d("EditBookFragment", "Book details received: " + currentBook.getTitle());
        } else {
            Log.e("EditBookFragment", "No book details received");
        }

        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", requireContext().MODE_PRIVATE);

        // Set initial data to UI components
        tvBookTitle.setText(currentBook.getTitle());
        tvAuthor.setText(currentBook.getAuthors() != null ? String.join(", ", currentBook.getAuthors()) : "Unknown Author");
        tvPublicationDate.setText(currentBook.getPublishedDate());
        tvGenre.setText(currentBook.getCategories() != null ? String.join(", ", currentBook.getCategories()) : "Unknown Genre");
        if (currentBook.getImageLinks() != null) {
            Glide.with(this)
                    .load(currentBook.getImageLinks().getThumbnail())
                    .into(ivBookCover);
        } else {
            ivBookCover.setImageResource(R.drawable.placeholder);
        }

        // Set initial progress and set up SeekBar listener
        sbReadingProgress.setProgress(currentBook.getReadingProgress());
        tvProgressPercentage.setText(currentBook.getReadingProgress() + "%");


        sbReadingProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvProgressPercentage.setText(progress + "%");
                if (currentBook != null) {
                    currentBook.setReadingProgress(progress);
                    saveReadingProgress(currentBook);
                } else {
                    Log.e("EditBookFragment", "currentBook is null in onProgressChanged");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tvProgressPercentage.setText(sbReadingProgress.getProgress() + "%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvProgressPercentage.setText(sbReadingProgress.getProgress() + "%");
                // Check if the progress is 100%
                if (sbReadingProgress.getProgress() == 100) {
                    Constants.ReadBooks.add(currentBook);
                }
            }
        });

       btnDoneEditing.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
               String bookId = currentBook.getTitle();

               if(sbReadingProgress.getProgress()==100){
                   Constants.ReadBooks.add(currentBook);
                   Constants.CurrentlyReading = null;
                   db.collection("users")
                           .document(userId)
                           .collection("bookshelf")
                           .document(bookId)
                           .set(currentBook)
                           .addOnSuccessListener(aVoid -> Log.d("Firestore", "Book added to bookshelf"))
                           .addOnFailureListener(e -> Log.e("FirestoreError", "Error adding book", e));

                   db.collection("users")
                           .document(userId)
                           .update("booksRead", Constants.ReadBooks.size())
                           .addOnSuccessListener(null)
                           .addOnFailureListener(null);

                       FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                       fragmentManager.popBackStack(); // Go back to the previous fragment
               }
               else{
                   db.collection("users")
                                   .document(userId)
                                           .update("readingProgress", sbReadingProgress.getProgress())
                                           .addOnSuccessListener(null)
                                           .addOnFailureListener(null);
                   FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                   fragmentManager.popBackStack(); // Go back to the previous fragment
               }
           }
       });

        return view;
    }

    private void saveReadingProgress(BookResponse.VolumeInfo book) {
        // Save the reading progress to SharedPreferences or a database
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ReadingProgress", requireContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(book.getTitle(), book.getReadingProgress()); // Assuming title is unique
        editor.apply();
    }
}
