package com.quanlychitieu.doan.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.setting.AccountStorage;

import java.util.Calendar;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private EditText edtFullName;
    private EditText edtEmail;
    private EditText edtPhone;
    private EditText edtBirthday;
    private EditText edtPassword;
    private EditText edtConfirmPassword;

    private CheckBox chkTerms;

    private Button btnSignUp;

    private TextView tvLogin;

    private ImageView imgAvatar;
    private ImageView imgCamera;

    private boolean signUpProcessing = false;

    private String selectedAvatarUri = "";

    private final ActivityResultLauncher<String[]> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri == null) {
                            return;
                        }

                        try {
                            getContentResolver()
                                    .takePersistableUriPermission(
                                            uri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    );
                        } catch (SecurityException ignored) {
                            // Một số thiết bị không yêu cầu quyền lưu URI.
                        }

                        selectedAvatarUri = uri.toString();

                        imgAvatar.setImageURI(uri);
                    }
            );

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();

        initViews();
        setupSafeArea();
        setupEvents();

        setupPasswordToggle(edtPassword);
        setupPasswordToggle(edtConfirmPassword);
    }

    // =========================================================
    // KHỞI TẠO VIEW
    // =========================================================

    private void initViews() {
        edtFullName =
                findViewById(R.id.edtFullName);

        edtEmail =
                findViewById(R.id.edtEmail);

        edtPhone =
                findViewById(R.id.edtPhone);

        edtBirthday =
                findViewById(R.id.edtBirthday);

        edtPassword =
                findViewById(R.id.edtPassword);

        edtConfirmPassword =
                findViewById(R.id.edtConfirmPassword);

        chkTerms =
                findViewById(R.id.chkTerms);

        btnSignUp =
                findViewById(R.id.btnSignUp);

        tvLogin =
                findViewById(R.id.tvLogin);

        imgAvatar =
                findViewById(R.id.imgAvatar);

        imgCamera =
                findViewById(R.id.imgCamera);
    }

    // =========================================================
    // THIẾT LẬP SỰ KIỆN
    // =========================================================

    private void setupEvents() {

        tvLogin.setOnClickListener(
                view -> openLoginActivity()
        );

        btnSignUp.setOnClickListener(
                view -> registerAccount()
        );

        imgAvatar.setOnClickListener(
                view -> openImagePicker()
        );

        imgCamera.setOnClickListener(
                view -> openImagePicker()
        );

        edtBirthday.setOnClickListener(
                view -> showBirthdayPicker()
        );

        edtFullName.setOnEditorActionListener(
                (view, actionId, event) -> {
                    edtEmail.requestFocus();
                    return true;
                }
        );

        edtEmail.setOnEditorActionListener(
                (view, actionId, event) -> {
                    edtPhone.requestFocus();
                    return true;
                }
        );

        edtPhone.setOnEditorActionListener(
                (view, actionId, event) -> {
                    hideKeyboard(edtPhone);
                    edtPhone.clearFocus();
                    showBirthdayPicker();
                    return true;
                }
        );

        edtPassword.setOnEditorActionListener(
                (view, actionId, event) -> {
                    edtConfirmPassword.requestFocus();
                    return true;
                }
        );

        edtConfirmPassword.setOnEditorActionListener(
                (view, actionId, event) -> {
                    if (actionId ==
                            EditorInfo.IME_ACTION_DONE) {

                        hideKeyboard(
                                edtConfirmPassword
                        );

                        edtConfirmPassword.clearFocus();

                        registerAccount();

                        return true;
                    }

                    return false;
                }
        );
    }

    // =========================================================
    // MỞ THƯ VIỆN CHỌN ẢNH
    // =========================================================

    private void openImagePicker() {
        if (signUpProcessing) {
            return;
        }

        imagePickerLauncher.launch(
                new String[]{
                        "image/*"
                }
        );
    }

    // =========================================================
    // HIỂN THỊ DATE PICKER CHỌN NGÀY SINH
    // =========================================================

    private void showBirthdayPicker() {
        if (signUpProcessing) {
            return;
        }

        Calendar calendar =
                Calendar.getInstance();

        int currentYear =
                calendar.get(Calendar.YEAR);

        int currentMonth =
                calendar.get(Calendar.MONTH);

        int currentDay =
                calendar.get(Calendar.DAY_OF_MONTH);

        int selectedYear =
                currentYear - 18;

        int selectedMonth =
                currentMonth;

        int selectedDay =
                currentDay;

        String currentBirthday =
                edtBirthday
                        .getText()
                        .toString()
                        .trim();

        if (!currentBirthday.isEmpty()) {
            String[] birthdayParts =
                    currentBirthday.split("/");

            if (birthdayParts.length == 3) {
                try {
                    selectedDay =
                            Integer.parseInt(
                                    birthdayParts[0]
                            );

                    selectedMonth =
                            Integer.parseInt(
                                    birthdayParts[1]
                            ) - 1;

                    selectedYear =
                            Integer.parseInt(
                                    birthdayParts[2]
                            );

                } catch (NumberFormatException ignored) {
                    selectedYear =
                            currentYear - 18;

                    selectedMonth =
                            currentMonth;

                    selectedDay =
                            currentDay;
                }
            }
        }

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        SignUpActivity.this,
                        (datePicker, year, month, dayOfMonth) -> {

                            String birthday =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            dayOfMonth,
                                            month + 1,
                                            year
                                    );

                            edtBirthday.setText(
                                    birthday
                            );

                            edtBirthday.setError(null);

                            edtPassword.requestFocus();
                        },
                        selectedYear,
                        selectedMonth,
                        selectedDay
                );

        datePickerDialog
                .getDatePicker()
                .setMaxDate(
                        System.currentTimeMillis()
                );

        datePickerDialog.show();
    }

    // =========================================================
    // ĐĂNG KÝ TÀI KHOẢN
    // =========================================================

    private void registerAccount() {

        if (signUpProcessing) {
            return;
        }

        hideKeyboard(
                edtConfirmPassword
        );

        String fullName =
                edtFullName
                        .getText()
                        .toString()
                        .trim();

        String email =
                edtEmail
                        .getText()
                        .toString()
                        .trim();

        String phone =
                edtPhone
                        .getText()
                        .toString()
                        .trim();

        String birthday =
                edtBirthday
                        .getText()
                        .toString()
                        .trim();

        String password =
                edtPassword
                        .getText()
                        .toString();

        String confirmPassword =
                edtConfirmPassword
                        .getText()
                        .toString();

        clearErrors();

        if (!chkTerms.isChecked()) {
            Toast.makeText(
                    SignUpActivity.this,
                    "Bạn cần đồng ý với điều khoản sử dụng",
                    Toast.LENGTH_SHORT
            ).show();

            chkTerms.requestFocus();
            return;
        }

        boolean valid =
                validateInput(
                        fullName,
                        email,
                        phone,
                        birthday,
                        password,
                        confirmPassword
                );

        if (!valid) {
            return;
        }

        setSignUpProcessing(true);

        auth.createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnSuccessListener(
                        authResult -> {

                            FirebaseUser firebaseUser =
                                    authResult.getUser();

                            if (firebaseUser == null) {
                                setSignUpProcessing(false);

                                Toast.makeText(
                                        SignUpActivity.this,
                                        "Không lấy được thông tin tài khoản",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            saveRegisteredProfile(
                                    firebaseUser,
                                    fullName,
                                    email,
                                    phone,
                                    birthday
                            );
                        }
                )
                .addOnFailureListener(
                        exception -> {

                            setSignUpProcessing(false);

                            Toast.makeText(
                                    SignUpActivity.this,
                                    getErrorMessage(exception),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // =========================================================
    // LƯU THÔNG TIN TÀI KHOẢN
    // =========================================================

    private void saveRegisteredProfile(
            FirebaseUser firebaseUser,
            String fullName,
            String email,
            String phone,
            String birthday
    ) {
        UserProfileChangeRequest profileChangeRequest =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build();

        firebaseUser.updateProfile(
                        profileChangeRequest
                )
                .addOnCompleteListener(task -> {

                    String userId =
                            firebaseUser.getUid();

                    AccountStorage.saveProfile(
                            SignUpActivity.this,
                            userId,
                            fullName,
                            email,
                            phone,
                            birthday
                    );

                    if (selectedAvatarUri != null
                            && !selectedAvatarUri.trim().isEmpty()) {

                        AccountStorage.saveAvatarUri(
                                SignUpActivity.this,
                                userId,
                                selectedAvatarUri
                        );
                    }

                    /*
                     * Firebase tự đăng nhập sau khi tạo tài khoản.
                     * Đăng xuất để người dùng đăng nhập lại ở LoginActivity.
                     */
                    auth.signOut();

                    setSignUpProcessing(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(
                                SignUpActivity.this,
                                "Đăng ký tài khoản thành công",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {
                        Toast.makeText(
                                SignUpActivity.this,
                                "Đăng ký thành công nhưng chưa cập nhật được tên hiển thị",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    openLoginActivity();
                });
    }

    // =========================================================
    // KIỂM TRA DỮ LIỆU
    // =========================================================

    private boolean validateInput(
            String fullName,
            String email,
            String phone,
            String birthday,
            String password,
            String confirmPassword
    ) {
        if (fullName.isEmpty()) {
            edtFullName.setError(
                    "Vui lòng nhập họ và tên"
            );

            edtFullName.requestFocus();
            return false;
        }

        if (fullName.length() < 2) {
            edtFullName.setError(
                    "Họ và tên không hợp lệ"
            );

            edtFullName.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            edtEmail.setError(
                    "Vui lòng nhập email"
            );

            edtEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            edtEmail.setError(
                    "Địa chỉ email không hợp lệ"
            );

            edtEmail.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            edtPhone.setError(
                    "Vui lòng nhập số điện thoại"
            );

            edtPhone.requestFocus();
            return false;
        }

        if (!phone.matches("^[0-9]{9,11}$")) {
            edtPhone.setError(
                    "Số điện thoại phải có từ 9 đến 11 chữ số"
            );

            edtPhone.requestFocus();
            return false;
        }

        if (birthday.isEmpty()) {
            edtBirthday.setError(
                    "Vui lòng chọn ngày sinh"
            );

            showBirthdayPicker();
            return false;
        }

        if (password.isEmpty()) {
            edtPassword.setError(
                    "Vui lòng nhập mật khẩu"
            );

            edtPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            edtPassword.setError(
                    "Mật khẩu phải có ít nhất 6 ký tự"
            );

            edtPassword.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            edtConfirmPassword.setError(
                    "Vui lòng nhập lại mật khẩu"
            );

            edtConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError(
                    "Mật khẩu nhập lại không khớp"
            );

            edtConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    // =========================================================
    // XÓA THÔNG BÁO LỖI
    // =========================================================

    private void clearErrors() {
        edtFullName.setError(null);
        edtEmail.setError(null);
        edtPhone.setError(null);
        edtBirthday.setError(null);
        edtPassword.setError(null);
        edtConfirmPassword.setError(null);
    }

    // =========================================================
    // CHUYỂN VỀ MÀN HÌNH ĐĂNG NHẬP
    // =========================================================

    private void openLoginActivity() {
        Intent intent =
                new Intent(
                        SignUpActivity.this,
                        LoginActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        startActivity(intent);
        finish();
    }

    // =========================================================
    // TRẠNG THÁI ĐANG ĐĂNG KÝ
    // =========================================================

    private void setSignUpProcessing(
            boolean processing
    ) {
        signUpProcessing = processing;

        edtFullName.setEnabled(!processing);
        edtEmail.setEnabled(!processing);
        edtPhone.setEnabled(!processing);
        edtBirthday.setEnabled(!processing);
        edtPassword.setEnabled(!processing);
        edtConfirmPassword.setEnabled(!processing);

        chkTerms.setEnabled(!processing);

        btnSignUp.setEnabled(!processing);
        tvLogin.setEnabled(!processing);

        imgAvatar.setEnabled(!processing);
        imgCamera.setEnabled(!processing);

        btnSignUp.setText(
                processing
                        ? "Đang đăng ký..."
                        : "Đăng ký"
        );
    }

    // =========================================================
    // HIỆN / ẨN MẬT KHẨU
    // =========================================================

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle(
            EditText editText
    ) {
        final boolean[] passwordVisible =
                {false};

        editText.setOnTouchListener(
                (view, event) -> {

                    if (event.getAction()
                            != MotionEvent.ACTION_UP) {

                        return false;
                    }

                    if (editText
                            .getCompoundDrawables()[2]
                            == null) {

                        return false;
                    }

                    int drawableWidth =
                            editText
                                    .getCompoundDrawables()[2]
                                    .getBounds()
                                    .width();

                    float iconStartPosition =
                            editText.getWidth()
                                    - editText.getPaddingEnd()
                                    - drawableWidth;

                    if (event.getX()
                            < iconStartPosition) {

                        return false;
                    }

                    passwordVisible[0] =
                            !passwordVisible[0];

                    int selectionPosition =
                            editText
                                    .getText()
                                    .length();

                    if (passwordVisible[0]) {
                        editText.setInputType(
                                InputType.TYPE_CLASS_TEXT
                                        | InputType
                                        .TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );

                        editText.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_lock,
                                0,
                                R.drawable.ic_eye,
                                0
                        );

                    } else {
                        editText.setInputType(
                                InputType.TYPE_CLASS_TEXT
                                        | InputType
                                        .TYPE_TEXT_VARIATION_PASSWORD
                        );

                        editText.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_lock,
                                0,
                                R.drawable.ic_eye_off,
                                0
                        );
                    }

                    editText.setCompoundDrawablePadding(
                            dp(12)
                    );

                    editText.setSelection(
                            selectionPosition
                    );

                    return true;
                }
        );
    }

    // =========================================================
    // SAFE AREA
    // =========================================================

    private void setupSafeArea() {
        View content =
                findViewById(R.id.contentLayout);

        if (content == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                content,
                (view, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat
                                            .Type
                                            .systemBars()
                            );

                    view.setPadding(
                            dp(24),
                            systemBars.top + dp(20),
                            dp(24),
                            systemBars.bottom + dp(30)
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(content);
    }

    // =========================================================
    // ẨN BÀN PHÍM
    // =========================================================

    private void hideKeyboard(
            View view
    ) {
        if (view == null) {
            return;
        }

        InputMethodManager inputMethodManager =
                (InputMethodManager)
                        getSystemService(
                                INPUT_METHOD_SERVICE
                        );

        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    view.getWindowToken(),
                    0
            );
        }
    }

    // =========================================================
    // THÔNG BÁO LỖI FIREBASE
    // =========================================================

    private String getErrorMessage(
            Exception exception
    ) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            return "Email này đã được đăng ký";
        }

        if (exception instanceof FirebaseAuthWeakPasswordException) {
            return "Mật khẩu quá yếu, vui lòng chọn mật khẩu khác";
        }

        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Email hoặc thông tin đăng ký không hợp lệ";
        }

        if (exception instanceof FirebaseNetworkException) {
            return "Không có kết nối mạng, vui lòng thử lại";
        }

        if (exception == null
                || exception.getMessage() == null
                || exception.getMessage().trim().isEmpty()) {

            return "Đăng ký thất bại, vui lòng thử lại";
        }

        return "Đăng ký thất bại: "
                + exception.getMessage();
    }

    // =========================================================
    // CHUYỂN DP
    // =========================================================

    private int dp(
            int value
    ) {
        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}