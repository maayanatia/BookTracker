package com.example.booktracker.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booktracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public class FollowedListAdapter extends RecyclerView.Adapter<FollowedListAdapter.ViewHolder> {

    private final List<String> followedUsers;
    private final Context context;

    public FollowedListAdapter(Context context,int resource, List<String> followedUsers) {
        super();
        this.context = context;
        this.followedUsers = followedUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.followed_user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String username = followedUsers.get(position);
        holder.usernameTextView.setText(username);

        holder.unfollowButton.setOnClickListener(v -> unfollowUser(username, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return followedUsers.size();
    }

    private void unfollowUser(String username, int position) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .collection("followed")
                .document(username)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    followedUsers.remove(position);
                    notifyItemRemoved(position);

                    // Decrease the followed count in Firestore
                    db.collection("users").document(userId)
                            .update("followedCount", FieldValue.increment(-1))
                            .addOnSuccessListener(unused -> Toast.makeText(context, "Unfollowed " + username, Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to update followed count", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to unfollow " + username, Toast.LENGTH_SHORT).show());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView usernameTextView;
        final Button unfollowButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.followed_username);
            unfollowButton = itemView.findViewById(R.id.btnUnfollow);
        }
    }
}
