package com.example.booktracker;

import java.util.List;

public class BooksResponse {
    List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

}
