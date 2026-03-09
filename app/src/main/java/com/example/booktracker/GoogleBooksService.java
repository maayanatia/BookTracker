package com.example.booktracker;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleBooksService {
    @GET("volumes")
    Call<BooksResponse> getBooks(
            @Query("q") String query,
            @Query("key") String apiKey
    );
    @GET ("pageCount")
    Call<BooksResponse> getPageCount(
            @Query("q") String query,
            @Query("key") String apiKey
    );
    @GET("volumes")
    Call<BookResponse> searchBooks(@Query("q") String query);
}
