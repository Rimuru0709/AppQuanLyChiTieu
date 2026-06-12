package com.quanlychitieu.doan.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.quanlychitieu.doan.home.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        SharedPreferences pref = getSharedPreferences("USER", MODE_PRIVATE);
        boolean isLogin = pref.getBoolean("isLogin", false);

        if (isLogin) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        }

        TextView tvSignUp = findViewById(R.id.tvSignUp);
        TextView btnLogin = findViewById(R.id.btnLogin);
        EditText edtEmail = findViewById(R.id.edtEmail);
        EditText edtPassword = findViewById(R.id.edtPassword);

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty()) {
                edtEmail.setError("Vui lòng nhập email");
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("isLogin", true);
                        editor.putString("email", email);
                        editor.apply();

                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        edtEmail.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtPassword.requestFocus();
                return true;
            }
            return false;
        });

        edtPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(edtPassword);
                edtPassword.clearFocus();
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
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle(EditText edtPassword) {
        final boolean[] isVisible = {false};

        edtPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();

                if (event.getRawX() >= edtPassword.getRight()
                        - edtPassword.getCompoundDrawables()[2].getBounds().width()
                        - edtPassword.getPaddingEnd()) {

                    if (isVisible[0]) {
                        edtPassword.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_PASSWORD);

                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_lock, 0, R.drawable.ic_eye_off, 0);

                    } else {
                        edtPassword.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_lock, 0, R.drawable.ic_eye, 0);
                    }

                    isVisible[0] = !isVisible[0];
                    edtPassword.setSelection(edtPassword.getText().length());
                    return true;
                }
            }
            return false;
        });
    }
}