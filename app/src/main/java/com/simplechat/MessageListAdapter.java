package com.simplechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import transfers.Message;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_ME = 1;
    private static final int VIEW_TYPE_MESSAGE_FRIEND = 2;

    private Context mContext;
    private ArrayList<Message> messagesList;
    private int friendId;

    public MessageListAdapter(Context mContext, ArrayList<Message> messagesList, int friendId) {
        this.mContext = mContext;
        this.messagesList = messagesList;
        this.friendId = friendId;
    }

    public void setMessagesList(ArrayList<Message> messagesList) {
        this.messagesList = messagesList;
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messagesList.get(position);
        if (message.iduser==friendId){
            return VIEW_TYPE_MESSAGE_FRIEND;
        }else {
            return VIEW_TYPE_MESSAGE_ME;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_ME){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_message,parent,false);
        }else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_message,parent,false);
        }
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messagesList.get(position);
        ((MessageHolder) holder).bind(message);
    }

    private class MessageHolder extends RecyclerView.ViewHolder{

        TextView messageText;
        TextView messageDate;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_body);
            messageDate = itemView.findViewById(R.id.message_time);
        }

        void bind(Message message){
            messageText.setText(message.text);
            String dateStr = dateToString(message.date);
            messageDate.setText(dateStr);
        }
    }


    private static String dateToString(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar currentCallendar = Calendar.getInstance();
        currentCallendar.setTime(new Date());
        boolean sameDay = calendar.get(Calendar.DAY_OF_YEAR) == currentCallendar.get(Calendar.DAY_OF_YEAR) &&
                calendar.get(Calendar.YEAR) == currentCallendar.get(Calendar.YEAR);
        if (sameDay){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            return simpleDateFormat.format(date);
        }else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            return simpleDateFormat.format(date);
        }
    }
}
