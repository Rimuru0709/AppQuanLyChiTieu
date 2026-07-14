package com.quanlychitieu.doan.notification;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.quanlychitieu.doan.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<
        NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {

        void onNotificationClick(
                NotificationModel notification,
                int position
        );

        void onNotificationLongClick(
                NotificationModel notification,
                int position
        );
    }

    private final List<NotificationModel> notificationList;
    private final OnNotificationClickListener listener;

    public NotificationAdapter(
            List<NotificationModel> notificationList,
            OnNotificationClickListener listener
    ) {
        this.notificationList = new ArrayList<>();

        if (notificationList != null) {
            this.notificationList.addAll(
                    notificationList
            );
        }

        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_notification,
                        parent,
                        false
                );

        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull NotificationViewHolder holder,
            int position
    ) {
        NotificationModel notification =
                notificationList.get(position);

        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void updateData(
            List<NotificationModel> newList
    ) {
        notificationList.clear();

        if (newList != null) {
            notificationList.addAll(newList);
        }

        notifyDataSetChanged();
    }

    public void markItemAsRead(
            int position
    ) {
        if (position < 0
                || position >= notificationList.size()) {

            return;
        }

        notificationList
                .get(position)
                .setRead(true);

        notifyItemChanged(position);
    }

    public void markAllAsRead() {
        for (NotificationModel notification
                : notificationList) {

            notification.setRead(true);
        }

        notifyDataSetChanged();
    }

    public List<NotificationModel> getNotificationList() {
        return notificationList;
    }

    class NotificationViewHolder
            extends RecyclerView.ViewHolder {

        private final MaterialCardView cardNotification;
        private final FrameLayout layoutNotificationIcon;

        private final ImageView imgNotificationIcon;

        private final TextView tvNotificationTitle;
        private final TextView tvNotificationMessage;
        private final TextView tvNotificationTime;

        private final View viewUnread;

        public NotificationViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            cardNotification =
                    itemView.findViewById(
                            R.id.cardNotification
                    );

            layoutNotificationIcon =
                    itemView.findViewById(
                            R.id.layoutNotificationIcon
                    );

            imgNotificationIcon =
                    itemView.findViewById(
                            R.id.imgNotificationIcon
                    );

            tvNotificationTitle =
                    itemView.findViewById(
                            R.id.tvNotificationTitle
                    );

            tvNotificationMessage =
                    itemView.findViewById(
                            R.id.tvNotificationMessage
                    );

            tvNotificationTime =
                    itemView.findViewById(
                            R.id.tvNotificationTime
                    );

            viewUnread =
                    itemView.findViewById(
                            R.id.viewUnread
                    );
        }

        private void bind(
                NotificationModel notification
        ) {
            tvNotificationTitle.setText(
                    notification.getTitle()
            );

            tvNotificationMessage.setText(
                    notification.getMessage()
            );

            tvNotificationTime.setText(
                    notification.getCreatedAt()
            );

            viewUnread.setVisibility(
                    notification.isRead()
                            ? View.GONE
                            : View.VISIBLE
            );

            updateNotificationAppearance(
                    notification
            );

            /*
             * Bấm bình thường:
             * mở màn hình chi tiết.
             */
            cardNotification.setOnClickListener(
                    view -> {

                        int position =
                                getBindingAdapterPosition();

                        if (position
                                == RecyclerView.NO_POSITION) {

                            return;
                        }

                        if (listener != null) {
                            listener.onNotificationClick(
                                    notificationList.get(
                                            position
                                    ),
                                    position
                            );
                        }
                    }
            );

            /*
             * Nhấn giữ:
             * mở hộp thoại xóa thông báo.
             */
            cardNotification.setOnLongClickListener(
                    view -> {

                        int position =
                                getBindingAdapterPosition();

                        if (position
                                == RecyclerView.NO_POSITION) {

                            return false;
                        }

                        if (listener != null) {
                            listener.onNotificationLongClick(
                                    notificationList.get(
                                            position
                                    ),
                                    position
                            );
                        }

                        return true;
                    }
            );
        }

        private void updateNotificationAppearance(
                NotificationModel notification
        ) {
            int iconResource;
            int backgroundColor;

            if (notification.isAlert()) {
                iconResource =
                        R.drawable.ic_warning;

                backgroundColor =
                        ContextCompat.getColor(
                                itemView.getContext(),
                                R.color.delete_color
                        );

            } else {
                iconResource =
                        R.drawable.ic_bell;

                backgroundColor =
                        ContextCompat.getColor(
                                itemView.getContext(),
                                R.color.color_primary
                        );
            }

            imgNotificationIcon.setImageResource(
                    iconResource
            );

            imgNotificationIcon.setColorFilter(
                    Color.WHITE
            );

            GradientDrawable background =
                    new GradientDrawable();

            background.setShape(
                    GradientDrawable.OVAL
            );

            background.setColor(
                    backgroundColor
            );

            layoutNotificationIcon.setBackground(
                    background
            );

            cardNotification.setAlpha(
                    notification.isRead()
                            ? 0.75f
                            : 1.0f
            );
        }
    }
}