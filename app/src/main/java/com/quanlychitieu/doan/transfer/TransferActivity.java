package com.quanlychitieu.doan.transfer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.quanlychitieu.doan.R;

public class TransferActivity extends AppCompatActivity {

    private EditText edtAccountNumber, edtBank, edtAmount, edtContent, edtReceiver;
    private Button btnConfirmTransfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        edtReceiver = findViewById(R.id.edtReceiver);
        edtAccountNumber = findViewById(R.id.edtAccountNumber);
        edtBank = findViewById(R.id.edtBank);
        edtAmount = findViewById(R.id.edtAmount);
        edtContent = findViewById(R.id.edtContent);
        btnConfirmTransfer = findViewById(R.id.btnConfirmTransfer);
        setupQuickAmountButtons();
        btnConfirmTransfer.setOnClickListener(v -> {
            String stk = edtAccountNumber.getText().toString().trim();
            String bank = edtBank.getText().toString().trim();
            String amount = edtAmount.getText().toString().trim();
            String content = edtContent.getText().toString().trim();
            if (stk.isEmpty() || bank.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ số tài khoản, ngân hàng và số tiền!", Toast.LENGTH_SHORT).show();
                return;
            }
            String message = "Chuyển thành công " + amount + "đ đến STK: " + stk + " (" + bank + ")";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            finish();
        });
        if (findViewById(R.id.imgBack) != null) {
            findViewById(R.id.imgBack).setOnClickListener(v -> finish());
        }
    }

    /**
     * Hàm hỗ trợ bắt sự kiện cho 4 nút chọn tiền nhanh dựa theo Text hiển thị
     * Vì trong XML tạm thời các nút này chưa có ID riêng lẻ mà nằm trong LinearLayout,
     * Cách tốt nhất để xử lý nhanh là tìm trực tiếp qua cơ chế click hoặc bạn có thể tìm theo logic Text.
     * Để an toàn không bị crash, chúng ta sẽ bắt sự kiện cho các khối tiền nếu bạn bổ sung ID hoặc click bằng code.
     */
    private void setupQuickAmountButtons() {
        // Mẹo: Để code Java này chạy mượt nhất, bạn có thể thiết lập số tiền trực tiếp khi gõ
        // Nếu sau này bạn đặt id cho 4 nút đó là: btn50k, btn100k, btn500k, btn1M thì mở đoạn code dưới đây ra:
        /*
        findViewById(R.id.btn50k).setOnClickListener(v -> edtAmount.setText("50000"));
        findViewById(R.id.btn100k).setOnClickListener(v -> edtAmount.setText("100000"));
        findViewById(R.id.btn500k).setOnClickListener(v -> edtAmount.setText("500000"));
        findViewById(R.id.btn1M).setOnClickListener(v -> edtAmount.setText("1000000"));
        */
    }
}