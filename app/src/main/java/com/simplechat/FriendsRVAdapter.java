package com.simplechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import transfers.User;

public class FriendsRVAdapter  extends RecyclerView.Adapter<FriendsRVAdapter.FriendViewHolder> {


    private ArrayList<User> friends;
    private OnFriendsListListener mOnFriendsListListener;
    private OnFriendListLClickListener mOnFriendListLClickListener;


    public FriendsRVAdapter(ArrayList<User> friends, OnFriendsListListener onFriendsListListener, OnFriendListLClickListener onFriendListLClickListener) {
        this.friends = friends;
        this.mOnFriendsListListener = onFriendsListListener;
        this.mOnFriendListLClickListener = onFriendListLClickListener;
    }

    public User getItem(int position){
        return friends.get(position);
    }

    public void setFriends(ArrayList<User> friends) {
        this.friends = friends;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForFriendList = R.layout.friend_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutIdForFriendList, parent,false);
        FriendViewHolder viewHolder = new FriendViewHolder(view,mOnFriendsListListener,mOnFriendListLClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        holder.bind(friends.get(position).login);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView friendName;
        OnFriendsListListener onFriendsListListener;
        OnFriendListLClickListener onFriendListLClickListener;

        public FriendViewHolder(@NonNull View itemView, OnFriendsListListener onFriendsListListener, OnFriendListLClickListener onFriendListLClickListener) {
            super(itemView);
            friendName = itemView.findViewById(R.id.tv_friendName);
            this.onFriendsListListener = onFriendsListListener;
            this.onFriendListLClickListener = onFriendListLClickListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onFriendsListListener.onFriendClick(getAdapterPosition(), v);
        }

        @Override
        public boolean onLongClick(View v) {
            onFriendListLClickListener.onFriendClickLong(getAdapterPosition(),v);
            return true;
        }

        void bind(String nameValue){
            friendName.setText(nameValue);
        }
    }


    public interface OnFriendsListListener {
        void onFriendClick(int position, View v);
    }

    public interface OnFriendListLClickListener{
        void onFriendClickLong(int position, View v);
    }
}
