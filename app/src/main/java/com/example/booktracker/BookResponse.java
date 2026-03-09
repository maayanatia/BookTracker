package com.example.booktracker;

import android.app.SharedElementCallback;

import java.util.List;

public class BookResponse {
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class Item {
        private VolumeInfo volumeInfo;

        public VolumeInfo getVolumeInfo() {
            return volumeInfo;
        }

        public void setVolumeInfo(VolumeInfo volumeInfo) {
            this.volumeInfo = volumeInfo;
        }
    }

    public static class VolumeInfo {
        private String title;
        private String subtitle;
        private List<String> authors;
        private String publisher;
        private String publishedDate;
        private String description;
        private int pageCount;
        private List<String> categories;
        private double averageRating;
        private int ratingsCount;
        private String language;
        private String previewLink;
        private String infoLink;
        private ImageLinks imageLinks;
        private int readingProgress;

        // Getters and setters for all fields


        
        public VolumeInfo(String title,String subtitle,List<String> authors,String publisher,String publishedDate,String description,int pageCount,List<String> categories,double averageRating,int ratingsCount,String language,String previewLink,String infoLink,ImageLinks imageLinks ) {
            this.title = title;
            this.subtitle=subtitle;
            this.authors=authors;
            this.publisher=publisher;
            this.publishedDate=publishedDate;
            this.description=description;
            this.pageCount=pageCount;
            this.categories=categories;
            this.averageRating=averageRating;
            this.ratingsCount=ratingsCount;
            this.language=language;
            this.previewLink=previewLink;
            this.infoLink=infoLink;
            this.imageLinks=imageLinks;
            this.readingProgress = 0; // Default reading progress
        }

        public VolumeInfo(String title) {
        }

        public VolumeInfo() {
        }


        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public List<String> getAuthors() {
            return authors;
        }

        public String getPublisher() {
            return publisher;
        }

        public String getPublishedDate() {
            return publishedDate;
        }

        public String getDescription() {
            return description;
        }

        public int getPageCount() {
            return pageCount;
        }

        public List<String> getCategories() {
            return categories;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public int getRatingsCount() {
            return ratingsCount;
        }

        public String getLanguage() {
            return language;
        }

        public String getPreviewLink() {
            return previewLink;
        }
        public String getInfoLink() {
            return infoLink;
        }
        public ImageLinks getImageLinks() {
            return imageLinks;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public void setAuthors(List<String> authors) {
            this.authors = authors;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public void setPublishedDate(String publishedDate) {
            this.publishedDate = publishedDate;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }

        public void setCategories(List<String> categories) {
            this.categories = categories;
        }

        public void setAverageRating(double averageRating) {
            this.averageRating = averageRating;
        }

        public void setRatingsCount(int ratingsCount) {
            this.ratingsCount = ratingsCount;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public void setPreviewLink(String previewLink) {
            this.previewLink = previewLink;
        }

        public void setInfoLink(String infoLink) {
            this.infoLink = infoLink;
        }

        public void setImageLinks(ImageLinks imageLinks) {
            this.imageLinks = imageLinks;
        }
        public void setCurrentlyReading(BookResponse.VolumeInfo book){

        }
        public int getReadingProgress() {
            return readingProgress;
        }

        public void setReadingProgress(int readingProgress) {
            this.readingProgress = readingProgress;
        }


        public static class ImageLinks {
            private String smallThumbnail;
            private String thumbnail;

            // Getters and setters


            public ImageLinks(String smallThumbnail, String thumbnail) {
                this.smallThumbnail = smallThumbnail;
                this.thumbnail = thumbnail;
            }

            public ImageLinks() {
            }

            public String getSmallThumbnail() {
                return smallThumbnail;
            }

            public String getThumbnail() {
                return thumbnail;
            }

            public void setSmallThumbnail(String smallThumbnail) {
                this.smallThumbnail = smallThumbnail;
            }
            public void setThumbnail(String thumbnail) {
                this.thumbnail = thumbnail;
            }
        }
    }
}

