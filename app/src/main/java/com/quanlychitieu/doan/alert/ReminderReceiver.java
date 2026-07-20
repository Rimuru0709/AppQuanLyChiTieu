package com.quanlychitieu.doan.alert;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.database.DatabaseHelper;
import com.quanlychitieu.doan.home.HomeActivity;
import com.quanlychitieu.doan.notification.NotificationModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID =
            "expense_reminder_channel";

    public static final String EXTRA_NOTIFICATION_CONTENT =
            "notification_content";

    public static final String EXTRA_DAY_INDEX =
            "day_index";

    private static final String PREF_REMINDER =
            "ReminderPreference";

    private static final String KEY_ENABLED =
            "reminder_enabled";

    private static final String KEY_HOUR =
            "reminder_hour";

    private static final String KEY_MINUTE =
            "reminder_minute";

    private static final String KEY_WEEKLY_DAYS =
            "reminder_weekly_days";

    private static final String KEY_CONTENT =
            "reminder_content";

    private static final int REMINDER_REQUEST_CODE_BASE = 100;

    private static final String NOTIFICATION_TITLE =
            "Nhắc nhở ghi chép chi tiêu";

    private static final String DEFAULT_CONTENT =
            "Đừng quên cập nhật các khoản thu chi hôm nay.";

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {
        int dayIndex =
                intent.getIntExtra(
                        EXTRA_DAY_INDEX,
                        -1
                );

        String content =
                intent.getStringExtra(
                        EXTRA_NOTIFICATION_CONTENT
                );

        if (content == null
                || content.trim().isEmpty()) {

            content = DEFAULT_CONTENT;
        }

        /*
         * Lưu lịch sử thông báo vào SQLite trước.
         */
        saveNotificationToDatabase(
                context,
                content
        );

        /*
         * Hiển thị thông báo ngoài hệ thống.
         */
        showNotification(
                context,
                content,
                dayIndex
        );

        /*
         * Đặt lại lịch cho tuần tiếp theo.
         */
        scheduleNextWeekIfNeeded(
                context,
                dayIndex
        );
    }

    private void saveNotificationToDatabase(
            Context context,
            String content
    ) {
        DatabaseHelper dbHelper = null;

        try {
            dbHelper =
                    new DatabaseHelper(
                            context.getApplicationContext()
                    );

            dbHelper.insertNotification(
                    NOTIFICATION_TITLE,
                    content,
                    NotificationModel.TYPE_REMINDER,
                    formatCurrentTime()
            );

        } catch (Exception exception) {
            exception.printStackTrace();

        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }

    private String formatCurrentTime() {
        SimpleDateFormat timeFormat =
                new SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                );

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                );

        Date now = new Date();

        return timeFormat.format(now)
                + " - "
                + dateFormat.format(now);
    }

    private void showNotification(
            Context context,
            String content,
            int dayIndex
    ) {
        createNotificationChannel(context);

        Intent openAppIntent =
                new Intent(
                        context,
                        HomeActivity.class
                );

        openAppIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent contentPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                )
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(
                                NOTIFICATION_TITLE
                        )
                        .setContentText(content)
                        .setStyle(
                                new NotificationCompat
                                        .BigTextStyle()
                                        .bigText(content)
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setCategory(
                                NotificationCompat.CATEGORY_REMINDER
                        )
                        .setAutoCancel(true)
                        .setContentIntent(
                                contentPendingIntent
                        );

        NotificationManagerCompat manager =
                NotificationManagerCompat.from(
                        context
                );

        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        int notificationId =
                dayIndex >= 0
                        ? 2000 + dayIndex
                        : 2000;

        manager.notify(
                notificationId,
                builder.build()
        );
    }

    private void scheduleNextWeekIfNeeded(
            Context context,
            int dayIndex
    ) {
        if (dayIndex < 0 || dayIndex > 6) {
            return;
        }

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREF_REMINDER,
                        Context.MODE_PRIVATE
                );

        boolean isEnabled =
                preferences.getBoolean(
                        KEY_ENABLED,
                        false
                );

        if (!isEnabled) {
            return;
        }

        String savedDays =
                preferences.getString(
                        KEY_WEEKLY_DAYS,
                        ""
                );

        if (!containsSelectedDay(
                savedDays,
                dayIndex
        )) {
            return;
        }

        int hour =
                preferences.getInt(
                        KEY_HOUR,
                        20
                );

        int minute =
                preferences.getInt(
                        KEY_MINUTE,
                        0
                );

        String content =
                preferences.getString(
                        KEY_CONTENT,
                        DEFAULT_CONTENT
                );

        if (content == null
                || content.trim().isEmpty()) {

            content = DEFAULT_CONTENT;
        }

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.S
                && !alarmManager.canScheduleExactAlarms()) {

            return;
        }

        Intent nextIntent =
                new Intent(
                        context,
                        ReminderReceiver.class
                );

        nextIntent.putExtra(
                EXTRA_NOTIFICATION_CONTENT,
                content
        );

        nextIntent.putExtra(
                EXTRA_DAY_INDEX,
                dayIndex
        );

        PendingIntent nextPendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        REMINDER_REQUEST_CODE_BASE + dayIndex,
                        nextIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        Calendar nextTrigger =
                Calendar.getInstance();

        nextTrigger.add(
                Calendar.WEEK_OF_YEAR,
                1
        );

        nextTrigger.set(
                Calendar.DAY_OF_WEEK,
                convertDayIndexToCalendarDay(
                        dayIndex
                )
        );

        nextTrigger.set(
                Calendar.HOUR_OF_DAY,
                hour
        );

        nextTrigger.set(
                Calendar.MINUTE,
                minute
        );

        nextTrigger.set(
                Calendar.SECOND,
                0
        );

        nextTrigger.set(
                Calendar.MILLISECOND,
                0
        );

        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.M) {

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTrigger.getTimeInMillis(),
                    nextPendingIntent
            );

        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextTrigger.getTimeInMillis(),
                    nextPendingIntent
            );
        }
    }

    private boolean containsSelectedDay(
            String savedDays,
            int requiredDayIndex
    ) {
        if (savedDays == null
                || savedDays.trim().isEmpty()) {

            return false;
        }

        String[] values =
                savedDays.split(",");

        for (String value : values) {
            try {
                int savedIndex =
                        Integer.parseInt(
                                value.trim()
                        );

                if (savedIndex == requiredDayIndex) {
                    return true;
                }

            } catch (NumberFormatException ignored) {
                // Bỏ qua dữ liệu sai.
            }
        }

        return false;
    }

    private int convertDayIndexToCalendarDay(
            int dayIndex
    ) {
        switch (dayIndex) {
            case 0:
                return Calendar.MONDAY;

            case 1:
                return Calendar.TUESDAY;

            case 2:
                return Calendar.WEDNESDAY;

            case 3:
                return Calendar.THURSDAY;

            case 4:
                return Calendar.FRIDAY;

            case 5:
                return Calendar.SATURDAY;

            case 6:
                return Calendar.SUNDAY;

            default:
                return Calendar.MONDAY;
        }
    }

    private void createNotificationChannel(
            Context context
    ) {
        if (Build.VERSION.SDK_INT
                < Build.VERSION_CODES.O) {

            return;
        }

        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL_ID,
                        "Nhắc nhở chi tiêu",
                        NotificationManager.IMPORTANCE_HIGH
                );

        channel.setDescription(
                "Thông báo nhắc người dùng ghi chép thu chi"
        );

        channel.enableVibration(true);

        NotificationManager notificationManager =
                context.getSystemService(
                        NotificationManager.class
                );

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(
                    channel
            );
        }
    }
}