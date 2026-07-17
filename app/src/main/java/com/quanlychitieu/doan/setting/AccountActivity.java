package com.quanlychitieu.doan.setting;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.quanlychitieu.doan.R;

import java.util.Calendar;
import java.util.Locale;

public class AccountActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private String currentUserId = "";

    private ImageView imgBack;
    private ImageView imgAvatar;
    private ImageView imgChangeAvatar;

    private TextView tvAccountName;
    private TextView tvAccountEmail;

    private TextView tvFullName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvBirthday;

    private MaterialButton btnEditAccount;
    private MaterialButton btnChangePassword;

    //=========================================================
    // CHỌN ẢNH ĐẠI DIỆN
    //=========================================================

    private final ActivityResultLauncher<String[]> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {

                        if (uri == null) {
                            return;
                        }

                        saveSelectedAvatar(uri);

                    });

    //=========================================================
    // onCreate
    //=========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        initViews();

        setupSafeArea();

        setupEvents();

        if (!prepareCurrentUser()) {
            return;
        }

        AccountStorage.createProfileFromFirebaseIfNeeded(
                AccountActivity.this
        );

        loadAccountInfo();
    }

    //=========================================================
    // onResume
    //=========================================================

    @Override
    protected void onResume() {
        super.onResume();

        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        currentUser.reload()
                .addOnCompleteListener(task -> {

                    currentUser = auth.getCurrentUser();

                    if (currentUser == null) {
                        return;
                    }

                    currentUserId = currentUser.getUid();

                    String firebaseEmail =
                            currentUser.getEmail();

                    if (firebaseEmail != null
                            && !firebaseEmail.trim().isEmpty()) {

                        AccountStorage.saveEmail(
                                AccountActivity.this,
                                currentUserId,
                                firebaseEmail
                        );
                    }

                    AccountStorage.createProfileFromFirebaseIfNeeded(
                            AccountActivity.this
                    );

                    loadAccountInfo();

                });
    }

    //=========================================================
    // KIỂM TRA ĐĂNG NHẬP
    //=========================================================

    private boolean prepareCurrentUser() {

        currentUser = auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Bạn chưa đăng nhập",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return false;
        }

        currentUserId =
                currentUser.getUid();

        return true;
    }

    //=========================================================
    // ÁNH XẠ VIEW
    //=========================================================

    private void initViews() {

        imgBack =
                findViewById(R.id.imgBack);

        imgAvatar =
                findViewById(R.id.imgAvatar);

        imgChangeAvatar =
                findViewById(R.id.imgChangeAvatar);

        tvAccountName =
                findViewById(R.id.tvAccountName);

        tvAccountEmail =
                findViewById(R.id.tvAccountEmail);

        tvFullName =
                findViewById(R.id.tvFullName);

        tvEmail =
                findViewById(R.id.tvEmail);

        tvPhone =
                findViewById(R.id.tvPhone);

        tvBirthday =
                findViewById(R.id.tvBirthday);

        btnEditAccount =
                findViewById(R.id.btnEditAccount);

        btnChangePassword =
                findViewById(R.id.btnChangePassword);
    }

    //=========================================================
    // SAFE AREA
    //=========================================================

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
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    view.setPadding(
                            dp(22),
                            systemBars.top + dp(8),
                            dp(22),
                            systemBars.bottom + dp(28)
                    );

                    return insets;
                });

        ViewCompat.requestApplyInsets(content);
    }

    //=========================================================
    // SỰ KIỆN
    //=========================================================

    private void setupEvents() {

        imgBack.setOnClickListener(v -> finish());

        imgAvatar.setOnClickListener(v ->
                openImagePicker()
        );

        imgChangeAvatar.setOnClickListener(v ->
                openImagePicker()
        );

        btnEditAccount.setOnClickListener(v ->
                showEditAccountDialog()
        );

        btnChangePassword.setOnClickListener(v ->
                showChangePasswordDialog()
        );
    }
    // =========================================================
    // TẢI THÔNG TIN TÀI KHOẢN
    // =========================================================

    private void loadAccountInfo() {

        if (currentUserId == null
                || currentUserId.trim().isEmpty()) {

            return;
        }

        String fullName =
                AccountStorage.getFullName(
                        AccountActivity.this,
                        currentUserId
                );

        String email =
                AccountStorage.getEmail(
                        AccountActivity.this,
                        currentUserId
                );

        String phone =
                AccountStorage.getPhone(
                        AccountActivity.this,
                        currentUserId
                );

        String birthday =
                AccountStorage.getBirthday(
                        AccountActivity.this,
                        currentUserId
                );

        /*
         * Nếu AccountStorage chưa có email,
         * lấy email hiện tại từ Firebase.
         */
        if ((email == null || email.trim().isEmpty())
                && currentUser != null
                && currentUser.getEmail() != null) {

            email =
                    currentUser
                            .getEmail()
                            .trim();

            AccountStorage.saveEmail(
                    AccountActivity.this,
                    currentUserId,
                    email
            );
        }

        /*
         * Nếu AccountStorage chưa có họ tên,
         * lấy displayName từ Firebase.
         */
        if ((fullName == null || fullName.trim().isEmpty())
                && currentUser != null
                && currentUser.getDisplayName() != null) {

            fullName =
                    currentUser
                            .getDisplayName()
                            .trim();

            AccountStorage.saveFullName(
                    AccountActivity.this,
                    currentUserId,
                    fullName
            );
        }

        tvAccountName.setText(
                displayValue(
                        fullName,
                        "Người dùng"
                )
        );

        tvAccountEmail.setText(
                displayValue(
                        email,
                        "Chưa cập nhật email"
                )
        );

        tvFullName.setText(
                displayValue(
                        fullName,
                        "Chưa cập nhật"
                )
        );

        tvEmail.setText(
                displayValue(
                        email,
                        "Chưa cập nhật"
                )
        );

        tvPhone.setText(
                displayValue(
                        phone,
                        "Chưa cập nhật"
                )
        );

        tvBirthday.setText(
                displayValue(
                        birthday,
                        "Chưa cập nhật"
                )
        );

        loadAvatar();
    }

    // =========================================================
    // MỞ THƯ VIỆN CHỌN ẢNH
    // =========================================================

    private void openImagePicker() {

        currentUser = auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    AccountActivity.this,
                    "Bạn chưa đăng nhập",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        imagePickerLauncher.launch(
                new String[]{
                        "image/*"
                }
        );
    }

    // =========================================================
    // LƯU ẢNH ĐẠI DIỆN
    // =========================================================

    private void saveSelectedAvatar(Uri uri) {

        if (uri == null
                || currentUserId == null
                || currentUserId.trim().isEmpty()) {

            return;
        }

        try {

            getContentResolver()
                    .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );

        } catch (SecurityException ignored) {

            /*
             * Một số thiết bị không cần giữ quyền URI.
             */
        }

        AccountStorage.saveAvatarUri(
                AccountActivity.this,
                currentUserId,
                uri.toString()
        );

        displayLocalAvatar(uri);

        Toast.makeText(
                AccountActivity.this,
                "Đã cập nhật ảnh đại diện",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================================================
    // TẢI ẢNH ĐẠI DIỆN
    // =========================================================

    private void loadAvatar() {

        if (currentUserId == null
                || currentUserId.trim().isEmpty()) {

            showDefaultAvatar();
            return;
        }

        String avatarUri =
                AccountStorage.getAvatarUri(
                        AccountActivity.this,
                        currentUserId
                );

        if (avatarUri == null
                || avatarUri.trim().isEmpty()) {

            showDefaultAvatar();
            return;
        }

        if (avatarUri.startsWith("content://")
                || avatarUri.startsWith("file://")) {

            try {

                Uri uri =
                        Uri.parse(avatarUri);

                displayLocalAvatar(uri);

            } catch (Exception exception) {

                showDefaultAvatar();
            }

        } else {

            /*
             * URI dạng https:// từ Google hoặc Facebook
             * cần Glide hoặc Picasso để tải ảnh mạng.
             */
            showDefaultAvatar();
        }
    }

    // =========================================================
    // HIỂN THỊ ẢNH ĐƯỢC CHỌN
    // =========================================================

    private void displayLocalAvatar(Uri uri) {

        if (uri == null) {

            showDefaultAvatar();
            return;
        }

        try {

            imgAvatar.setImageURI(null);
            imgAvatar.setImageURI(uri);

            imgAvatar.setPadding(
                    0,
                    0,
                    0,
                    0
            );

            imgAvatar.setScaleType(
                    ImageView.ScaleType.CENTER_CROP
            );

        } catch (Exception exception) {

            showDefaultAvatar();
        }
    }

    // =========================================================
    // HIỂN THỊ ẢNH ĐẠI DIỆN MẶC ĐỊNH
    // =========================================================

    private void showDefaultAvatar() {

        imgAvatar.setImageResource(
                R.drawable.ic_avata
        );

        imgAvatar.setScaleType(
                ImageView.ScaleType.CENTER_INSIDE
        );

        imgAvatar.setPadding(
                dp(20),
                dp(20),
                dp(20),
                dp(20)
        );
    }

    // =========================================================
    // HỘP THOẠI CHỈNH SỬA THÔNG TIN
    // =========================================================

    private void showEditAccountDialog() {

        currentUser =
                auth.getCurrentUser();

        if (currentUser == null
                || currentUserId == null
                || currentUserId.trim().isEmpty()) {

            Toast.makeText(
                    AccountActivity.this,
                    "Bạn chưa đăng nhập",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                dp(20),
                dp(8),
                dp(20),
                0
        );

        // HỌ VÀ TÊN

        EditText edtFullName =
                createEditText(
                        "Họ và tên",
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_FLAG_CAP_WORDS
                );

        // EMAIL

        EditText edtEmail =
                createEditText(
                        "Email",
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                );

        // SỐ ĐIỆN THOẠI

        EditText edtPhone =
                createEditText(
                        "Số điện thoại",
                        InputType.TYPE_CLASS_PHONE
                );

        edtPhone.setFilters(
                new InputFilter[]{
                        new InputFilter.LengthFilter(11)
                }
        );

        // NGÀY SINH

        EditText edtBirthday =
                createEditText(
                        "Ngày sinh",
                        InputType.TYPE_NULL
                );

        edtBirthday.setFocusable(false);
        edtBirthday.setFocusableInTouchMode(false);
        edtBirthday.setCursorVisible(false);
        edtBirthday.setClickable(true);

        // EMAIL ĐƯỢC PHÉP CHỈNH SỬA

        edtEmail.setEnabled(true);
        edtEmail.setFocusable(true);
        edtEmail.setFocusableInTouchMode(true);
        edtEmail.setAlpha(1.0f);

        // LẤY DỮ LIỆU HIỆN TẠI

        String currentFullName =
                AccountStorage.getFullName(
                        AccountActivity.this,
                        currentUserId
                );

        String currentEmail =
                currentUser.getEmail();

        if (currentEmail == null
                || currentEmail.trim().isEmpty()) {

            currentEmail =
                    AccountStorage.getEmail(
                            AccountActivity.this,
                            currentUserId
                    );
        }

        String currentPhone =
                AccountStorage.getPhone(
                        AccountActivity.this,
                        currentUserId
                );

        String currentBirthday =
                AccountStorage.getBirthday(
                        AccountActivity.this,
                        currentUserId
                );

        edtFullName.setText(
                currentFullName
        );

        edtEmail.setText(
                currentEmail
        );

        edtPhone.setText(
                currentPhone
        );

        edtBirthday.setText(
                currentBirthday
        );

        if (edtFullName.length() > 0) {

            edtFullName.setSelection(
                    edtFullName.length()
            );
        }

        edtBirthday.setOnClickListener(
                view -> showBirthdayPicker(
                        edtBirthday
                )
        );

        // THÊM CÁC Ô NHẬP VÀO CONTAINER

        container.addView(
                edtFullName,
                createInputLayoutParams()
        );

        container.addView(
                edtEmail,
                createInputLayoutParams()
        );

        container.addView(
                edtPhone,
                createInputLayoutParams()
        );

        container.addView(
                edtBirthday,
                createInputLayoutParams()
        );

        // THÔNG BÁO ĐỔI EMAIL

        TextView tvEmailNotice =
                new TextView(this);

        tvEmailNotice.setText(
                "Khi đổi email, Firebase sẽ gửi liên kết xác nhận đến email mới."
        );

        tvEmailNotice.setTextSize(12);

        tvEmailNotice.setPadding(
                dp(4),
                dp(6),
                dp(4),
                0
        );

        container.addView(
                tvEmailNotice
        );

        // TẠO DIALOG

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Chỉnh sửa thông tin"
                        )
                        .setView(container)
                        .setPositiveButton(
                                "Lưu",
                                null
                        )
                        .setNegativeButton(
                                "Hủy",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                ignored -> dialog
                        .getButton(
                                AlertDialog.BUTTON_POSITIVE
                        )
                        .setOnClickListener(
                                view -> {

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

                                    boolean valid =
                                            validateAccountInfo(
                                                    edtFullName,
                                                    edtEmail,
                                                    edtPhone,
                                                    fullName,
                                                    email,
                                                    phone
                                            );

                                    if (!valid) {
                                        return;
                                    }

                                    String oldEmail =
                                            currentUser.getEmail();

                                    boolean emailChanged =
                                            oldEmail != null
                                                    && !oldEmail
                                                    .equalsIgnoreCase(
                                                            email
                                                    );

                                    if (emailChanged) {

                                        showConfirmPasswordForEmailChange(
                                                fullName,
                                                email,
                                                phone,
                                                birthday,
                                                dialog
                                        );

                                    } else {

                                        saveAccountInfo(
                                                fullName,
                                                email,
                                                phone,
                                                birthday
                                        );

                                        dialog.dismiss();
                                    }
                                }
                        )
        );

        dialog.show();
    }
    // =========================================================
    // KIỂM TRA THÔNG TIN TÀI KHOẢN
    // =========================================================

    private boolean validateAccountInfo(
            EditText edtFullName,
            EditText edtEmail,
            EditText edtPhone,
            String fullName,
            String email,
            String phone
    ) {

        edtFullName.setError(null);
        edtEmail.setError(null);
        edtPhone.setError(null);

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
                    "Email không hợp lệ"
            );

            edtEmail.requestFocus();
            return false;
        }

        if (!phone.isEmpty()
                && !phone.matches("^[0-9]{9,11}$")) {

            edtPhone.setError(
                    "Số điện thoại phải có từ 9 đến 11 chữ số"
            );

            edtPhone.requestFocus();
            return false;
        }

        return true;
    }

    // =========================================================
    // LƯU THÔNG TIN TÀI KHOẢN
    // DÙNG KHI EMAIL KHÔNG THAY ĐỔI
    // =========================================================

    private void saveAccountInfo(
            String fullName,
            String email,
            String phone,
            String birthday
    ) {

        if (currentUser == null
                || currentUserId == null
                || currentUserId.trim().isEmpty()) {

            Toast.makeText(
                    AccountActivity.this,
                    "Không tìm thấy tài khoản",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        AccountStorage.saveProfile(
                AccountActivity.this,
                currentUserId,
                fullName,
                email,
                phone,
                birthday
        );

        UserProfileChangeRequest profileRequest =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build();

        currentUser.updateProfile(profileRequest)
                .addOnCompleteListener(task -> {

                    loadAccountInfo();

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                AccountActivity.this,
                                "Đã cập nhật thông tin",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                AccountActivity.this,
                                "Đã lưu thông tin trên thiết bị",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // =========================================================
    // CHỌN NGÀY SINH
    // =========================================================

    private void showBirthdayPicker(
            EditText edtBirthday
    ) {

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

            String[] parts =
                    currentBirthday.split("/");

            if (parts.length == 3) {

                try {

                    selectedDay =
                            Integer.parseInt(
                                    parts[0]
                            );

                    selectedMonth =
                            Integer.parseInt(
                                    parts[1]
                            ) - 1;

                    selectedYear =
                            Integer.parseInt(
                                    parts[2]
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

        DatePickerDialog dialog =
                new DatePickerDialog(
                        AccountActivity.this,
                        (view,
                         year,
                         month,
                         dayOfMonth) -> {

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
                        },
                        selectedYear,
                        selectedMonth,
                        selectedDay
                );

        dialog.getDatePicker()
                .setMaxDate(
                        System.currentTimeMillis()
                );

        dialog.show();
    }
    // =========================================================
    // XÁC NHẬN MẬT KHẨU TRƯỚC KHI ĐỔI EMAIL
    // =========================================================

    private void showConfirmPasswordForEmailChange(
            String fullName,
            String newEmail,
            String phone,
            String birthday,
            AlertDialog editDialog
    ) {

        currentUser = auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    AccountActivity.this,
                    "Bạn chưa đăng nhập",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Chỉ tài khoản đăng nhập bằng email và mật khẩu
         * mới có thể xác thực lại bằng mật khẩu hiện tại.
         */
        if (!hasPasswordProvider(currentUser)) {

            Toast.makeText(
                    AccountActivity.this,
                    "Tài khoản Google hoặc Facebook cần đăng nhập lại bằng nhà cung cấp tương ứng để đổi email",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String oldEmail =
                currentUser.getEmail();

        if (oldEmail == null
                || oldEmail.trim().isEmpty()) {

            Toast.makeText(
                    AccountActivity.this,
                    "Không lấy được email hiện tại",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        EditText edtPassword =
                createPasswordEditText(
                        "Nhập mật khẩu hiện tại"
                );

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                dp(20),
                dp(8),
                dp(20),
                0
        );

        container.addView(
                edtPassword,
                createInputLayoutParams()
        );

        TextView tvNotice =
                new TextView(this);

        tvNotice.setText(
                "Sau khi xác thực mật khẩu, Firebase sẽ gửi liên kết xác nhận đến email mới:\n"
                        + newEmail
        );

        tvNotice.setTextSize(13);

        tvNotice.setPadding(
                dp(4),
                dp(10),
                dp(4),
                0
        );

        container.addView(
                tvNotice
        );

        AlertDialog passwordDialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Xác nhận đổi email"
                        )
                        .setView(container)
                        .setPositiveButton(
                                "Gửi xác nhận",
                                null
                        )
                        .setNegativeButton(
                                "Hủy",
                                null
                        )
                        .create();

        passwordDialog.setOnShowListener(
                ignored -> {

                    passwordDialog
                            .getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            )
                            .setOnClickListener(
                                    view -> {

                                        edtPassword.setError(null);

                                        String password =
                                                edtPassword
                                                        .getText()
                                                        .toString();

                                        if (password.isEmpty()) {

                                            edtPassword.setError(
                                                    "Vui lòng nhập mật khẩu hiện tại"
                                            );

                                            edtPassword.requestFocus();
                                            return;
                                        }

                                        passwordDialog
                                                .getButton(
                                                        AlertDialog.BUTTON_POSITIVE
                                                )
                                                .setEnabled(false);

                                        reauthenticateAndRequestEmailChange(
                                                oldEmail.trim(),
                                                password,
                                                fullName,
                                                newEmail,
                                                phone,
                                                birthday,
                                                editDialog,
                                                passwordDialog
                                        );
                                    }
                            );
                }
        );

        passwordDialog.show();
    }
    // =========================================================
    // XÁC THỰC LẠI VÀ GỬI LIÊN KẾT ĐỔI EMAIL
    // =========================================================

    private void reauthenticateAndRequestEmailChange(
            String oldEmail,
            String password,
            String fullName,
            String newEmail,
            String phone,
            String birthday,
            AlertDialog editDialog,
            AlertDialog passwordDialog
    ) {

        currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            enableEmailChangeButton(
                    passwordDialog
            );

            Toast.makeText(
                    AccountActivity.this,
                    "Bạn chưa đăng nhập",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (oldEmail == null
                || oldEmail.trim().isEmpty()) {

            enableEmailChangeButton(
                    passwordDialog
            );

            Toast.makeText(
                    AccountActivity.this,
                    "Không lấy được email hiện tại",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (newEmail == null
                || newEmail.trim().isEmpty()) {

            enableEmailChangeButton(
                    passwordDialog
            );

            Toast.makeText(
                    AccountActivity.this,
                    "Email mới không hợp lệ",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        AuthCredential credential =
                EmailAuthProvider.getCredential(
                        oldEmail.trim(),
                        password
                );

        currentUser
                .reauthenticate(credential)
                .addOnSuccessListener(
                        unused -> {

                            /*
                             * Firebase gửi liên kết xác nhận đến email mới.
                             *
                             * Email đăng nhập chỉ được thay đổi sau khi
                             * người dùng bấm vào liên kết xác nhận.
                             */
                            currentUser
                                    .verifyBeforeUpdateEmail(
                                            newEmail.trim()
                                    )
                                    .addOnSuccessListener(
                                            emailUnused -> {

                                                /*
                                                 * Chưa lưu email mới vào
                                                 * AccountStorage vì email
                                                 * chưa được xác minh.
                                                 *
                                                 * Tạm thời tiếp tục giữ
                                                 * email hiện tại.
                                                 */
                                                AccountStorage.saveProfile(
                                                        AccountActivity.this,
                                                        currentUserId,
                                                        fullName,
                                                        oldEmail.trim(),
                                                        phone,
                                                        birthday
                                                );

                                                /*
                                                 * Cập nhật tên hiển thị
                                                 * trên Firebase.
                                                 */
                                                UserProfileChangeRequest
                                                        profileRequest =
                                                        new UserProfileChangeRequest
                                                                .Builder()
                                                                .setDisplayName(
                                                                        fullName
                                                                )
                                                                .build();

                                                currentUser
                                                        .updateProfile(
                                                                profileRequest
                                                        )
                                                        .addOnCompleteListener(
                                                                profileTask -> {

                                                                    passwordDialog
                                                                            .dismiss();

                                                                    editDialog
                                                                            .dismiss();

                                                                    loadAccountInfo();

                                                                    showEmailVerificationSentDialog(
                                                                            newEmail
                                                                    );
                                                                }
                                                        );
                                            }
                                    )
                                    .addOnFailureListener(
                                            exception -> {

                                                enableEmailChangeButton(
                                                        passwordDialog
                                                );

                                                Toast.makeText(
                                                        AccountActivity.this,
                                                        "Không thể gửi liên kết xác nhận: "
                                                                + getErrorMessage(
                                                                exception
                                                        ),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        exception -> {

                            enableEmailChangeButton(
                                    passwordDialog
                            );

                            edtPasswordErrorMessage(
                                    exception
                            );
                        }
                );
    }

    // =========================================================
    // HIỆN LẠI NÚT GỬI XÁC NHẬN KHI CÓ LỖI
    // =========================================================

    private void enableEmailChangeButton(
            AlertDialog passwordDialog
    ) {

        if (passwordDialog == null) {
            return;
        }

        if (passwordDialog.getButton(
                AlertDialog.BUTTON_POSITIVE
        ) != null) {

            passwordDialog
                    .getButton(
                            AlertDialog.BUTTON_POSITIVE
                    )
                    .setEnabled(true);
        }
    }

    // =========================================================
    // THÔNG BÁO ĐÃ GỬI EMAIL XÁC NHẬN
    // =========================================================

    private void showEmailVerificationSentDialog(
            String newEmail
    ) {

        new AlertDialog.Builder(
                AccountActivity.this
        )
                .setTitle(
                        "Đã gửi email xác nhận"
                )
                .setMessage(
                        "Firebase đã gửi một liên kết xác nhận đến:\n\n"
                                + newEmail
                                + "\n\n"
                                + "Bạn hãy mở hộp thư và bấm vào liên kết xác nhận."
                                + "\n\nSau khi xác nhận thành công, hãy quay lại ứng dụng để email mới được cập nhật."
                )
                .setPositiveButton(
                        "Đã hiểu",
                        null
                )
                .show();
    }
    // =========================================================
    // ĐỔI MẬT KHẨU
    // =========================================================

    private void showChangePasswordDialog() {

        currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    AccountActivity.this,
                    "Bạn chưa đăng nhập",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Tài khoản Google hoặc Facebook có thể
         * không sử dụng mật khẩu Firebase.
         */
        if (!hasPasswordProvider(currentUser)) {

            Toast.makeText(
                    AccountActivity.this,
                    "Tài khoản Google hoặc Facebook không sử dụng mật khẩu tại ứng dụng",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String userEmail =
                currentUser.getEmail();

        if (userEmail == null
                || userEmail.trim().isEmpty()) {

            Toast.makeText(
                    AccountActivity.this,
                    "Không lấy được email tài khoản",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                dp(20),
                dp(8),
                dp(20),
                0
        );

        EditText edtOldPassword =
                createPasswordEditText(
                        "Mật khẩu hiện tại"
                );

        EditText edtNewPassword =
                createPasswordEditText(
                        "Mật khẩu mới"
                );

        EditText edtConfirmPassword =
                createPasswordEditText(
                        "Nhập lại mật khẩu mới"
                );

        container.addView(
                edtOldPassword,
                createInputLayoutParams()
        );

        container.addView(
                edtNewPassword,
                createInputLayoutParams()
        );

        container.addView(
                edtConfirmPassword,
                createInputLayoutParams()
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Đổi mật khẩu"
                        )
                        .setView(container)
                        .setPositiveButton(
                                "Đổi mật khẩu",
                                null
                        )
                        .setNegativeButton(
                                "Hủy",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                ignored -> {

                    dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            )
                            .setOnClickListener(
                                    view -> {

                                        String oldPassword =
                                                edtOldPassword
                                                        .getText()
                                                        .toString();

                                        String newPassword =
                                                edtNewPassword
                                                        .getText()
                                                        .toString();

                                        String confirmPassword =
                                                edtConfirmPassword
                                                        .getText()
                                                        .toString();

                                        boolean valid =
                                                validatePasswords(
                                                        edtOldPassword,
                                                        edtNewPassword,
                                                        edtConfirmPassword,
                                                        oldPassword,
                                                        newPassword,
                                                        confirmPassword
                                                );

                                        if (!valid) {
                                            return;
                                        }

                                        dialog.getButton(
                                                        AlertDialog.BUTTON_POSITIVE
                                                )
                                                .setEnabled(false);

                                        reauthenticateAndChangePassword(
                                                userEmail.trim(),
                                                oldPassword,
                                                newPassword,
                                                dialog
                                        );
                                    }
                            );
                }
        );

        dialog.show();
    }

    // =========================================================
    // KIỂM TRA NHÀ CUNG CẤP ĐĂNG NHẬP
    // =========================================================

    private boolean hasPasswordProvider(
            FirebaseUser user
    ) {

        if (user == null) {
            return false;
        }

        for (UserInfo userInfo :
                user.getProviderData()) {

            if (EmailAuthProvider.PROVIDER_ID.equals(
                    userInfo.getProviderId()
            )) {

                return true;
            }
        }

        return false;
    }

    // =========================================================
    // KIỂM TRA MẬT KHẨU
    // =========================================================

    private boolean validatePasswords(
            EditText edtOldPassword,
            EditText edtNewPassword,
            EditText edtConfirmPassword,
            String oldPassword,
            String newPassword,
            String confirmPassword
    ) {

        edtOldPassword.setError(null);
        edtNewPassword.setError(null);
        edtConfirmPassword.setError(null);

        if (oldPassword.isEmpty()) {

            edtOldPassword.setError(
                    "Vui lòng nhập mật khẩu hiện tại"
            );

            edtOldPassword.requestFocus();
            return false;
        }

        if (newPassword.isEmpty()) {

            edtNewPassword.setError(
                    "Vui lòng nhập mật khẩu mới"
            );

            edtNewPassword.requestFocus();
            return false;
        }

        if (newPassword.length() < 6) {

            edtNewPassword.setError(
                    "Mật khẩu mới phải có ít nhất 6 ký tự"
            );

            edtNewPassword.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {

            edtConfirmPassword.setError(
                    "Vui lòng nhập lại mật khẩu mới"
            );

            edtConfirmPassword.requestFocus();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {

            edtConfirmPassword.setError(
                    "Mật khẩu nhập lại không khớp"
            );

            edtConfirmPassword.requestFocus();
            return false;
        }

        if (newPassword.equals(oldPassword)) {

            edtNewPassword.setError(
                    "Mật khẩu mới phải khác mật khẩu hiện tại"
            );

            edtNewPassword.requestFocus();
            return false;
        }

        return true;
    }
    // =========================================================
    // XÁC THỰC LẠI VÀ ĐỔI MẬT KHẨU FIREBASE
    // =========================================================

    private void reauthenticateAndChangePassword(
            String email,
            String oldPassword,
            String newPassword,
            AlertDialog dialog
    ) {

        currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {

            if (dialog != null
                    && dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ) != null) {

                dialog.getButton(
                        AlertDialog.BUTTON_POSITIVE
                ).setEnabled(true);
            }

            Toast.makeText(
                    AccountActivity.this,
                    "Bạn chưa đăng nhập",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (email == null
                || email.trim().isEmpty()) {

            if (dialog != null
                    && dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ) != null) {

                dialog.getButton(
                        AlertDialog.BUTTON_POSITIVE
                ).setEnabled(true);
            }

            Toast.makeText(
                    AccountActivity.this,
                    "Không lấy được email tài khoản",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        AuthCredential credential =
                EmailAuthProvider.getCredential(
                        email.trim(),
                        oldPassword
                );

        currentUser
                .reauthenticate(credential)
                .addOnSuccessListener(
                        unused -> {

                            currentUser
                                    .updatePassword(newPassword)
                                    .addOnSuccessListener(
                                            updateUnused -> {

                                                Toast.makeText(
                                                        AccountActivity.this,
                                                        "Đổi mật khẩu thành công",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                dialog.dismiss();
                                            }
                                    )
                                    .addOnFailureListener(
                                            exception -> {

                                                if (dialog.getButton(
                                                        AlertDialog.BUTTON_POSITIVE
                                                ) != null) {

                                                    dialog.getButton(
                                                            AlertDialog.BUTTON_POSITIVE
                                                    ).setEnabled(true);
                                                }

                                                Toast.makeText(
                                                        AccountActivity.this,
                                                        "Không thể đổi mật khẩu: "
                                                                + getErrorMessage(
                                                                exception
                                                        ),
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                    );
                        }
                )
                .addOnFailureListener(
                        exception -> {

                            if (dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            ) != null) {

                                dialog.getButton(
                                        AlertDialog.BUTTON_POSITIVE
                                ).setEnabled(true);
                            }

                            edtPasswordErrorMessage(
                                    exception
                            );
                        }
                );
    }

    // =========================================================
    // HIỂN THỊ LỖI MẬT KHẨU
    // =========================================================

    private void edtPasswordErrorMessage(
            Exception exception
    ) {

        String errorMessage =
                getErrorMessage(
                        exception
                );

        Toast.makeText(
                AccountActivity.this,
                "Mật khẩu hiện tại không đúng hoặc đã xảy ra lỗi: "
                        + errorMessage,
                Toast.LENGTH_LONG
        ).show();
    }
    // =========================================================
    // TẠO EDITTEXT
    // =========================================================

    private EditText createEditText(
            String hint,
            int inputType
    ) {

        EditText editText =
                new EditText(this);

        editText.setHint(hint);

        editText.setInputType(
                inputType
        );

        editText.setSingleLine(true);

        editText.setTextSize(15);

        editText.setPadding(
                dp(12),
                dp(12),
                dp(12),
                dp(12)
        );

        return editText;
    }

    // =========================================================
    // TẠO EDITTEXT MẬT KHẨU
    // =========================================================

    private EditText createPasswordEditText(
            String hint
    ) {

        EditText editText =
                createEditText(
                        hint,
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_PASSWORD
                );

        editText.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_lock,
                0,
                0,
                0
        );

        editText.setCompoundDrawablePadding(
                dp(10)
        );

        return editText;
    }

    // =========================================================
    // LAYOUT PARAMS
    // =========================================================

    private LinearLayout.LayoutParams
    createInputLayoutParams() {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                dp(8),
                0,
                0
        );

        return params;
    }

    // =========================================================
    // HIỂN THỊ GIÁ TRỊ
    // =========================================================

    private String displayValue(
            String value,
            String defaultValue
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return defaultValue;
        }

        return value.trim();
    }

    // =========================================================
    // LẤY THÔNG BÁO LỖI
    // =========================================================

    private String getErrorMessage(
            Exception exception
    ) {

        if (exception == null
                || exception.getMessage() == null
                || exception.getMessage().trim().isEmpty()) {

            return "Đã xảy ra lỗi";
        }

        return exception.getMessage();
    }

    // =========================================================
    // DP -> PX
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