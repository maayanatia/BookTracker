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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booktracker.Adapters.BooksAdapter;
import com.example.booktracker.BooksResponse;
import com.example.booktracker.Constants;
import com.example.booktracker.GoogleBooksService;
import com.example.booktracker.Item;
import com.example.booktracker.R;
import com.example.booktracker.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends Fragment {
    static final String API_KEY = "AIzaSyDrp72XUG7j7P448Jmxw_BPLMkIn37uGI0";
    static final String BASE_URL = "https://www.googleapis.com/books/v1/";
    private BooksAdapter booksAdapter;
    private RecyclerView rvBooks;

    private void printLog(String s){
        Log.d("Lifecycle", s);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);


        rvBooks = view.findViewById(R.id.rvBooks);
        rvBooks.setLayoutManager(new LinearLayoutManager(getActivity()));
        booksAdapter = new BooksAdapter(new ArrayList<>(), requireActivity());
        rvBooks.setAdapter(booksAdapter);

        BottomNavigationView bottomNavigation = view.findViewById(R.id.bottom_navigation);



        // Load the default fragment
        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        fetchBooks(); // Call the method to fetch popular books

        return view;
    }

    private void fetchBooks() {
        GoogleBooksService service = RetrofitClient.getClient(BASE_URL).create(GoogleBooksService.class);
        Call<BooksResponse> call = service.getBooks("bestsellers", API_KEY);

        call.enqueue(new Callback<BooksResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooksResponse> call, @NonNull Response<BooksResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Item> books = response.body().getItems();
                    List<Item> filteredBooks = new ArrayList<>();
                    for (Item book : books) {
                        if (book.getVolumeInfo().getImageLinks() != null && book.getVolumeInfo().getImageLinks().getThumbnail() != null) {
                            filteredBooks.add(book);
                            if (Constants.ReadBooks.contains(book.getVolumeInfo())) {
                                filteredBooks.remove(book);
                            }
                        }
                    }
                    booksAdapter.updateData(filteredBooks);
                } else {
                    Toast.makeText(getActivity(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BooksResponse> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
