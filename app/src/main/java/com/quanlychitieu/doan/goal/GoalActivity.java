package com.quanlychitieu.doan.goal;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.quanlychitieu.doan.R;

import java.util.Locale;

public class GoalActivity extends AppCompatActivity {

    private EditText edtName, edtMoney, edtMonth;
    private TextView txtGoalMoney, txtMonthly, txtSaved, txtRemain;
    private ProgressBar progressGoal;
    private SwitchCompat swAuto;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        edtName = findViewById(R.id.edtName);
        edtMoney = findViewById(R.id.edtMoney);
        edtMonth = findViewById(R.id.edtMonth);

        txtGoalMoney = findViewById(R.id.txtGoalMoney);
        txtMonthly = findViewById(R.id.txtMonthly);
        txtSaved = findViewById(R.id.txtSaved);
        txtRemain = findViewById(R.id.txtRemain);

        progressGoal = findViewById(R.id.progressGoal);

        swAuto = findViewById(R.id.swAuto);

        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {

            String name = edtName.getText().toString().trim();
            String money = edtMoney.getText().toString().trim();
            String month = edtMonth.getText().toString().trim();

            if (name.isEmpty() || money.isEmpty() || month.isEmpty()) {
                Toast.makeText(
                        GoalActivity.this,
                        "Vui lòng nhập đầy đủ thông tin",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            double total;
            int months;

            try {
                total = Double.parseDouble(money);
                months = Integer.parseInt(month);
            } catch (Exception e) {
                Toast.makeText(
                        GoalActivity.this,
                        "Dữ liệu không hợp lệ",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (months <= 0) {
                Toast.makeText(
                        GoalActivity.this,
                        "Số tháng phải lớn hơn 0",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            double monthly = total / months;

            txtGoalMoney.setText(
                    String.format(Locale.getDefault(), "%,.0f đ", total)
            );

            txtMonthly.setText(
                    String.format(Locale.getDefault(), "%,.0f đ", monthly)
            );

            double saved = total * 0.25;
            double remain = total - saved;

            txtSaved.setText(
                    String.format(
                            Locale.getDefault(),
                            "Đã tiết kiệm: %,.0f đ",
                            saved
                    )
            );

            txtRemain.setText(
                    String.format(
                            Locale.getDefault(),
                            "Còn: %,.0f đ",
                            remain
                    )
            );

            progressGoal.setProgress(25);

            Toast.makeText(
                    GoalActivity.this,
                    "Đã lưu mục tiêu tiết kiệm",
                    Toast.LENGTH_SHORT
            ).show();
        });

        swAuto.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {

                Toast.makeText(
                        GoalActivity.this,
                        "Đã bật tự động tiết kiệm",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        GoalActivity.this,
                        "Đã tắt tự động tiết kiệm",
                        Toast.LENGTH_SHORT
                ).show();

            }
        });
    }
}