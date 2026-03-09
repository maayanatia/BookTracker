package com.example.booktracker.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.booktracker.R;
import com.example.booktracker.User;
import com.example.booktracker.fragments.UserProfileFragment;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.username.setText(user.getUsername());

        // Check for null lists and use 0 if needed.
        int followersSize = (user.getFollowers() != null) ? user.getFollowers().size() : 0;
        int followedSize = (user.getFollowed() != null) ? user.getFollowed().size() : 0;

        holder.followersCount.setText("Followers: " + followersSize);
        holder.followedCount.setText("Followed: " + followedSize);
        holder.booksReadCount.setText("Books Read: " + user.getBooksReadCount());

        Glide.with(context).load(user.getProfileImageUrl()).into(holder.profileImage);

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("username", user.getUsername());
            bundle.putString("viewedUserId", user.getUserId()); // Pass the actual user ID from Firestore
            UserProfileFragment userProfileFragment = new UserProfileFragment();
            userProfileFragment.setArguments(bundle);

            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, userProfileFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView username;
        TextView followersCount;
        TextView followedCount;
        TextView booksReadCount;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            followersCount = itemView.findViewById(R.id.followers_count);
            followedCount = itemView.findViewById(R.id.followed_count);
            booksReadCount = itemView.findViewById(R.id.books_read_count);
        }
    }
}
