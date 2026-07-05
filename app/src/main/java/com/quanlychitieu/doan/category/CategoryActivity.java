package com.quanlychitieu.doan.category;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.quanlychitieu.doan.R;

public class CategoryActivity extends AppCompatActivity {
    private TextView tabExpense, tabIncome;
    private Button btnCreateCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        tabExpense = findViewById(R.id.tabExpense);
        tabIncome = findViewById(R.id.tabIncome);
        btnCreateCategory = findViewById(R.id.btnCreateCategory);

        tabExpense.setOnClickListener(v -> {
            tabExpense.setBackgroundColor(android.graphics.Color.parseColor("#E74C3C"));
            tabExpense.setTextColor(android.graphics.Color.WHITE);
            tabIncome.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            tabIncome.setTextColor(android.graphics.Color.parseColor("#7F8C8D"));

            Toast.makeText(this, "Đang xem danh mục Khoản Chi", Toast.LENGTH_SHORT).show();
        });

        tabIncome.setOnClickListener(v -> {
            tabIncome.setBackgroundColor(android.graphics.Color.parseColor("#2ECC71"));
            tabIncome.setTextColor(android.graphics.Color.WHITE);
            tabExpense.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            tabExpense.setTextColor(android.graphics.Color.parseColor("#7F8C8D"));

            Toast.makeText(this, "Đang xem danh mục Khoản Thu", Toast.LENGTH_SHORT).show();
        });
        btnCreateCategory.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng thêm danh mục đang được phát triển!", Toast.LENGTH_SHORT).show();
        });
        if (findViewById(R.id.imgCategoryBack) != null) {
            findViewById(R.id.imgCategoryBack).setOnClickListener(v -> finish());
        }
    }
}