package com.example.booktracker;

import java.util.List;

public class User {
    private String userId; // New field to store Firestore document ID
    private String username;
    private String profileImageUrl;
    private int followersCount;
    private int followedCount;
    private int booksReadCount;

    // New fields to store collections of usernames (or IDs)
    private List<String> followers;
    private List<String> followed;

    // Parameterized constructor – note that now it expects values for the new fields as well.
    public User(String username, String profileImageUrl, int followersCount, int followedCount, int booksReadCount, List<String> followers, List<String> followed) {
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.followersCount = followersCount;
        this.followedCount = followedCount;
        this.booksReadCount = booksReadCount;
        this.followers = followers;
        this.followed = followed;
    }

    // Default constructor (required for Firebase serialization)
    public User() {
    }

    public User(String username, String profileImageUrl, int followers, int followed, int booksReadCount) {
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.followersCount = followers;
        this.followedCount = followed;
        this.booksReadCount = booksReadCount;
    }

    // Getter and setter for userId
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Existing getters and setters
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getFollowersCount() {
        return followersCount;
    }
    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowedCount() {
        return followedCount;
    }
    public void setFollowedCount(int followedCount) {
        this.followedCount = followedCount;
    }

    public int getBooksReadCount() {
        return booksReadCount;
    }
    public void setBooksReadCount(int booksReadCount) {
        this.booksReadCount = booksReadCount;
    }

    // New getters and setters for the followers collection
    public List<String> getFollowers() {
        return followers;
    }
    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    // New getters and setters for the followed collection
    public List<String> getFollowed() {
        return followed;
    }
    public void setFollowed(List<String> followed) {
        this.followed = followed;
    }
}
