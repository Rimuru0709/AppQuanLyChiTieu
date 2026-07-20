package com.quanlychitieu.doan.notification;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private static final String FILTER_ALL = "ALL";

    private ImageView imgBack;
    private TextView tvMarkAllRead;

    private MaterialButtonToggleGroup toggleNotificationFilter;

    private RecyclerView recyclerNotifications;
    private LinearLayout layoutEmptyNotification;

    private NotificationAdapter notificationAdapter;

    private DatabaseHelper dbHelper;

    private final List<NotificationModel> allNotifications =
            new ArrayList<>();

    private final List<NotificationModel> filteredNotifications =
            new ArrayList<>();

    private String currentFilter = FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupHeaderInsets();
        setupRecyclerView();
        setupEvents();

        loadNotificationsFromDatabase();
        applyFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (dbHelper != null
                && notificationAdapter != null) {

            loadNotificationsFromDatabase();
            applyFilter();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void initViews() {
        imgBack =
                findViewById(R.id.imgBack);

        tvMarkAllRead =
                findViewById(R.id.tvMarkAllRead);

        toggleNotificationFilter =
                findViewById(
                        R.id.toggleNotificationFilter
                );

        recyclerNotifications =
                findViewById(
                        R.id.recyclerNotifications
                );

        layoutEmptyNotification =
                findViewById(
                        R.id.layoutEmptyNotification
                );
    }

    private void setupHeaderInsets() {
        View header =
                findViewById(
                        R.id.headerNotification
                );

        if (header == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                header,
                (view, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat
                                            .Type
                                            .systemBars()
                            );

                    view.setPadding(
                            dpToPx(12),
                            systemBars.top,
                            dpToPx(12),
                            0
                    );

                    view.getLayoutParams().height =
                            dpToPx(64)
                                    + systemBars.top;

                    view.requestLayout();

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(header);
    }

    private void setupRecyclerView() {
        notificationAdapter =
                new NotificationAdapter(
                        filteredNotifications,
                        new NotificationAdapter
                                .OnNotificationClickListener() {

                            @Override
                            public void onNotificationClick(
                                    NotificationModel notification,
                                    int position
                            ) {
                                onNotificationClicked(
                                        notification,
                                        position
                                );
                            }

                            @Override
                            public void onNotificationLongClick(
                                    NotificationModel notification,
                                    int position
                            ) {
                                showDeleteNotificationDialog(
                                        notification
                                );
                            }
                        }
                );

        recyclerNotifications.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerNotifications.setHasFixedSize(false);

        recyclerNotifications.setAdapter(
                notificationAdapter
        );
    }

    private void setupEvents() {
        if (imgBack != null) {
            imgBack.setOnClickListener(
                    view -> finish()
            );
        }

        if (tvMarkAllRead != null) {
            tvMarkAllRead.setOnClickListener(
                    view -> markAllNotificationsAsRead()
            );
        }

        if (toggleNotificationFilter != null) {
            toggleNotificationFilter
                    .addOnButtonCheckedListener(
                            (group,
                             checkedId,
                             isChecked) -> {

                                if (!isChecked) {
                                    return;
                                }

                                if (checkedId
                                        == R.id.btnFilterReminder) {

                                    currentFilter =
                                            NotificationModel
                                                    .TYPE_REMINDER;

                                } else if (checkedId
                                        == R.id.btnFilterAlert) {

                                    currentFilter =
                                            NotificationModel
                                                    .TYPE_ALERT;

                                } else {
                                    currentFilter =
                                            FILTER_ALL;
                                }

                                applyFilter();
                            }
                    );
        }
    }

    private void loadNotificationsFromDatabase() {
        allNotifications.clear();

        Cursor cursor = null;

        try {
            cursor =
                    dbHelper.getAllNotifications();

            if (cursor == null) {
                return;
            }

            int idIndex =
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.NOTIFICATION_ID
                    );

            int titleIndex =
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.NOTIFICATION_TITLE
                    );

            int messageIndex =
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.NOTIFICATION_MESSAGE
                    );

            int typeIndex =
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.NOTIFICATION_TYPE
                    );

            int createdAtIndex =
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.NOTIFICATION_CREATED_AT
                    );

            int isReadIndex =
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.NOTIFICATION_IS_READ
                    );

            while (cursor.moveToNext()) {
                int id =
                        cursor.getInt(idIndex);

                String title =
                        cursor.getString(titleIndex);

                String message =
                        cursor.getString(messageIndex);

                String type =
                        cursor.getString(typeIndex);

                String createdAt =
                        cursor.getString(createdAtIndex);

                boolean isRead =
                        cursor.getInt(isReadIndex) == 1;

                allNotifications.add(
                        new NotificationModel(
                                id,
                                title,
                                message,
                                type,
                                createdAt,
                                isRead
                        )
                );
            }

        } catch (Exception exception) {
            Toast.makeText(
                    this,
                    "Không thể tải danh sách thông báo",
                    Toast.LENGTH_SHORT
            ).show();

            exception.printStackTrace();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void applyFilter() {
        filteredNotifications.clear();

        for (NotificationModel notification
                : allNotifications) {

            if (FILTER_ALL.equals(currentFilter)) {
                filteredNotifications.add(
                        notification
                );

            } else if (currentFilter.equals(
                    notification.getType()
            )) {
                filteredNotifications.add(
                        notification
                );
            }
        }

        if (notificationAdapter != null) {
            notificationAdapter.updateData(
                    filteredNotifications
            );
        }

        updateEmptyState();
        updateMarkAllReadButton();
    }

    private void onNotificationClicked(
            NotificationModel notification,
            int position
    ) {
        if (notification == null) {
            return;
        }

        if (!notification.isRead()) {
            dbHelper.markNotificationAsRead(
                    notification.getId()
            );

            notification.setRead(true);

            if (notificationAdapter != null) {
                notificationAdapter.notifyItemChanged(
                        position
                );
            }
        }

        Intent intent =
                new Intent(
                        NotificationActivity.this,
                        NotificationDetailActivity.class
                );

        intent.putExtra(
                NotificationDetailActivity.EXTRA_TITLE,
                notification.getTitle()
        );

        intent.putExtra(
                NotificationDetailActivity.EXTRA_MESSAGE,
                notification.getMessage()
        );

        intent.putExtra(
                NotificationDetailActivity.EXTRA_TIME,
                notification.getCreatedAt()
        );

        intent.putExtra(
                NotificationDetailActivity.EXTRA_TYPE,
                notification.getType()
        );

        intent.putExtra(
                NotificationDetailActivity.EXTRA_READ,
                true
        );

        startActivity(intent);

        updateMarkAllReadButton();
    }

    private void showDeleteNotificationDialog(
            NotificationModel notification
    ) {
        if (notification == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa thông báo")
                .setMessage(
                        "Bạn có chắc muốn xóa thông báo này không?"
                )
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> {

                            int deletedRows =
                                    dbHelper.deleteNotification(
                                            notification.getId()
                                    );

                            if (deletedRows > 0) {
                                loadNotificationsFromDatabase();
                                applyFilter();

                                Toast.makeText(
                                        this,
                                        "Đã xóa thông báo",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } else {
                                Toast.makeText(
                                        this,
                                        "Không thể xóa thông báo",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .setNegativeButton(
                        "Hủy",
                        null
                )
                .show();
    }

    private void markAllNotificationsAsRead() {
        boolean hasUnreadNotification =
                false;

        for (NotificationModel notification
                : allNotifications) {

            if (!notification.isRead()) {
                hasUnreadNotification = true;
                break;
            }
        }

        if (!hasUnreadNotification) {
            Toast.makeText(
                    this,
                    "Tất cả thông báo đã được đọc",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int updatedRows =
                dbHelper.markAllNotificationsAsRead();

        for (NotificationModel notification
                : allNotifications) {

            notification.setRead(true);
        }

        applyFilter();

        if (updatedRows > 0) {
            Toast.makeText(
                    this,
                    "Đã đánh dấu tất cả là đã đọc",
                    Toast.LENGTH_SHORT
            ).show();

        } else {
            Toast.makeText(
                    this,
                    "Không có thông báo nào được cập nhật",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void updateEmptyState() {
        boolean isEmpty =
                filteredNotifications.isEmpty();

        recyclerNotifications.setVisibility(
                isEmpty
                        ? View.GONE
                        : View.VISIBLE
        );

        layoutEmptyNotification.setVisibility(
                isEmpty
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void updateMarkAllReadButton() {
        if (tvMarkAllRead == null) {
            return;
        }

        boolean hasUnreadNotification =
                false;

        for (NotificationModel notification
                : allNotifications) {

            if (!notification.isRead()) {
                hasUnreadNotification = true;
                break;
            }
        }

        tvMarkAllRead.setEnabled(
                hasUnreadNotification
        );

        tvMarkAllRead.setClickable(
                hasUnreadNotification
        );

        tvMarkAllRead.setAlpha(
                hasUnreadNotification
                        ? 1.0f
                        : 0.45f
        );
    }

    private int dpToPx(int dp) {
        return Math.round(
                dp
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}