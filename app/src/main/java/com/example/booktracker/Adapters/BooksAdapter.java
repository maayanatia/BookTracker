package com.example.booktracker.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.booktracker.BookResponse;
import com.example.booktracker.Constants;
import com.example.booktracker.Item;
import com.example.booktracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.List;
import java.util.Objects;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {
    private List<Item> books;
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public BooksAdapter(List<Item> books, Context context) {
        this.books = books;
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences("book_prefs", Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();

        loadBookshelf(); // Load bookshelf once to avoid repeated calls
        loadFollowedUsers(); // Load followed users once to avoid repeated calls
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Item book = books.get(position);

        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("users")
                .document(userId)
                .update("followed", Constants.FollowedCount , "followers", Constants.FollowersCount )
                .addOnSuccessListener(null)
                .addOnFailureListener(null);



        if (book != null && book.getVolumeInfo().getTitle() != null) {
            holder.tvBookTitle.setText(book.getVolumeInfo().getTitle());
        } else {
            holder.tvBookTitle.setText("Unknown Title");
        }


        assert book != null;
        String bookId = book.getVolumeInfo().getTitle();

        // Check if the book is already in the bookshelf and set the button text
        if (Constants.ReadBooks.contains(book.getVolumeInfo())) {
            holder.read.setText("Remove from bookshelf");
        } else {
            holder.read.setText("Read");
        }

        assert book != null;
        if (Constants.ReadBooks.contains(book.getVolumeInfo())) {
            holder.read.setText("Remove from bookshelf");
        } else {
            holder.read.setText("Add to bookshelf");
        }

        if (book != null && book.getVolumeInfo().getImageLinks() != null) {
            Glide.with(context)
                    .load(book.getVolumeInfo().getImageLinks().getThumbnail())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("GlideError", "Failed to load image", e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.ivBookCover);
        } else {
            holder.ivBookCover.setImageResource(R.drawable.placeholder);
        }

        holder.read.setText(Constants.ReadBooks.contains(book.getVolumeInfo()) ? "Remove from bookshelf" : "Read");




        holder.read.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                assert book != null;
                String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                String bookId = book.getVolumeInfo().getTitle(); // Use a unique identifier if available

                if (holder.currentlyReading.isChecked()) {
                    Toast.makeText(context, "You can't mark this book as read from here.", Toast.LENGTH_SHORT).show();
                } else {

                    if (!Constants.ReadBooks.contains(book.getVolumeInfo())) {
                        // Add the book to the bookshelf locally
                        Constants.ReadBooks.add(book.getVolumeInfo());
                        holder.read.setText("Remove from bookshelf");

                        // Update Firestore
                        db.collection("users")
                                .document(userId)
                                .collection("bookshelf")
                                .document(bookId)
                                .set(book.getVolumeInfo())
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Book added to bookshelf"))
                                .addOnFailureListener(e -> Log.e("FirestoreError", "Error adding book", e));

                        db.collection("users")
                                .document(userId)
                                .update("booksRead", Constants.ReadBooks.size())
                                .addOnSuccessListener(null)
                                .addOnFailureListener(null);
                    } else {
                        // Remove the book locally
                        Constants.ReadBooks.remove(book.getVolumeInfo());
                        holder.read.setText("Read");

                        // Update Firestore
                        db.collection("users")
                                .document(userId)
                                .collection("bookshelf")
                                .document(bookId)
                                .delete()
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Book removed from bookshelf"))
                                .addOnFailureListener(e -> Log.e("FirestoreError", "Error removing book", e));

                        db.collection("users")
                                .document(userId)
                                .update("booksRead", Constants.ReadBooks.size())
                                .addOnSuccessListener(null)
                                .addOnFailureListener(null);
                    }
                }
            }
        });

        holder.currentlyReading.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Constants.ReadBooks.contains(book.getVolumeInfo())) {
                    Toast.makeText(buttonView.getContext(),
                            "You can't mark this book as currently reading.",
                            Toast.LENGTH_SHORT).show();
                    holder.currentlyReading.setChecked(false);
                } else {
                    Constants.CurrentlyReading = book.getVolumeInfo();
                    db.collection("users")
                            .document(userId)
                            .update("CurrentlyReading", Constants.CurrentlyReading);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public void updateData(List<Item> newBooks) {
        books.clear();
        books.addAll(newBooks);
        notifyDataSetChanged();
    }

    private void loadBookshelf() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db.collection("users").document(userId).collection("bookshelf")
                .get(Source.SERVER)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Constants.ReadBooks.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Constants.ReadBooks.add(document.toObject(BookResponse.VolumeInfo.class));
                        }
                        Log.d("Bookshelf", "Books loaded: " + Constants.ReadBooks.size());
                    } else {
                        Log.w("Bookshelf", "Error getting documents", task.getException());
                    }
                });
    }

    private void loadFollowedUsers() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db.collection("users").document(userId).collection("followed")
                .get(Source.SERVER)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Constants.Followed.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Constants.Followed.add(document.getId());
                        }
                        Log.d("FollowedUsers", "Followed users loaded: " + Constants.Followed.size());
                    } else {
                        Log.w("FollowedUsers", "Error getting documents", task.getException());
                    }
                });
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover;
        TextView tvBookTitle;
        Button read;
        CheckBox currentlyReading;

        public BookViewHolder(View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            read = itemView.findViewById(R.id.read);
            currentlyReading = itemView.findViewById(R.id.cbCurrentlyReading);
        }
    }

    public void updateBookCount() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db.collection("users")
                .document(userId)
                .update("booksRead" , Constants.ReadBooks.size())
                .addOnSuccessListener(null)
                .addOnFailureListener(null);
    }
    public void syncBookshelfFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        db.collection("users").document(userId).collection("bookshelf")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w("SyncBookshelf", "Listen failed.", e);
                        return;
                    }
                    if (querySnapshot != null) {
                        // Clear local list to avoid duplicates
                        Constants.ReadBooks.clear();
                        // Add every book document to the local array list
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            BookResponse.VolumeInfo book = document.toObject(BookResponse.VolumeInfo.class);
                            if (book != null) {
                                Constants.ReadBooks.add(book);
                            }
                        }
                        Log.d("SyncBookshelf", "Bookshelf synced. Total books: " + Constants.ReadBooks.size());
                        // Optionally, notify any adapters or UI elements to refresh their data here.
                    }
                });
    }

    public void CheckCurrent(){
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        String title = db.collection("users").document(userId).collection("title").get().toString();
        String subtitle = db.collection("users").document(userId).collection("subtitle").get().toString();
        List<String> authors = (List<String>) db.collection("users").document(userId).collection("authors").get();
        String publisher = db.collection("users").document(userId).collection("publisher").get().toString();
        String publishedDate = db.collection("users").document(userId).collection("publishedDate").get().toString();
        String description = db.collection("users").document(userId).collection("description").get().toString();
        int pageCount = Integer.parseInt(db.collection("users").document(userId).collection("pageCount").get().toString());
        List<String> categories = (List<String>) db.collection("users").document(userId).collection("categories").get();
        double averageRating = Double.parseDouble(db.collection("users").document(userId).collection("averageRating").get().toString());
        int ratingsCount = Integer.parseInt(db.collection("users").document(userId).collection("ratingsCount").get().toString());
        String language = db.collection("users").document(userId).collection("language").get().toString();
        String previewLink = db.collection("users").document(userId).collection("previewLink").get().toString();
        int readingProgress = Integer.parseInt(db.collection("users").document(userId).collection("readingProgress").get().toString());
        String smallThumbnail = db.collection("users").document(userId).collection("smallThumbnail").get().toString();
        String thumbnail = db.collection("users").document(userId).collection("thumbnail").get().toString();

        BookResponse.VolumeInfo.ImageLinks imagelinks= new BookResponse.VolumeInfo.ImageLinks(smallThumbnail , thumbnail);

        BookResponse.VolumeInfo CurrentBook = new BookResponse.VolumeInfo();
        CurrentBook.setTitle(title);
        CurrentBook.setSubtitle(subtitle);
        CurrentBook.setAuthors(authors);
        CurrentBook.setPublisher(publisher);
        CurrentBook.setPublishedDate(publishedDate);
        CurrentBook.setDescription(description);
        CurrentBook.setPageCount(pageCount);
        CurrentBook.setCategories(categories);
        CurrentBook.setAverageRating(averageRating);
        CurrentBook.setRatingsCount(ratingsCount);
        CurrentBook.setLanguage(language);
        CurrentBook.setPreviewLink(previewLink);
        CurrentBook.setReadingProgress(readingProgress);
        CurrentBook.setImageLinks(imagelinks);
        Constants.CurrentlyReading = CurrentBook;
    }

}
