package com.quanlychitieu.doan.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quanlychitieu.doan.R;

import java.util.List;

public class ChatAdapter
        extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private final List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getType();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        int layoutResource;

        if (viewType == ChatMessage.TYPE_USER) {
            layoutResource = R.layout.item_chat_user;
        } else {
            layoutResource = R.layout.item_chat_ai;
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutResource, parent, false);

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MessageViewHolder holder,
            int position
    ) {
        ChatMessage chatMessage = messageList.get(position);

        holder.tvChatMessage.setText(
                chatMessage.getMessage()
        );
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addMessage(ChatMessage chatMessage) {
        messageList.add(chatMessage);

        notifyItemInserted(
                messageList.size() - 1
        );
    }

    static class MessageViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView tvChatMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            tvChatMessage = itemView.findViewById(
                    R.id.tvChatMessage
            );
        }
    }
}