package com.quanlychitieu.doan.notification;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;

public class NotificationDetailActivity
        extends AppCompatActivity {

    public static final String EXTRA_TITLE =
            "notification_title";

    public static final String EXTRA_MESSAGE =
            "notification_message";

    public static final String EXTRA_TIME =
            "notification_time";

    public static final String EXTRA_TYPE =
            "notification_type";

    public static final String EXTRA_READ =
            "notification_read";

    private ImageView imgBack;
    private ImageView imgDetailIcon;

    private FrameLayout layoutDetailIcon;

    private TextView tvDetailType;
    private TextView tvDetailTitle;
    private TextView tvDetailMessage;
    private TextView tvDetailTime;
    private TextView tvDetailStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(
                R.layout.activity_notification_detail
        );

        initViews();
        setupHeaderInsets();
        loadNotificationData();
        setupEvents();
    }

    private void initViews() {
        imgBack =
                findViewById(R.id.imgBack);

        imgDetailIcon =
                findViewById(R.id.imgDetailIcon);

        layoutDetailIcon =
                findViewById(R.id.layoutDetailIcon);

        tvDetailType =
                findViewById(R.id.tvDetailType);

        tvDetailTitle =
                findViewById(R.id.tvDetailTitle);

        tvDetailMessage =
                findViewById(R.id.tvDetailMessage);

        tvDetailTime =
                findViewById(R.id.tvDetailTime);

        tvDetailStatus =
                findViewById(R.id.tvDetailStatus);
    }

    private void setupHeaderInsets() {
        View header =
                findViewById(
                        R.id.headerNotificationDetail
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

    private void loadNotificationData() {
        String title =
                getIntent().getStringExtra(
                        EXTRA_TITLE
                );

        String message =
                getIntent().getStringExtra(
                        EXTRA_MESSAGE
                );

        String time =
                getIntent().getStringExtra(
                        EXTRA_TIME
                );

        String type =
                getIntent().getStringExtra(
                        EXTRA_TYPE
                );

        boolean isRead =
                getIntent().getBooleanExtra(
                        EXTRA_READ,
                        true
                );

        if (title == null
                || title.trim().isEmpty()) {

            title = "Thông báo";
        }

        if (message == null
                || message.trim().isEmpty()) {

            message = "Không có nội dung.";
        }

        if (time == null
                || time.trim().isEmpty()) {

            time = "Không xác định";
        }

        tvDetailTitle.setText(title);
        tvDetailMessage.setText(message);
        tvDetailTime.setText(time);

        tvDetailStatus.setText(
                isRead
                        ? "Đã đọc"
                        : "Chưa đọc"
        );

        updateAppearance(type);
    }

    private void updateAppearance(String type) {
        int backgroundColor;
        int labelColor;
        int iconResource;
        String typeText;

        if (NotificationModel.TYPE_ALERT.equals(type)) {
            backgroundColor =
                    ContextCompat.getColor(
                            this,
                            R.color.delete_color
                    );

            labelColor =
                    ContextCompat.getColor(
                            this,
                            R.color.delete_color
                    );

            iconResource =
                    R.drawable.ic_warning;

            typeText =
                    "CẢNH BÁO";

        } else {
            backgroundColor =
                    ContextCompat.getColor(
                            this,
                            R.color.color_primary
                    );

            labelColor =
                    ContextCompat.getColor(
                            this,
                            R.color.color_primary
                    );

            iconResource =
                    R.drawable.ic_bell;

            typeText =
                    "NHẮC NHỞ";
        }

        GradientDrawable background =
                new GradientDrawable();

        background.setShape(
                GradientDrawable.OVAL
        );

        background.setColor(
                backgroundColor
        );

        layoutDetailIcon.setBackground(
                background
        );

        imgDetailIcon.setImageResource(
                iconResource
        );

        imgDetailIcon.setColorFilter(
                Color.WHITE
        );

        tvDetailType.setText(
                typeText
        );

        tvDetailType.setTextColor(
                labelColor
        );
    }

    private void setupEvents() {
        imgBack.setOnClickListener(
                view -> finish()
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