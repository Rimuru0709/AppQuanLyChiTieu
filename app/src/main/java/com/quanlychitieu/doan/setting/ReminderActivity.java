package com.quanlychitieu.doan.setting;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.alert.ReminderReceiver;

import java.util.Calendar;
import java.util.Locale;

public class ReminderActivity extends AppCompatActivity {

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

    private static final String DEFAULT_CONTENT =
            "Đừng quên cập nhật các khoản thu chi hôm nay.";

    private static final int REMINDER_REQUEST_CODE_BASE = 100;

    private TextView btnBack;
    private TextView tvReminderStatus;
    private TextView tvReminderTime;
    private TextView tvRepeatValue;
    private TextView tvNotificationContent;

    private SwitchCompat switchReminder;

    private MaterialCardView layoutSelectTime;
    private MaterialCardView layoutRepeat;
    private MaterialCardView layoutNotificationContent;

    private MaterialButton btnSaveReminder;

    private SharedPreferences reminderPreferences;

    private ActivityResultLauncher<String>
            notificationPermissionLauncher;

    private int selectedHour = 20;
    private int selectedMinute = 0;

    /*
     * 0 = Thứ Hai
     * 1 = Thứ Ba
     * 2 = Thứ Tư
     * 3 = Thứ Năm
     * 4 = Thứ Sáu
     * 5 = Thứ Bảy
     * 6 = Chủ Nhật
     */
    private final boolean[] selectedWeekDays = {
            true,
            true,
            true,
            true,
            true,
            true,
            true
    };

    private String notificationContent =
            DEFAULT_CONTENT;

    /*
     * Dùng để biết ứng dụng vừa mở trang cấp
     * quyền Báo thức và lời nhắc.
     */
    private boolean waitingForExactAlarmPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerNotificationPermissionLauncher();

        setContentView(R.layout.activity_reminder);

        reminderPreferences = getSharedPreferences(
                PREF_REMINDER,
                MODE_PRIVATE
        );

        initViews();
        setupHeaderInsets();
        loadReminderData();
        updateAllViews();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Khi người dùng quay lại từ màn hình
         * cấp quyền Báo thức và lời nhắc.
         */
        if (waitingForExactAlarmPermission) {
            waitingForExactAlarmPermission = false;

            if (canScheduleExactAlarms()) {
                scheduleSelectedReminders();

                Toast.makeText(
                        this,
                        "Đã cấp quyền và đặt lịch nhắc nhở",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        this,
                        "Chưa được cấp quyền Báo thức và lời nhắc",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private void registerNotificationPermissionLauncher() {
        notificationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                checkExactAlarmPermissionAndSchedule();
                            } else {
                                Toast.makeText(
                                        ReminderActivity.this,
                                        "Bạn cần cấp quyền thông báo để nhận nhắc nhở",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void initViews() {
        btnBack =
                findViewById(R.id.btnBack);

        tvReminderStatus =
                findViewById(R.id.tvReminderStatus);

        tvReminderTime =
                findViewById(R.id.tvReminderTime);

        tvRepeatValue =
                findViewById(R.id.tvRepeatValue);

        tvNotificationContent =
                findViewById(R.id.tvNotificationContent);

        switchReminder =
                findViewById(R.id.switchReminder);

        layoutSelectTime =
                findViewById(R.id.layoutSelectTime);

        layoutRepeat =
                findViewById(R.id.layoutRepeat);

        layoutNotificationContent =
                findViewById(R.id.layoutNotificationContent);

        btnSaveReminder =
                findViewById(R.id.btnSaveReminder);
    }

    private void setupHeaderInsets() {
        View header =
                findViewById(R.id.headerReminder);

        if (header == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                header,
                (view, insets) -> {
                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    view.setPadding(
                            dpToPx(12),
                            systemBars.top,
                            dpToPx(12),
                            0
                    );

                    view.getLayoutParams().height =
                            dpToPx(64) + systemBars.top;

                    view.requestLayout();

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(header);
    }

    private void loadReminderData() {
        boolean isEnabled =
                reminderPreferences.getBoolean(
                        KEY_ENABLED,
                        false
                );

        selectedHour =
                reminderPreferences.getInt(
                        KEY_HOUR,
                        20
                );

        selectedMinute =
                reminderPreferences.getInt(
                        KEY_MINUTE,
                        0
                );

        notificationContent =
                reminderPreferences.getString(
                        KEY_CONTENT,
                        DEFAULT_CONTENT
                );

        if (notificationContent == null
                || notificationContent.trim().isEmpty()) {

            notificationContent =
                    DEFAULT_CONTENT;
        }

        loadSelectedWeekDays();

        switchReminder.setChecked(isEnabled);
    }

    private void loadSelectedWeekDays() {
        String savedDays =
                reminderPreferences.getString(
                        KEY_WEEKLY_DAYS,
                        "0,1,2,3,4,5,6"
                );

        for (int i = 0;
             i < selectedWeekDays.length;
             i++) {

            selectedWeekDays[i] = false;
        }

        if (savedDays == null
                || savedDays.trim().isEmpty()) {

            selectAllDays();
            return;
        }

        String[] dayIndexes =
                savedDays.split(",");

        for (String dayIndex : dayIndexes) {
            try {
                int index =
                        Integer.parseInt(
                                dayIndex.trim()
                        );

                if (index >= 0
                        && index < selectedWeekDays.length) {

                    selectedWeekDays[index] = true;
                }

            } catch (NumberFormatException ignored) {
                // Bỏ qua dữ liệu không hợp lệ.
            }
        }

        if (!hasSelectedDay(selectedWeekDays)) {
            selectAllDays();
        }
    }

    private void selectAllDays() {
        for (int i = 0;
             i < selectedWeekDays.length;
             i++) {

            selectedWeekDays[i] = true;
        }
    }

    private void setupEvents() {
        btnBack.setOnClickListener(
                view -> finish()
        );

        layoutSelectTime.setOnClickListener(
                view -> showTimePicker()
        );

        layoutRepeat.setOnClickListener(
                view -> showWeeklyDaysDialog()
        );

        layoutNotificationContent.setOnClickListener(
                view -> showContentDialog()
        );

        switchReminder.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        updateReminderStatus()
        );

        btnSaveReminder.setOnClickListener(
                view -> saveReminderData()
        );
    }

    private void showTimePicker() {
        TimePickerDialog dialog =
                new TimePickerDialog(
                        this,
                        (view, hourOfDay, minute) -> {
                            selectedHour = hourOfDay;
                            selectedMinute = minute;

                            updateTimeText();
                        },
                        selectedHour,
                        selectedMinute,
                        true
                );

        dialog.setTitle("Chọn giờ nhắc");
        dialog.show();
    }

    private void showWeeklyDaysDialog() {
        String[] dayNames = {
                "Thứ Hai",
                "Thứ Ba",
                "Thứ Tư",
                "Thứ Năm",
                "Thứ Sáu",
                "Thứ Bảy",
                "Chủ Nhật"
        };

        boolean[] temporaryDays =
                selectedWeekDays.clone();

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Chọn ngày lặp lại")
                        .setMultiChoiceItems(
                                dayNames,
                                temporaryDays,
                                (dialogInterface,
                                 which,
                                 isChecked) ->

                                        temporaryDays[which] =
                                                isChecked
                        )
                        .setPositiveButton(
                                "Lưu",
                                null
                        )
                        .setNegativeButton(
                                "Hủy",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface ->

                        dialog.getButton(
                                        AlertDialog.BUTTON_POSITIVE
                                )
                                .setOnClickListener(
                                        view -> {

                                            if (!hasSelectedDay(
                                                    temporaryDays
                                            )) {
                                                Toast.makeText(
                                                        this,
                                                        "Vui lòng chọn ít nhất một ngày",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                return;
                                            }

                                            System.arraycopy(
                                                    temporaryDays,
                                                    0,
                                                    selectedWeekDays,
                                                    0,
                                                    temporaryDays.length
                                            );

                                            updateRepeatText();
                                            dialog.dismiss();
                                        }
                                )
        );

        dialog.show();
    }

    private boolean hasSelectedDay(boolean[] days) {
        for (boolean selected : days) {
            if (selected) {
                return true;
            }
        }

        return false;
    }

    private void showContentDialog() {
        EditText editText =
                new EditText(this);

        editText.setText(notificationContent);
        editText.setHint("Nhập nội dung thông báo");

        editText.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        editText.setSingleLine(false);
        editText.setMinLines(3);
        editText.setMaxLines(6);

        editText.setPadding(
                dpToPx(20),
                dpToPx(12),
                dpToPx(20),
                dpToPx(12)
        );

        if (editText.getText() != null) {
            editText.setSelection(
                    editText.getText().length()
            );
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Nội dung thông báo")
                        .setView(editText)
                        .setPositiveButton(
                                "Lưu",
                                null
                        )
                        .setNegativeButton(
                                "Hủy",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface ->

                        dialog.getButton(
                                        AlertDialog.BUTTON_POSITIVE
                                )
                                .setOnClickListener(
                                        view -> {
                                            String content =
                                                    editText
                                                            .getText()
                                                            .toString()
                                                            .trim();

                                            if (content.isEmpty()) {
                                                editText.setError(
                                                        "Nội dung không được để trống"
                                                );

                                                return;
                                            }

                                            notificationContent =
                                                    content;

                                            updateNotificationContent();
                                            dialog.dismiss();
                                        }
                                )
        );

        dialog.show();
    }

    private void saveReminderData() {
        boolean isEnabled =
                switchReminder.isChecked();

        if (isEnabled
                && !hasSelectedDay(selectedWeekDays)) {

            Toast.makeText(
                    this,
                    "Vui lòng chọn ít nhất một ngày",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        reminderPreferences.edit()
                .putBoolean(
                        KEY_ENABLED,
                        isEnabled
                )
                .putInt(
                        KEY_HOUR,
                        selectedHour
                )
                .putInt(
                        KEY_MINUTE,
                        selectedMinute
                )
                .putString(
                        KEY_WEEKLY_DAYS,
                        buildSavedWeekDays()
                )
                .putString(
                        KEY_CONTENT,
                        notificationContent
                )
                .apply();

        if (!isEnabled) {
            cancelAllReminders();

            Toast.makeText(
                    this,
                    "Đã lưu và tắt nhắc nhở",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Xin quyền thông báo trước.
         */
        if (needsNotificationPermission()) {
            notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
            );

            return;
        }

        /*
         * Sau đó kiểm tra quyền exact alarm.
         */
        checkExactAlarmPermissionAndSchedule();
    }

    private boolean needsNotificationPermission() {
        return Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED;
    }

    private void checkExactAlarmPermissionAndSchedule() {
        if (canScheduleExactAlarms()) {
            scheduleSelectedReminders();

            Toast.makeText(
                    this,
                    "Đã đặt nhắc nhở lúc "
                            + formatTime(
                            selectedHour,
                            selectedMinute
                    ),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        requestExactAlarmPermission();
    }

    private boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT
                < Build.VERSION_CODES.S) {

            return true;
        }

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(
                        Context.ALARM_SERVICE
                );

        return alarmManager != null
                && alarmManager.canScheduleExactAlarms();
    }

    /**
     * Quyền exact alarm là quyền đặc biệt,
     * không phải hộp thoại runtime thông thường.
     */
    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT
                < Build.VERSION_CODES.S) {

            scheduleSelectedReminders();
            return;
        }

        waitingForExactAlarmPermission = true;

        try {
            Intent intent =
                    new Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    );

            intent.setData(
                    Uri.parse(
                            "package:" + getPackageName()
                    )
            );

            startActivity(intent);

        } catch (Exception exception) {
            waitingForExactAlarmPermission = false;

            Toast.makeText(
                    this,
                    "Hãy vào Cài đặt và cấp quyền Báo thức và lời nhắc",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void scheduleSelectedReminders() {
        cancelAllReminders();

        for (int dayIndex = 0;
             dayIndex < selectedWeekDays.length;
             dayIndex++) {

            if (selectedWeekDays[dayIndex]) {
                scheduleReminderForDay(dayIndex);
            }
        }
    }

    /**
     * Đặt một exact alarm cho lần gần nhất.
     * Receiver sẽ đặt lại tuần tiếp theo.
     */
    private void scheduleReminderForDay(
            int dayIndex
    ) {
        AlarmManager alarmManager =
                (AlarmManager) getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        Intent receiverIntent =
                new Intent(
                        this,
                        ReminderReceiver.class
                );

        receiverIntent.putExtra(
                ReminderReceiver.EXTRA_NOTIFICATION_CONTENT,
                notificationContent
        );

        receiverIntent.putExtra(
                ReminderReceiver.EXTRA_DAY_INDEX,
                dayIndex
        );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        REMINDER_REQUEST_CODE_BASE + dayIndex,
                        receiverIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        Calendar triggerCalendar =
                createNextTriggerCalendar(
                        dayIndex
                );

        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.M) {

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerCalendar.getTimeInMillis(),
                    pendingIntent
            );

        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerCalendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    private Calendar createNextTriggerCalendar(
            int dayIndex
    ) {
        Calendar now =
                Calendar.getInstance();

        Calendar trigger =
                Calendar.getInstance();

        trigger.set(
                Calendar.HOUR_OF_DAY,
                selectedHour
        );

        trigger.set(
                Calendar.MINUTE,
                selectedMinute
        );

        trigger.set(
                Calendar.SECOND,
                0
        );

        trigger.set(
                Calendar.MILLISECOND,
                0
        );

        trigger.set(
                Calendar.DAY_OF_WEEK,
                convertDayIndexToCalendarDay(
                        dayIndex
                )
        );

        if (trigger.getTimeInMillis()
                <= now.getTimeInMillis()) {

            trigger.add(
                    Calendar.WEEK_OF_YEAR,
                    1
            );
        }

        return trigger;
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

    private void cancelAllReminders() {
        AlarmManager alarmManager =
                (AlarmManager) getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        for (int dayIndex = 0;
             dayIndex < 7;
             dayIndex++) {

            Intent receiverIntent =
                    new Intent(
                            this,
                            ReminderReceiver.class
                    );

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(
                            this,
                            REMINDER_REQUEST_CODE_BASE + dayIndex,
                            receiverIntent,
                            PendingIntent.FLAG_NO_CREATE
                                    | PendingIntent.FLAG_IMMUTABLE
                    );

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    private String buildSavedWeekDays() {
        StringBuilder result =
                new StringBuilder();

        for (int i = 0;
             i < selectedWeekDays.length;
             i++) {

            if (!selectedWeekDays[i]) {
                continue;
            }

            if (result.length() > 0) {
                result.append(",");
            }

            result.append(i);
        }

        return result.toString();
    }

    private void updateAllViews() {
        updateTimeText();
        updateReminderStatus();
        updateRepeatText();
        updateNotificationContent();
    }

    private void updateTimeText() {
        tvReminderTime.setText(
                formatTime(
                        selectedHour,
                        selectedMinute
                )
        );
    }

    private void updateReminderStatus() {
        if (switchReminder.isChecked()) {
            tvReminderStatus.setText(
                    "Nhắc nhở đang bật"
            );
        } else {
            tvReminderStatus.setText(
                    "Nhắc nhở đang tắt"
            );
        }
    }

    private void updateRepeatText() {
        tvRepeatValue.setText(
                buildWeeklyDisplayText()
        );
    }

    private String buildWeeklyDisplayText() {
        String[] shortDayNames = {
                "T2",
                "T3",
                "T4",
                "T5",
                "T6",
                "T7",
                "CN"
        };

        int selectedCount = 0;

        for (boolean selected : selectedWeekDays) {
            if (selected) {
                selectedCount++;
            }
        }

        if (selectedCount == 7) {
            return "Hằng ngày";
        }

        StringBuilder result =
                new StringBuilder();

        for (int i = 0;
             i < selectedWeekDays.length;
             i++) {

            if (!selectedWeekDays[i]) {
                continue;
            }

            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(
                    shortDayNames[i]
            );
        }

        return result.toString();
    }

    private void updateNotificationContent() {
        tvNotificationContent.setText(
                notificationContent
        );
    }

    private String formatTime(
            int hour,
            int minute
    ) {
        return String.format(
                Locale.getDefault(),
                "%02d:%02d",
                hour,
                minute
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