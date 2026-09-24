package com.quanlychitieu.doan.category;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateCategoryActivity extends AppCompatActivity {

    private static final String TYPE_EXPENSE = "EXPENSE";
    private static final String TYPE_INCOME = "INCOME";
    private static final String ICON_MORE = "ic_picker_ellipsis";

    private final List<String> compactExpenseIcons = Arrays.asList(
            "ic_picker_utensils",
            "ic_picker_car_front",
            "ic_picker_shopping_cart"
    );

    private final List<String> compactIncomeIcons = Arrays.asList(
            "ic_picker_wallet_cards",
            "ic_picker_banknote",
            "ic_picker_briefcase_business"
    );

    private final List<String> colors = Arrays.asList(
            // Xanh dương
            "#64A8F4",
            "#2196F3",
            "#3F51B5",
            "#00BCD4",

            // Xanh lá
            "#2ECC71",
            "#009688",
            "#8BC34A",
            "#CDDC39",

            // Vàng và cam
            "#F9C74F",
            "#FFC107",
            "#FF9800",
            "#FF5722",

            // Đỏ và hồng
            "#EF4444",
            "#E91E63",
            "#D14D8B",
            "#FFB3C6",

            // Tím và nâu
            "#9C27B0",
            "#673AB7",
            "#795548",
            "#A8C3A0",

            // Màu trung tính
            "#8FA29E",
            "#607D8B",
            "#B0BEC5",
            "#A2D2FF"
    );

    private final List<ImageView> iconViews = new ArrayList<>();
    private final List<View> colorViews = new ArrayList<>();

    private EditText edtCategoryName;
    private TextView tabChiTieu;
    private TextView tabThuNhap;
    private GridLayout layoutIcons;

    private DatabaseHelper databaseHelper;

    private String selectedType = TYPE_EXPENSE;
    private String selectedIcon = "ic_picker_utensils";
    private String selectedColor = "#64A8F4";

    private final ActivityResultLauncher<Intent> iconPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK
                                || result.getData() == null) {
                            return;
                        }

                        String iconName = result.getData().getStringExtra(
                                IconPickerActivity.EXTRA_SELECTED_ICON
                        );

                        if (iconName == null || iconName.trim().isEmpty()) {
                            return;
                        }

                        selectedIcon = iconName;
                        createIconChoices();
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_category);

        setupSafeArea();

        databaseHelper = new DatabaseHelper(this);

        ImageView imgBack = findViewById(R.id.imgCreateCategoryBack);
        edtCategoryName = findViewById(R.id.edtCategoryName);
        tabChiTieu = findViewById(R.id.tabCreateExpense);
        tabThuNhap = findViewById(R.id.tabCreateIncome);
        layoutIcons = findViewById(R.id.layoutCategoryIcons);
        GridLayout layoutColors = findViewById(R.id.layoutCategoryColors);
        Button btnSave = findViewById(R.id.btnSaveCategory);

        String typeFromIntent = getIntent().getStringExtra(
                CategoryActivity.EXTRA_CATEGORY_TYPE
        );

        if (TYPE_INCOME.equals(typeFromIntent)) {
            selectedType = TYPE_INCOME;
            selectedIcon = "ic_picker_wallet_cards";
            selectedColor = "#2ECC71";
        }

        createIconChoices();
        createColorChoices(layoutColors);
        updateTypeTabs();

        imgBack.setOnClickListener(view -> finish());
        tabChiTieu.setOnClickListener(view -> changeCategoryType(TYPE_EXPENSE));
        tabThuNhap.setOnClickListener(view -> changeCategoryType(TYPE_INCOME));
        btnSave.setOnClickListener(view -> saveCategory());
    }

    private void changeCategoryType(String newType) {
        if (newType.equals(selectedType)) {
            return;
        }

        selectedType = newType;

        if (TYPE_INCOME.equals(selectedType)) {
            selectedIcon = "ic_picker_wallet_cards";
        } else {
            selectedIcon = "ic_picker_utensils";
        }

        updateTypeTabs();
        createIconChoices();
    }

    /**
     * Luôn đặt icon người dùng vừa chọn ở vị trí đầu tiên.
     * Ba vị trí còn lại gồm các icon gợi ý và nút ba chấm.
     */
    private List<String> getCompactIcons() {
        List<String> suggestions = TYPE_INCOME.equals(selectedType)
                ? compactIncomeIcons
                : compactExpenseIcons;

        List<String> visibleIcons = new ArrayList<>();
        visibleIcons.add(selectedIcon);

        for (String suggestion : suggestions) {
            if (!visibleIcons.contains(suggestion)
                    && visibleIcons.size() < 3) {
                visibleIcons.add(suggestion);
            }
        }

        visibleIcons.add(ICON_MORE);
        return visibleIcons;
    }

    private void createIconChoices() {
        iconViews.clear();
        layoutIcons.removeAllViews();
        layoutIcons.setColumnCount(4);

        for (String iconName : getCompactIcons()) {
            ImageView imageView = new ImageView(this);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(
                    GridLayout.UNDEFINED,
                    1,
                    1f
            );
            params.width = dp(62);
            params.height = dp(62);
            params.setMargins(dp(6), dp(7), dp(6), dp(7));
            params.setGravity(Gravity.CENTER);

            imageView.setLayoutParams(params);
            imageView.setPadding(dp(15), dp(15), dp(15), dp(15));
            imageView.setTag(iconName);

            int iconResource = getResources().getIdentifier(
                    iconName,
                    "drawable",
                    getPackageName()
            );

            if (iconResource == 0) {
                iconResource = R.drawable.ic_picker_ellipsis;
            }

            imageView.setImageResource(iconResource);
            ImageViewCompat.setImageTintList(
                    imageView,
                    ColorStateList.valueOf(Color.WHITE)
            );

            if (ICON_MORE.equals(iconName)) {
                imageView.setContentDescription("Mở danh sách biểu tượng");
                imageView.setOnClickListener(view -> openIconPicker());
            } else {
                imageView.setContentDescription("Chọn biểu tượng " + iconName);
                imageView.setOnClickListener(view -> {
                    selectedIcon = (String) view.getTag();
                    refreshIconSelection();
                });
            }

            iconViews.add(imageView);
            layoutIcons.addView(imageView);
        }

        refreshIconSelection();
    }

    private void openIconPicker() {
        Intent intent = new Intent(this, IconPickerActivity.class);
        intent.putExtra(
                IconPickerActivity.EXTRA_CATEGORY_TYPE,
                selectedType
        );
        intent.putExtra(
                IconPickerActivity.EXTRA_SELECTED_ICON,
                selectedIcon
        );
        intent.putExtra(
                IconPickerActivity.EXTRA_SELECTED_COLOR,
                selectedColor
        );

        iconPickerLauncher.launch(intent);
    }

    private void createColorChoices(GridLayout container) {
        colorViews.clear();
        container.removeAllViews();
        container.setColumnCount(4);

        for (String color : colors) {
            FrameLayout colorCell = new FrameLayout(this);

            GridLayout.LayoutParams cellParams = new GridLayout.LayoutParams();
            cellParams.width = 0;
            cellParams.height = dp(64);
            cellParams.columnSpec = GridLayout.spec(
                    GridLayout.UNDEFINED,
                    1,
                    GridLayout.FILL,
                    1f
            );

            colorCell.setLayoutParams(cellParams);

            View colorView = new View(this);
            FrameLayout.LayoutParams colorParams = new FrameLayout.LayoutParams(
                    dp(48),
                    dp(48),
                    Gravity.CENTER
            );

            colorView.setLayoutParams(colorParams);
            colorView.setTag(color);
            colorView.setContentDescription("Chọn màu " + color);

            colorView.setOnClickListener(view -> {
                selectedColor = (String) view.getTag();
                refreshColorSelection();
                refreshIconSelection();
            });

            colorViews.add(colorView);
            colorCell.addView(colorView);
            container.addView(colorCell);
        }

        refreshColorSelection();
    }

    private void refreshIconSelection() {
        for (ImageView imageView : iconViews) {
            String iconName = (String) imageView.getTag();
            boolean selected = !ICON_MORE.equals(iconName)
                    && selectedIcon.equals(iconName);

            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);

            if (selected) {
                background.setColor(Color.parseColor(selectedColor));
                background.setStroke(dp(3), Color.WHITE);
                imageView.setAlpha(1.0f);
            } else {
                background.setColor(Color.parseColor("#8FA29E"));
                imageView.setAlpha(
                        ICON_MORE.equals(iconName) ? 1.0f : 0.78f
                );
            }

            imageView.setBackground(background);
        }
    }

    private void refreshColorSelection() {
        for (View colorView : colorViews) {
            String color = (String) colorView.getTag();
            boolean selected = selectedColor.equals(color);

            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);
            background.setColor(Color.parseColor(color));

            if (selected) {
                background.setStroke(dp(4), Color.WHITE);
                colorView.setAlpha(1.0f);
            } else {
                colorView.setAlpha(0.78f);
            }

            colorView.setBackground(background);
        }
    }

    private void updateTypeTabs() {
        if (TYPE_INCOME.equals(selectedType)) {
            tabThuNhap.setBackgroundResource(R.drawable.bg_tab_selected);
            tabChiTieu.setBackgroundResource(R.drawable.bg_tab_unselected);

            tabThuNhap.setTextColor(Color.WHITE);
            tabChiTieu.setTextColor(
                    ContextCompat.getColor(this, R.color.text_primary)
            );
        } else {
            tabChiTieu.setBackgroundResource(R.drawable.bg_tab_selected);
            tabThuNhap.setBackgroundResource(R.drawable.bg_tab_unselected);

            tabChiTieu.setTextColor(Color.WHITE);
            tabThuNhap.setTextColor(
                    ContextCompat.getColor(this, R.color.text_primary)
            );
        }
    }

    private void saveCategory() {
        String name = edtCategoryName.getText().toString().trim();

        if (name.isEmpty()) {
            edtCategoryName.setError("Vui lòng nhập tên danh mục");
            edtCategoryName.requestFocus();
            return;
        }

        long categoryId = databaseHelper.insertCategory(
                name,
                selectedType,
                selectedIcon,
                selectedColor
        );

        if (categoryId == -1) {
            Toast.makeText(
                    this,
                    "Danh mục này đã tồn tại",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("category_id", categoryId);
        resultIntent.putExtra(CategoryActivity.EXTRA_CATEGORY_NAME, name);
        resultIntent.putExtra(CategoryActivity.EXTRA_CATEGORY_TYPE, selectedType);
        resultIntent.putExtra(CategoryActivity.EXTRA_CATEGORY_ICON, selectedIcon);
        resultIntent.putExtra(CategoryActivity.EXTRA_CATEGORY_COLOR, selectedColor);

        setResult(Activity.RESULT_OK, resultIntent);

        Toast.makeText(
                this,
                "Đã tạo danh mục",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.createCategoryContentLayout);

        ViewCompat.setOnApplyWindowInsetsListener(
                content,
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            view.getPaddingLeft(),
                            systemBars.top + dp(8),
                            view.getPaddingRight(),
                            systemBars.bottom + dp(24)
                    );

                    return insets;
                }
        );
    }

    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density
        );
    }

}