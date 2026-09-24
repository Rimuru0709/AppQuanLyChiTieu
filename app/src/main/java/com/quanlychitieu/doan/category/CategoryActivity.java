package com.quanlychitieu.doan.category;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_NAME =
            "category_name";

    public static final String EXTRA_CATEGORY_ICON =
            "category_icon";

    public static final String EXTRA_CATEGORY_COLOR =
            "category_color";

    public static final String EXTRA_CATEGORY_TYPE =
            "category_type";

    private TextView tabChiTieu;
    private TextView tabThuNhap;

    private DatabaseHelper databaseHelper;
    private CategoryAdapter categoryAdapter;

    private String selectedType = "EXPENSE";
    private boolean isSelectingCategory = false;

    private ActivityResultLauncher<Intent>
            createCategoryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Chừa khoảng trống cho thanh trạng thái phía trên.
        setupSafeArea();

        // Khởi tạo và xử lý thanh điều hướng phía dưới.
        BottomNavHelper.setup(this);

        databaseHelper = new DatabaseHelper(this);

        isSelectingCategory = getIntent().getBooleanExtra(
                "select_category_mode",
                false
        );

        setupCreateCategoryLauncher();

        ImageView imgBack = findViewById(
                R.id.imgCategoryBack
        );

        tabChiTieu = findViewById(
                R.id.tabExpenseCategory
        );

        tabThuNhap = findViewById(
                R.id.tabIncomeCategory
        );

        RecyclerView recyclerCategories =
                findViewById(
                        R.id.recyclerCategories
                );

        categoryAdapter = new CategoryAdapter(
                category -> {

                    if (category.isCreateButton()) {
                        openCreateCategory();
                        return;
                    }

                    if (isSelectingCategory) {
                        returnSelectedCategory(category);
                    } else {
                        openEditCategory(category);
                    }
                }
        );

        recyclerCategories.setLayoutManager(
                new GridLayoutManager(this, 4)
        );

        recyclerCategories.setAdapter(
                categoryAdapter
        );

        imgBack.setOnClickListener(
                view -> finish()
        );

        tabChiTieu.setOnClickListener(
                view -> setCategoryMode("EXPENSE")
        );

        tabThuNhap.setOnClickListener(
                view -> setCategoryMode("INCOME")
        );

        String typeFromIntent =
                getIntent().getStringExtra(
                        EXTRA_CATEGORY_TYPE
                );

        if ("INCOME".equals(typeFromIntent)) {
            setCategoryMode("INCOME");
        } else {
            setCategoryMode("EXPENSE");
        }
    }

    private void setupCreateCategoryLauncher() {
        createCategoryLauncher =
                registerForActivityResult(
                        new ActivityResultContracts
                                .StartActivityForResult(),

                        result -> {
                            if (result.getResultCode()
                                    != Activity.RESULT_OK) {
                                return;
                            }

                            Intent data = result.getData();

                            if (data == null) {
                                return;
                            }

                            String name =
                                    data.getStringExtra(
                                            EXTRA_CATEGORY_NAME
                                    );

                            String type =
                                    data.getStringExtra(
                                            EXTRA_CATEGORY_TYPE
                                    );

                            String icon =
                                    data.getStringExtra(
                                            EXTRA_CATEGORY_ICON
                                    );

                            String color =
                                    data.getStringExtra(
                                            EXTRA_CATEGORY_COLOR
                                    );

                            if (name == null
                                    || type == null
                                    || icon == null
                                    || color == null) {
                                return;
                            }

                            CategoryModel newCategory =
                                    new CategoryModel(
                                            data.getLongExtra(
                                                    "category_id",
                                                    -1
                                            ),
                                            name,
                                            type,
                                            icon,
                                            color,
                                            false
                                    );

                            returnSelectedCategory(
                                    newCategory
                            );
                        }
                );
    }

    private void setCategoryMode(String type) {
        selectedType = type;

        if ("INCOME".equals(type)) {

            tabThuNhap.setBackgroundResource(
                    R.drawable.bg_tab_selected
            );

            tabChiTieu.setBackgroundResource(
                    R.drawable.bg_tab_unselected
            );

            tabThuNhap.setTextColor(
                    Color.WHITE
            );

            tabChiTieu.setTextColor(
                    ContextCompat.getColor(
                            this,
                            R.color.text_primary
                    )
            );

        } else {

            tabChiTieu.setBackgroundResource(
                    R.drawable.bg_tab_selected
            );

            tabThuNhap.setBackgroundResource(
                    R.drawable.bg_tab_unselected
            );

            tabChiTieu.setTextColor(
                    Color.WHITE
            );

            tabThuNhap.setTextColor(
                    ContextCompat.getColor(
                            this,
                            R.color.text_primary
                    )
            );
        }

        loadCategories();
    }

    private void loadCategories() {
        if (databaseHelper == null
                || categoryAdapter == null) {
            return;
        }

        List<CategoryModel> categories =
                new ArrayList<>();

        Cursor cursor =
                databaseHelper.getCategoriesByType(
                        selectedType
                );

        try {
            int idIndex =
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.CATEGORY_ID
                    );

            int nameIndex =
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.CATEGORY_NAME
                    );

            int typeIndex =
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.CATEGORY_TYPE
                    );

            int iconIndex =
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.CATEGORY_ICON
                    );

            int colorIndex =
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.CATEGORY_COLOR
                    );

            int defaultIndex =
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper
                                    .CATEGORY_IS_DEFAULT
                    );

            while (cursor.moveToNext()) {
                CategoryModel category =
                        new CategoryModel(
                                cursor.getLong(idIndex),
                                cursor.getString(nameIndex),
                                cursor.getString(typeIndex),
                                cursor.getString(iconIndex),
                                cursor.getString(colorIndex),
                                cursor.getInt(defaultIndex) == 1
                        );

                categories.add(category);
            }

        } finally {
            cursor.close();
        }

        categories.add(
                CategoryModel.createButton(
                        selectedType
                )
        );

        categoryAdapter.setCategories(
                categories
        );
    }

    private void openCreateCategory() {
        Intent intent = new Intent(
                this,
                CreateCategoryActivity.class
        );

        intent.putExtra(
                EXTRA_CATEGORY_TYPE,
                selectedType
        );

        createCategoryLauncher.launch(
                intent
        );

    }

    private void returnSelectedCategory(
            CategoryModel category
    ) {
        Intent resultIntent =
                new Intent();

        resultIntent.putExtra(
                EXTRA_CATEGORY_NAME,
                category.getName()
        );

        resultIntent.putExtra(
                EXTRA_CATEGORY_ICON,
                category.getIcon()
        );

        resultIntent.putExtra(
                EXTRA_CATEGORY_COLOR,
                category.getColor()
        );

        resultIntent.putExtra(
                EXTRA_CATEGORY_TYPE,
                category.getType()
        );

        setResult(
                Activity.RESULT_OK,
                resultIntent
        );

        finish();
    }

    /**
     * Chừa khoảng trống cho thanh trạng thái phía trên.
     * Phần khoảng trống phía dưới được BottomNavHelper xử lý.
     */
    private void setupSafeArea() {
        View mainContent = findViewById(
                R.id.categoryMainContent
        );

        if (mainContent == null) {
            return;
        }

        int originalLeft =
                mainContent.getPaddingLeft();

        int originalRight =
                mainContent.getPaddingRight();

        int originalBottom =
                mainContent.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(
                mainContent,
                (view, insets) -> {

                    Insets statusBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type
                                            .statusBars()
                            );

                    view.setPadding(
                            originalLeft,
                            statusBars.top + dp(8),
                            originalRight,
                            originalBottom
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(
                mainContent
        );
    }

    private int dp(int value) {
        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private void openEditCategory(CategoryModel category) {

        Intent intent = new Intent(
                this,
                EditCategoryActivity.class
        );

        intent.putExtra(
                "category_id",
                category.getId()
        );

        intent.putExtra(
                EXTRA_CATEGORY_NAME,
                category.getName()
        );

        intent.putExtra(
                EXTRA_CATEGORY_TYPE,
                category.getType()
        );

        intent.putExtra(
                EXTRA_CATEGORY_ICON,
                category.getIcon()
        );

        intent.putExtra(
                EXTRA_CATEGORY_COLOR,
                category.getColor()
        );

        intent.putExtra(
                "category_is_default",
                category.isDefaultCategory()
        );

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && categoryAdapter != null) {
            loadCategories();
        }
    }
}