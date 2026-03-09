package com.example.booktracker.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.booktracker.BookResponse;
import com.example.booktracker.R;

import java.util.ArrayList;

public class BookShelfAdapter extends ArrayAdapter<BookResponse.VolumeInfo> {
    private ArrayList<BookResponse.VolumeInfo> books;
    private Context context;
    private SharedPreferences sharedPreferences;

    public BookShelfAdapter(ArrayList<BookResponse.VolumeInfo> books, int resource, Context context) {
        super(context, 0, books);
        this.books = books;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.booksehld_book, parent, false);
        }

        // Get the current book from the list
        BookResponse.VolumeInfo book = books.get(position);

        // Get the views from the layout
        ImageView ivBookCover = convertView.findViewById(R.id.bookCoverImageView);
        TextView tvBookTitle = convertView.findViewById(R.id.bookTitleTextView);
        TextView tvPageCount = convertView.findViewById(R.id.PageCount);
        sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Set the book data to the views
        tvBookTitle.setText(book.getTitle());  // Set the book title
        tvPageCount.setText(String.format("Pages: %d", book.getPageCount()));  // Set page count

        // Load the book cover image using Glide
        String thumbnailUrl = book.getImageLinks() != null ? book.getImageLinks().getThumbnail() : null;
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            Glide.with(context)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.placeholder)  // Placeholder image while loading
                    .error(R.drawable.placeholder)  // Error image if loading fails
                    .into(ivBookCover);
        } else {
            ivBookCover.setImageResource(R.drawable.placeholder);  // Show placeholder if no image
        }

        return convertView;
    }
}
