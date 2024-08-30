package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final Context context;
    private List<User> userList;
    private final OnUserClickListener onUserClickListener;
    private final OnUserLongClickListener onUserLongClickListener; // Listener for user long-clicks

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public interface OnUserLongClickListener {
        void onUserLongClick(User user);
    }

    public UserAdapter(Context context, List<User> userList, OnUserClickListener onUserClickListener, OnUserLongClickListener onUserLongClickListener) {
        this.context = context;
        this.userList = userList != null ? userList : new ArrayList<>();
        this.onUserClickListener = onUserClickListener;
        this.onUserLongClickListener = onUserLongClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.firstNameTextView.setText(user.getFirst_name());
        holder.lastNameTextView.setText(user.getLast_name());
        Glide.with(context).load(user.getAvatar()).into(holder.avatarImageView);

        // Add an animation
        holder.itemView.setAlpha(0);
        holder.itemView.animate().alpha(1).setDuration(1000).start();

        holder.itemView.setOnClickListener(v -> onUserClickListener.onUserClick(user));

        holder.itemView.setOnLongClickListener(v -> {
            onUserLongClickListener.onUserLongClick(user);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setUserList(List<User> userList) {
        this.userList = userList != null ? userList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView firstNameTextView;
        TextView lastNameTextView;
        ImageView avatarImageView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.firstNameTextView);
            lastNameTextView = itemView.findViewById(R.id.lastNameTextView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
        }
    }
}


