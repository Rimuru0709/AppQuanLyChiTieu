package com.quanlychitieu.doan.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.quanlychitieu.doan.R;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();

        TextView tvLogin = findViewById(R.id.tvLogin);
        TextView btnSignUp = findViewById(R.id.btnSignUp);

        EditText edtFullName = findViewById(R.id.edtFullName);
        EditText edtEmail = findViewById(R.id.edtEmail);
        EditText edtPhone = findViewById(R.id.edtPhone);
        EditText edtPassword = findViewById(R.id.edtPassword);
        EditText edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });

        btnSignUp.setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty()) {
                edtFullName.setError("Vui lòng nhập họ tên");
                return;
            }

            if (email.isEmpty()) {
                edtEmail.setError("Vui lòng nhập email");
                return;
            }

            if (phone.isEmpty()) {
                edtPhone.setError("Vui lòng nhập số điện thoại");
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                return;
            }

            if (password.length() < 6) {
                edtPassword.setError("Mật khẩu phải từ 6 ký tự");
                return;
            }

            if (!password.equals(confirmPassword)) {
                edtConfirmPassword.setError("Mật khẩu không khớp");
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        edtFullName.setOnEditorActionListener((v, actionId, event) -> {
            edtEmail.requestFocus();
            return true;
        });

        edtEmail.setOnEditorActionListener((v, actionId, event) -> {
            edtPhone.requestFocus();
            return true;
        });

        edtPhone.setOnEditorActionListener((v, actionId, event) -> {
            edtPassword.requestFocus();
            return true;
        });

        edtPassword.setOnEditorActionListener((v, actionId, event) -> {
            edtConfirmPassword.requestFocus();
            return true;
        });

        edtConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(edtConfirmPassword);
                edtConfirmPassword.clearFocus();
                return true;
            }
            return false;
        });

        findViewById(R.id.main).setOnClickListener(v -> {
            View currentView = getCurrentFocus();

            if (currentView != null) {
                hideKeyboard(currentView);
                currentView.clearFocus();
            }
        });

        setupPasswordToggle(edtPassword);
        setupPasswordToggle(edtConfirmPassword);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle(EditText editText) {
        final boolean[] isVisible = {false};

        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();

                if (event.getRawX() >= editText.getRight()
                        - editText.getCompoundDrawables()[2].getBounds().width()
                        - editText.getPaddingEnd()) {

                    if (isVisible[0]) {
                        editText.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_PASSWORD);

                        editText.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_lock, 0, R.drawable.ic_eye_off, 0);

                    } else {
                        editText.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

                        editText.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_lock, 0, R.drawable.ic_eye, 0);
                    }

                    isVisible[0] = !isVisible[0];
                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }
}