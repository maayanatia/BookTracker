package com.example.booktracker;

public class Item {
    private BookResponse.VolumeInfo volumeInfo;

    public BookResponse.VolumeInfo getVolumeInfo() {
        return volumeInfo;
    }

    public void setVolumeInfo(BookResponse.VolumeInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }
}
