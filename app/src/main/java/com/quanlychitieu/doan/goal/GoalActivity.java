package com.quanlychitieu.doan.goal;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.text.DecimalFormat;

public class GoalActivity extends AppCompatActivity {

    private ImageView imgBack;
    private Button btnAddGoal;
    private LinearLayout layoutGoals;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        initViews();
        setupSafeArea();
        BottomNavHelper.setup(this);
        setupDatabase();
        setupEvents();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        btnAddGoal = findViewById(R.id.btnAddGoal);
        layoutGoals = findViewById(R.id.layoutGoals);
    }

    private void setupDatabase() {
        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getReadableDatabase();
    }

    private void setupEvents() {
        imgBack.setOnClickListener(v -> finish());

        btnAddGoal.setOnClickListener(v -> {
            Intent intent = new Intent(
                    GoalActivity.this,
                    AddGoalActivity.class
            );

            startActivity(intent);
        });
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                content,
                (view, insets) -> {

                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            dp(20),
                            bars.top + dp(12),
                            dp(20),
                            dp(24)
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(content);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        database = dbHelper.getReadableDatabase();
        loadGoals();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (database != null && database.isOpen()) {
            database.close();
        }

        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void loadGoals() {
        if (layoutGoals == null ||
                database == null ||
                !database.isOpen()) {
            return;
        }

        layoutGoals.removeAllViews();

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT id, name, targetAmount, savedAmount, " +
                            "deadline, wallet, autoSave " +
                            "FROM goals ORDER BY id DESC",
                    null
            );

            if (cursor.getCount() == 0) {
                showEmptyMessage();
                return;
            }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int targetAmount = cursor.getInt(2);
                int savedAmount = cursor.getInt(3);
                String deadline = cursor.getString(4);
                String wallet = cursor.getString(5);
                int autoSave = cursor.getInt(6);

                addGoalCard(
                        id,
                        name,
                        targetAmount,
                        savedAmount,
                        deadline,
                        wallet,
                        autoSave
                );
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void showEmptyMessage() {
        TextView empty = new TextView(this);

        empty.setText(
                "Chưa có mục tiêu tiết kiệm nào\n" +
                        "Hãy bấm + Thêm mục tiêu để bắt đầu"
        );

        empty.setTextSize(16);
        empty.setGravity(Gravity.CENTER);

        empty.setPadding(
                0,
                dp(50),
                0,
                dp(50)
        );

        empty.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_secondary
                )
        );

        layoutGoals.addView(empty);
    }

    private void addGoalCard(
            int id,
            String name,
            int targetAmount,
            int savedAmount,
            String deadline,
            String wallet,
            int autoSave
    ) {
        int percent = 0;

        if (targetAmount > 0) {
            percent = (int) (
                    savedAmount * 100.0 / targetAmount
            );
        }

        percent = Math.min(percent, 100);

        int remain = Math.max(
                targetAmount - savedAmount,
                0
        );

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                dp(16)
        );

        card.setLayoutParams(cardParams);

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                ContextCompat.getColor(
                        this,
                        R.color.card_background
                )
        );

        background.setCornerRadius(
                dp(22)
        );

        background.setStroke(
                dp(1),
                ContextCompat.getColor(
                        this,
                        R.color.divider_color
                )
        );

        card.setBackground(background);
        card.setElevation(dp(3));

        TextView tvName = new TextView(this);

        tvName.setText("🎯 " + name);
        tvName.setTextSize(18);
        tvName.setTypeface(
                null,
                Typeface.BOLD
        );

        tvName.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_primary
                )
        );

        TextView tvTarget = new TextView(this);

        tvTarget.setText(
                formatMoney(targetAmount)
        );

        tvTarget.setTextSize(24);

        tvTarget.setTypeface(
                null,
                Typeface.BOLD
        );

        tvTarget.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.color_primary
                )
        );

        tvTarget.setPadding(
                0,
                dp(10),
                0,
                0
        );

        ProgressBar progressBar = new ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
        );

        progressBar.setMax(100);
        progressBar.setProgress(percent);

        progressBar.setProgressDrawable(
                ContextCompat.getDrawable(
                        this,
                        R.drawable.progress_goal
                )
        );

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(12)
                );

        progressParams.setMargins(
                0,
                dp(14),
                0,
                dp(8)
        );

        progressBar.setLayoutParams(progressParams);

        TextView tvPercent = makeInfoText(
                percent + "% hoàn thành",
                ContextCompat.getColor(
                        this,
                        R.color.color_primary
                )
        );

        tvPercent.setTypeface(
                null,
                Typeface.BOLD
        );

        TextView tvSaved = makeInfoText(
                "Đã tiết kiệm: " +
                        formatMoney(savedAmount),
                Color.parseColor("#16A34A")
        );

        TextView tvRemain = makeInfoText(
                "Còn lại: " +
                        formatMoney(remain),
                ContextCompat.getColor(
                        this,
                        R.color.delete_color
                )
        );

        String autoText =
                autoSave == 1
                        ? "Tự động: Bật"
                        : "Tự động: Tắt";

        TextView tvInfo = makeInfoText(
                "📅 " + deadline +
                        "\n💳 " + wallet +
                        "\n⚡ " + autoText,
                ContextCompat.getColor(
                        this,
                        R.color.text_secondary
                )
        );

        TextView tvEdit = makeInfoText(
                "Nhấn để xem hoặc chỉnh sửa",
                ContextCompat.getColor(
                        this,
                        R.color.color_primary
                )
        );

        tvEdit.setTypeface(
                null,
                Typeface.BOLD
        );

        card.addView(tvName);
        card.addView(tvTarget);
        card.addView(progressBar);
        card.addView(tvPercent);
        card.addView(tvSaved);
        card.addView(tvRemain);
        card.addView(tvInfo);
        card.addView(tvEdit);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(
                    GoalActivity.this,
                    AddGoalActivity.class
            );

            intent.putExtra(
                    "goalId",
                    id
            );

            startActivity(intent);
        });

        layoutGoals.addView(card);
    }

    private TextView makeInfoText(
            String text,
            int textColor
    ) {
        TextView textView = new TextView(this);

        textView.setText(text);
        textView.setTextSize(14);
        textView.setTextColor(textColor);

        textView.setPadding(
                0,
                dp(7),
                0,
                0
        );

        return textView;
    }

    private int dp(int value) {
        return Math.round(
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    private String formatMoney(int money) {
        DecimalFormat formatter =
                new DecimalFormat("#,###");

        return formatter
                .format(money)
                .replace(",", ".") +
                " đ";
    }
}