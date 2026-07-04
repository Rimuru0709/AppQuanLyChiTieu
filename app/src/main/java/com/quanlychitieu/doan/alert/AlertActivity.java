package com.quanlychitieu.doan.alert;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quanlychitieu.doan.R;

import java.util.ArrayList;

public class AlertActivity extends AppCompatActivity {

    RecyclerView recyclerAlert;

    Button btnAddCategory, btnSaveAlert;

    EditText edtBigTransaction, edtLowBalance;

    CheckBox cbPush, cbEmail, cbSMS;

    ArrayList<AlertModel> list;

    AlertAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        recyclerAlert = findViewById(R.id.recyclerAlert);

        btnAddCategory = findViewById(R.id.btnAddCategory);
        btnSaveAlert = findViewById(R.id.btnSaveAlert);

        edtBigTransaction = findViewById(R.id.edtBigTransaction);
        edtLowBalance = findViewById(R.id.edtLowBalance);

        cbPush = findViewById(R.id.cbPush);
        cbEmail = findViewById(R.id.cbEmail);
        cbSMS = findViewById(R.id.cbSMS);

        list = new ArrayList<>();

        list.add(new AlertModel(
                R.mipmap.ic_launcher,
                "Ăn uống",
                "Ngân sách: 2.000.000 đ/tháng",
                "Đã dùng: 1.600.000 đ",
                "80%",
                true));

        list.add(new AlertModel(
                R.mipmap.ic_launcher,
                "Đi lại",
                "Ngân sách: 500.000 đ/tháng",
                "Đã dùng: 400.000 đ",
                "80%",
                true));

        list.add(new AlertModel(
                R.mipmap.ic_launcher,
                "Mua sắm",
                "Ngân sách: 1.500.000 đ/tháng",
                "Đã dùng: 1.200.000 đ",
                "80%",
                true));

        list.add(new AlertModel(
                R.mipmap.ic_launcher,
                "Giải trí",
                "Ngân sách: 800.000 đ/tháng",
                "Đã dùng: 640.000 đ",
                "80%",
                true));

        list.add(new AlertModel(
                R.mipmap.ic_launcher,
                "Hóa đơn",
                "Ngân sách: 2.000.000 đ/tháng",
                "Đã dùng: 1.600.000 đ",
                "80%",
                true));

        adapter = new AlertAdapter(this, list);

        recyclerAlert.setLayoutManager(new LinearLayoutManager(this));

        recyclerAlert.setAdapter(adapter);

        // Thêm danh mục
        btnAddCategory.setOnClickListener(v ->

                Toast.makeText(AlertActivity.this,
                        "Chức năng thêm danh mục đang phát triển",
                        Toast.LENGTH_SHORT).show());

        // Lưu cài đặt
        btnSaveAlert.setOnClickListener(v -> {

            String big = edtBigTransaction.getText().toString();

            String low = edtLowBalance.getText().toString();

            if (big.isEmpty() || low.isEmpty()) {

                Toast.makeText(AlertActivity.this,
                        "Vui lòng nhập đầy đủ thông tin",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            String message =
                    "Đã lưu cài đặt\n\n" +
                            "Giao dịch lớn: " + big +
                            "\nSố dư thấp: " + low +
                            "\n\nThông báo đẩy: " + (cbPush.isChecked() ? "Có" : "Không") +
                            "\nEmail: " + (cbEmail.isChecked() ? "Có" : "Không") +
                            "\nSMS: " + (cbSMS.isChecked() ? "Có" : "Không");

            Toast.makeText(AlertActivity.this,
                    message,
                    Toast.LENGTH_LONG).show();

        });

    }
}