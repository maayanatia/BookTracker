package com.example.booktracker.fragments;

import static com.example.booktracker.fragments.MainFragment.API_KEY;
import static com.example.booktracker.fragments.MainFragment.BASE_URL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booktracker.Adapters.BooksAdapter;
import com.example.booktracker.BooksResponse;
import com.example.booktracker.GoogleBooksService;
import com.example.booktracker.Item;
import com.example.booktracker.R;
import com.example.booktracker.RetrofitClient;
import com.example.booktracker.User;
import com.example.booktracker.Adapters.UserAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class search_fragment extends Fragment {
    private EditText etSearch;
    private String query;
    private RecyclerView rvSearchResults;
    private BooksAdapter booksAdapter;
    private Button srchBtn;
    private Button searchButtonUsers;
    private List<Item> allBooks; // Store the original list of books
    private List<Item> filteredBooks; // Store the filtered list
    private List<User> users;
    private UserAdapter userAdapter;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);

        BottomNavigationView bottomNavigation = view.findViewById(R.id.bottom_navigation);

        etSearch = view.findViewById(R.id.etSearch);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getActivity()));
        srchBtn = view.findViewById(R.id.searchButton);
        searchButtonUsers = view.findViewById(R.id.searchButtonUsers);
        sharedPreferences = requireActivity().getSharedPreferences("book_prefs", Context.MODE_PRIVATE);

        // Initialize the lists
        allBooks = new ArrayList<>(); // This should be populated with books from the API
        filteredBooks = new ArrayList<>();

        // Set up the BooksAdapter for book search results
        booksAdapter = new BooksAdapter(filteredBooks, requireActivity());
        rvSearchResults.setAdapter(booksAdapter);
        firestore = FirebaseFirestore.getInstance();
        users = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), users);

        searchButtonUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvSearchResults.setAdapter(userAdapter);
                rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
                String searchText = etSearch.getText().toString();
                if (!TextUtils.isEmpty(searchText)) {
                    searchUsers(searchText);
                } else {
                    Toast.makeText(getContext(), "Please enter a username to search", Toast.LENGTH_SHORT).show();
                }
            }
        });

        srchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query = etSearch.getText().toString();
                fetchBooks(query);
            }
        });

        return view;
    }

    private void fetchBooks(String query) {
        GoogleBooksService service = RetrofitClient.getClient(BASE_URL).create(GoogleBooksService.class);
        Call<BooksResponse> call = service.getBooks(query, API_KEY);

        call.enqueue(new Callback<BooksResponse>() {
            @Override
            public void onResponse(@NonNull Call<BooksResponse> call, @NonNull Response<BooksResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Item> items = response.body().getItems();
                    List<Item> filteredBooks1 = new ArrayList<>();
                    for (Item book : items) {
                        if (book.getVolumeInfo().getImageLinks() != null &&
                                book.getVolumeInfo().getImageLinks().getThumbnail() != null) {
                            filteredBooks1.add(book);
                        }
                    }
                    booksAdapter.updateData(filteredBooks1);
                    rvSearchResults.setAdapter(booksAdapter);
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

    @SuppressLint("NotifyDataSetChanged")
    private void searchUsers(String searchText) {
        firestore.collection("users")
                .whereEqualTo("username", searchText)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        users.clear();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String docId = document.getId();
                                String username = document.getString("username");
                                String profileImageUrl = document.getString("profileImageUrl");
                                int followers = Objects.requireNonNull(document.getLong("followersCount")).intValue();
                                int followed = Objects.requireNonNull(document.getLong("followedCount")).intValue();
                                int booksReadCount = Objects.requireNonNull(document.getLong("booksRead")).intValue();
                                User user = new User(username, profileImageUrl, followers, followed, booksReadCount);
                                user.setUserId(docId);  // Make sure your User class has this setter
                                users.add(user);
                            }
                            userAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(getContext(), "Search failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
