package com.quanlychitieu.doan.wallet;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.quanlychitieu.doan.R;

public class WalletActivity extends AppCompatActivity {
    private TextView tvTotalBalance;
    private ImageView imgToggleHideBalance;
    private Button btnAddWallet;

    private boolean isBalanceHidden = false;
    private final String actualBalance = "15.500.000 đ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        imgToggleHideBalance = findViewById(R.id.imgToggleHideBalance);
        btnAddWallet = findViewById(R.id.btnAddWallet);

        imgToggleHideBalance.setOnClickListener(v -> {
            if (isBalanceHidden) {
                tvTotalBalance.setText(actualBalance);Toast.makeText(this, "Đã hiển thị số dư", Toast.LENGTH_SHORT).show();
            } else {
                tvTotalBalance.setText("******** đ");

                Toast.makeText(this, "Đã ẩn số dư bảo mật", Toast.LENGTH_SHORT).show();
            }
            isBalanceHidden = !isBalanceHidden;
        });
        btnAddWallet.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng thêm tài khoản ví mới đang được xây dựng!", Toast.LENGTH_SHORT).show();
            // Sau này viết code mở màn hình tạo ví mới ở đây
        });
        if (findViewById(R.id.imgWalletBack) != null) {
            findViewById(R.id.imgWalletBack).setOnClickListener(v -> finish());
        }
    }
}