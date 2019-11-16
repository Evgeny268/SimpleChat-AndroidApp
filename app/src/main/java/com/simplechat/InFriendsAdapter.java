package com.simplechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import transfers.User;

public class InFriendsAdapter extends RecyclerView.Adapter<InFriendsAdapter.InFriendHolder> {

    private  ArrayList<User> requests;
    private RequestInControl mRequestInControl;

    public InFriendsAdapter(ArrayList<User> requests,RequestInControl requestInControl) {
        this.requests = requests;
        this.mRequestInControl = requestInControl;
    }

    @NonNull
    @Override
    public InFriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutId = R.layout.friend_inrequest;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutId, parent,false);
        InFriendHolder viewHolder = new InFriendHolder(view, mRequestInControl);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull InFriendHolder holder, int position) {
        holder.bind(requests.get(position));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class InFriendHolder extends RecyclerView.ViewHolder{

        private User user;
        private RequestInControl requestInControl;
        private TextView nickname;
        private Button accept;
        private Button reject;
        public InFriendHolder(@NonNull View itemView, final RequestInControl requestInControl) {
            super(itemView);
            this.requestInControl = requestInControl;
            nickname = itemView.findViewById(R.id.inRequestNickname);
            accept = itemView.findViewById(R.id.acceptRequestIn);
            reject = itemView.findViewById(R.id.rejectRequestIn);
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = requests.get(getAdapterPosition());
                    requestInControl.onClickAccept(user);
                }
            });
            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = requests.get(getAdapterPosition());
                    requestInControl.onClickReject(user);
                }
            });
        }

        void bind(User user){
            this.user = new User(user);
            nickname.setText(user.login);
        }
    }

    public interface RequestInControl{

        void onClickAccept(User user);

        void onClickReject(User user);
    }
}
