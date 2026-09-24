package com.quanlychitieu.doan.category;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;

import com.quanlychitieu.doan.R;

import java.util.Arrays;
import java.util.List;

public class IconPickerActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_TYPE =
            "icon_picker_category_type";

    public static final String EXTRA_SELECTED_ICON =
            "icon_picker_selected_icon";

    public static final String EXTRA_SELECTED_COLOR =
            "icon_picker_selected_color";

    private static final String TYPE_INCOME = "INCOME";

    private final List<IconGroup> expenseGroups = Arrays.asList(
            new IconGroup(
                    "Ăn uống",
                    Arrays.asList(
                            "ic_picker_utensils",
                            "ic_picker_coffee",
                            "ic_picker_pizza",
                            "ic_picker_cake_slice",
                            "ic_picker_ice_cream_bowl",
                            "ic_picker_wine"
                    )
            ),
            new IconGroup(
                    "Mua sắm",
                    Arrays.asList(
                            "ic_picker_shopping_cart",
                            "ic_picker_shopping_bag",
                            "ic_picker_shirt",
                            "ic_picker_gift",
                            "ic_picker_smartphone",
                            "ic_picker_tag"
                    )
            ),
            new IconGroup(
                    "Di chuyển",
                    Arrays.asList(
                            "ic_picker_car_front",
                            "ic_picker_bus_front",
                            "ic_picker_bike",
                            "ic_picker_plane",
                            "ic_picker_train_front",
                            "ic_picker_fuel"
                    )
            ),
            new IconGroup(
                    "Nhà cửa",
                    Arrays.asList(
                            "ic_picker_house",
                            "ic_picker_lamp",
                            "ic_picker_sofa",
                            "ic_picker_bed",
                            "ic_picker_washing_machine",
                            "ic_picker_wrench"
                    )
            ),
            new IconGroup(
                    "Sức khỏe",
                    Arrays.asList(
                            "ic_picker_heart_pulse",
                            "ic_picker_hospital",
                            "ic_picker_pill",
                            "ic_picker_syringe",
                            "ic_picker_stethoscope",
                            "ic_picker_dumbbell"
                    )
            ),
            new IconGroup(
                    "Giải trí và học tập",
                    Arrays.asList(
                            "ic_picker_gamepad_2",
                            "ic_picker_film",
                            "ic_picker_music",
                            "ic_picker_camera",
                            "ic_picker_ticket",
                            "ic_picker_book_open"
                    )
            )
    );

    private final List<IconGroup> incomeGroups = Arrays.asList(
            new IconGroup(
                    "Thu nhập",
                    Arrays.asList(
                            "ic_picker_wallet_cards",
                            "ic_picker_banknote",
                            "ic_picker_circle_dollar_sign",
                            "ic_picker_coins",
                            "ic_picker_hand_coins",
                            "ic_picker_badge_dollar_sign"
                    )
            ),
            new IconGroup(
                    "Công việc và đầu tư",
                    Arrays.asList(
                            "ic_picker_briefcase_business",
                            "ic_picker_chart_no_axes_column_increasing",
                            "ic_picker_trending_up",
                            "ic_picker_landmark",
                            "ic_picker_piggy_bank",
                            "ic_picker_trophy"
                    )
            )
    );

    private LinearLayout iconGroupContainer;
    private String selectedIcon;
    private String selectedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_picker);

        View root = findViewById(R.id.iconPickerRoot);
        TextView btnBack = findViewById(R.id.btnIconPickerBack);
        iconGroupContainer = findViewById(R.id.iconGroupContainer);

        String selectedType = getIntent().getStringExtra(
                EXTRA_CATEGORY_TYPE
        );

        selectedIcon = getIntent().getStringExtra(
                EXTRA_SELECTED_ICON
        );

        selectedColor = getIntent().getStringExtra(
                EXTRA_SELECTED_COLOR
        );

        if (selectedColor == null || selectedColor.trim().isEmpty()) {
            selectedColor = "#64A8F4";
        }

        btnBack.setOnClickListener(view -> finish());

        setupSafeArea(root);
        createGroups(
                TYPE_INCOME.equals(selectedType)
                        ? incomeGroups
                        : expenseGroups
        );
    }

    private void createGroups(List<IconGroup> groups) {
        iconGroupContainer.removeAllViews();

        for (IconGroup group : groups) {
            TextView title = new TextView(this);
            title.setText(group.title);
            title.setTextColor(Color.WHITE);
            title.setTextSize(15);
            title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            title.setPadding(0, dp(12), 0, dp(8));

            iconGroupContainer.addView(title);

            GridLayout gridLayout = new GridLayout(this);
            gridLayout.setColumnCount(5);
            gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
            gridLayout.setUseDefaultMargins(false);

            LinearLayout.LayoutParams gridParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            gridParams.bottomMargin = dp(8);
            gridLayout.setLayoutParams(gridParams);

            for (String iconName : group.iconNames) {
                addIcon(gridLayout, iconName);
            }

            iconGroupContainer.addView(gridLayout);
        }
    }

    private void addIcon(GridLayout gridLayout, String iconName) {
        FrameLayout cell = new FrameLayout(this);

        GridLayout.LayoutParams cellParams = new GridLayout.LayoutParams();
        cellParams.width = 0;
        cellParams.height = dp(66);
        cellParams.columnSpec = GridLayout.spec(
                GridLayout.UNDEFINED,
                1,
                GridLayout.FILL,
                1f
        );

        cell.setLayoutParams(cellParams);

        ImageView iconView = new ImageView(this);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                dp(52),
                dp(52),
                Gravity.CENTER
        );

        iconView.setLayoutParams(iconParams);
        iconView.setPadding(dp(13), dp(13), dp(13), dp(13));
        iconView.setTag(iconName);
        iconView.setContentDescription("Chọn biểu tượng " + iconName);

        int resourceId = getResources().getIdentifier(
                iconName,
                "drawable",
                getPackageName()
        );

        if (resourceId == 0) {
            resourceId = R.drawable.ic_picker_ellipsis;
        }

        iconView.setImageResource(resourceId);
        ImageViewCompat.setImageTintList(
                iconView,
                ColorStateList.valueOf(Color.WHITE)
        );

        boolean isSelected = iconName.equals(selectedIcon);
        iconView.setBackground(createCircleBackground(isSelected));
        iconView.setAlpha(isSelected ? 1.0f : 0.82f);

        iconView.setOnClickListener(view -> returnSelectedIcon(iconName));

        cell.addView(iconView);
        gridLayout.addView(cell);
    }

    private GradientDrawable createCircleBackground(boolean isSelected) {
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);

        if (isSelected) {
            background.setColor(parseColor(selectedColor, "#64A8F4"));
            background.setStroke(dp(3), Color.WHITE);
        } else {
            background.setColor(Color.parseColor("#8FA29E"));
        }

        return background;
    }

    private int parseColor(String color, String fallback) {
        try {
            return Color.parseColor(color);
        } catch (IllegalArgumentException exception) {
            return Color.parseColor(fallback);
        }
    }

    private void returnSelectedIcon(String iconName) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_ICON, iconName);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void setupSafeArea(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(
                root,
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            0,
                            systemBars.top,
                            0,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(root);
    }

    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density
        );
    }

    private static class IconGroup {

        private final String title;
        private final List<String> iconNames;

        private IconGroup(String title, List<String> iconNames) {
            this.title = title;
            this.iconNames = iconNames;
        }
    }
}