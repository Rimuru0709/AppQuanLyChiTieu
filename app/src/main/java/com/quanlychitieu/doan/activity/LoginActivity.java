package com.quanlychitieu.doan.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.home.HomeActivity;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final String PREF_USER = "USER";
    private static final String KEY_IS_LOGIN = "isLogin";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_REMEMBER_LOGIN = "rememberLogin";

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private SharedPreferences preferences;

    private EditText edtEmail;
    private EditText edtPassword;

    private AppCompatCheckBox chkRemember;

    private Button btnLogin;
    private MaterialButton btnGoogle;
    private MaterialButton btnFacebook;

    private TextView tvSignUp;
    private TextView txtForgotPassword;

    private boolean passwordVisible = false;
    private boolean loginProcessing = false;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> handleGoogleSignInResult(result.getData())
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initFirebase();
        initPreferences();
        initViews();

        setupSafeArea();
        setupGoogleSignIn();
        setupFacebookLogin();
        setupEvents();
        setupPasswordToggle();
        loadSavedLoginInformation();

        /*
         * Không kiểm tra isLogin để tự chuyển Home tại đây.
         *
         * Vì HomeActivity là màn hình mở đầu.
         * LoginActivity chỉ xuất hiện khi người dùng chủ động bấm đăng nhập.
         */
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();
    }

    private void initPreferences() {
        preferences = getSharedPreferences(
                PREF_USER,
                MODE_PRIVATE
        );
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        chkRemember = findViewById(R.id.chkRemember);

        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);

        tvSignUp = findViewById(R.id.tvSignUp);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                content,
                (view, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            dp(28),
                            systemBars.top + dp(24),
                            dp(28),
                            systemBars.bottom + dp(28)
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(content);
    }

    private void setupEvents() {
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    SignUpActivity.class
            );

            startActivity(intent);
        });

        txtForgotPassword.setOnClickListener(v ->
                resetPassword()
        );

        btnLogin.setOnClickListener(v ->
                loginWithEmailAndPassword()
        );

        btnGoogle.setOnClickListener(v ->
                startGoogleLogin()
        );

        btnFacebook.setOnClickListener(v ->
                startFacebookLogin()
        );

        edtEmail.setOnEditorActionListener(
                (view, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        edtPassword.requestFocus();
                        return true;
                    }

                    return false;
                }
        );

        edtPassword.setOnEditorActionListener(
                (view, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        hideKeyboard();
                        loginWithEmailAndPassword();
                        return true;
                    }

                    return false;
                }
        );
    }

    private void loginWithEmailAndPassword() {
        if (loginProcessing) {
            return;
        }

        hideKeyboard();

        String email = edtEmail
                .getText()
                .toString()
                .trim();

        String password = edtPassword
                .getText()
                .toString()
                .trim();

        edtEmail.setError(null);
        edtPassword.setError(null);

        if (email.isEmpty()) {
            edtEmail.setError(
                    "Vui lòng nhập email"
            );

            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError(
                    "Địa chỉ email không hợp lệ"
            );

            edtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError(
                    "Vui lòng nhập mật khẩu"
            );

            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError(
                    "Mật khẩu phải có ít nhất 6 ký tự"
            );

            edtPassword.requestFocus();
            return;
        }

        setLoginProcessing(true);

        auth.signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnSuccessListener(authResult -> {
                    saveLoginInformation(email);

                    Toast.makeText(
                            LoginActivity.this,
                            "Đăng nhập thành công",
                            Toast.LENGTH_SHORT
                    ).show();

                    goToHome();
                })
                .addOnFailureListener(exception -> {
                    setLoginProcessing(false);

                    Toast.makeText(
                            LoginActivity.this,
                            "Đăng nhập thất bại: "
                                    + getErrorMessage(exception),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions googleSignInOptions =
                new GoogleSignInOptions.Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN
                )
                        .requestIdToken(
                                getString(
                                        R.string.default_web_client_id
                                )
                        )
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(
                this,
                googleSignInOptions
        );
    }

    private void startGoogleLogin() {
        if (loginProcessing) {
            return;
        }

        hideKeyboard();

        Intent signInIntent =
                googleSignInClient.getSignInIntent();

        googleLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(
            Intent data
    ) {
        if (data == null) {
            Toast.makeText(
                    this,
                    "Không nhận được dữ liệu đăng nhập Google",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Task<GoogleSignInAccount> task =
                GoogleSignIn
                        .getSignedInAccountFromIntent(data);

        try {
            GoogleSignInAccount account =
                    task.getResult(ApiException.class);

            if (account == null) {
                Toast.makeText(
                        this,
                        "Không nhận được tài khoản Google",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            firebaseAuthWithGoogle(
                    account.getIdToken()
            );

        } catch (ApiException exception) {
            Toast.makeText(
                    this,
                    "Đăng nhập Google thất bại",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void firebaseAuthWithGoogle(
            String idToken
    ) {
        if (idToken == null || idToken.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Không lấy được Google Token",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        setLoginProcessing(true);

        AuthCredential credential =
                GoogleAuthProvider.getCredential(
                        idToken,
                        null
                );

        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    String email =
                            getCurrentUserEmail();

                    saveLoginInformation(email);

                    Toast.makeText(
                            LoginActivity.this,
                            "Đăng nhập Google thành công",
                            Toast.LENGTH_SHORT
                    ).show();

                    goToHome();
                })
                .addOnFailureListener(exception -> {
                    setLoginProcessing(false);

                    Toast.makeText(
                            LoginActivity.this,
                            "Lỗi Google: "
                                    + getErrorMessage(exception),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void setupFacebookLogin() {
        LoginManager.getInstance()
                .registerCallback(
                        callbackManager,
                        new FacebookCallback<LoginResult>() {

                            @Override
                            public void onSuccess(
                                    LoginResult loginResult
                            ) {
                                firebaseAuthWithFacebook(
                                        loginResult
                                );
                            }

                            @Override
                            public void onCancel() {
                                setLoginProcessing(false);

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Bạn đã hủy đăng nhập Facebook",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                            @Override
                            public void onError(
                                    FacebookException error
                            ) {
                                setLoginProcessing(false);

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Lỗi Facebook: "
                                                + getErrorMessage(error),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    private void startFacebookLogin() {
        if (loginProcessing) {
            return;
        }

        hideKeyboard();

        LoginManager.getInstance()
                .logInWithReadPermissions(
                        LoginActivity.this,
                        Arrays.asList(
                                "public_profile",
                                "email"
                        )
                );
    }

    private void firebaseAuthWithFacebook(
            LoginResult loginResult
    ) {
        if (loginResult == null ||
                loginResult.getAccessToken() == null) {

            Toast.makeText(
                    this,
                    "Không lấy được Facebook Token",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        setLoginProcessing(true);

        AuthCredential credential =
                FacebookAuthProvider.getCredential(
                        loginResult
                                .getAccessToken()
                                .getToken()
                );

        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    String email =
                            getCurrentUserEmail();

                    saveLoginInformation(email);

                    Toast.makeText(
                            LoginActivity.this,
                            "Đăng nhập Facebook thành công",
                            Toast.LENGTH_SHORT
                    ).show();

                    goToHome();
                })
                .addOnFailureListener(exception -> {
                    setLoginProcessing(false);

                    Toast.makeText(
                            LoginActivity.this,
                            "Lỗi Facebook: "
                                    + getErrorMessage(exception),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void resetPassword() {
        String email = edtEmail
                .getText()
                .toString()
                .trim();

        edtEmail.setError(null);

        if (email.isEmpty()) {
            edtEmail.setError(
                    "Nhập email để đặt lại mật khẩu"
            );

            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError(
                    "Địa chỉ email không hợp lệ"
            );

            edtEmail.requestFocus();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Toast.makeText(
                                LoginActivity.this,
                                "Đã gửi liên kết đặt lại mật khẩu đến email",
                                Toast.LENGTH_LONG
                        ).show()
                )
                .addOnFailureListener(exception ->
                        Toast.makeText(
                                LoginActivity.this,
                                "Không thể gửi email: "
                                        + getErrorMessage(exception),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void saveLoginInformation(
            String email
    ) {
        boolean rememberLogin =
                chkRemember != null &&
                        chkRemember.isChecked();

        SharedPreferences.Editor editor =
                preferences.edit();

        /*
         * Người dùng đã đăng nhập thành công.
         * Home và AccountActivity có thể đọc trạng thái này.
         */
        editor.putBoolean(
                KEY_IS_LOGIN,
                true
        );

        editor.putBoolean(
                KEY_REMEMBER_LOGIN,
                rememberLogin
        );

        editor.putString(
                KEY_EMAIL,
                email == null ? "" : email
        );

        editor.apply();
    }

    private void loadSavedLoginInformation() {
        boolean rememberLogin =
                preferences.getBoolean(
                        KEY_REMEMBER_LOGIN,
                        false
                );

        String savedEmail =
                preferences.getString(
                        KEY_EMAIL,
                        ""
                );

        chkRemember.setChecked(rememberLogin);

        if (rememberLogin &&
                savedEmail != null &&
                !savedEmail.trim().isEmpty()) {

            edtEmail.setText(savedEmail);

            edtEmail.setSelection(
                    savedEmail.length()
            );
        }
    }

    private String getCurrentUserEmail() {
        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null ||
                currentUser.getEmail() == null) {

            return "";
        }

        return currentUser.getEmail();
    }

    private void goToHome() {
        Intent intent = new Intent(
                LoginActivity.this,
                HomeActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
        finish();
    }

    private void setLoginProcessing(
            boolean processing
    ) {
        loginProcessing = processing;

        btnLogin.setEnabled(!processing);
        btnGoogle.setEnabled(!processing);
        btnFacebook.setEnabled(!processing);

        edtEmail.setEnabled(!processing);
        edtPassword.setEnabled(!processing);

        chkRemember.setEnabled(!processing);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle() {
        edtPassword.setOnTouchListener(
                (view, event) -> {

                    if (event.getAction() !=
                            MotionEvent.ACTION_UP) {

                        return false;
                    }

                    if (edtPassword
                            .getCompoundDrawables()[2] == null) {

                        return false;
                    }

                    int drawableWidth =
                            edtPassword
                                    .getCompoundDrawables()[2]
                                    .getBounds()
                                    .width();

                    float iconStartPosition =
                            edtPassword.getRight()
                                    - edtPassword.getPaddingEnd()
                                    - drawableWidth;

                    if (event.getRawX() <
                            iconStartPosition) {

                        return false;
                    }

                    view.performClick();

                    passwordVisible =
                            !passwordVisible;

                    if (passwordVisible) {
                        edtPassword.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType
                                                .TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );

                        edtPassword
                                .setCompoundDrawablesWithIntrinsicBounds(
                                        R.drawable.ic_lock,
                                        0,
                                        R.drawable.ic_eye,
                                        0
                                );

                    } else {
                        edtPassword.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType
                                                .TYPE_TEXT_VARIATION_PASSWORD
                        );

                        edtPassword
                                .setCompoundDrawablesWithIntrinsicBounds(
                                        R.drawable.ic_lock,
                                        0,
                                        R.drawable.ic_eye_off,
                                        0
                                );
                    }

                    edtPassword.setCompoundDrawablePadding(
                            dp(10)
                    );

                    edtPassword.setSelection(
                            edtPassword
                                    .getText()
                                    .length()
                    );

                    return true;
                }
        );
    }

    @Override
    public boolean dispatchTouchEvent(
            MotionEvent event
    ) {
        if (event.getAction() ==
                MotionEvent.ACTION_DOWN) {

            View currentView =
                    getCurrentFocus();

            if (currentView instanceof EditText) {
                hideKeyboard();
                currentView.clearFocus();
            }
        }

        return super.dispatchTouchEvent(event);
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager)
                        getSystemService(
                                INPUT_METHOD_SERVICE
                        );

        View currentView =
                getCurrentFocus();

        if (inputMethodManager != null &&
                currentView != null) {

            inputMethodManager
                    .hideSoftInputFromWindow(
                            currentView.getWindowToken(),
                            0
                    );
        }
    }

    private String getErrorMessage(
            Exception exception
    ) {
        if (exception == null ||
                exception.getMessage() == null ||
                exception.getMessage()
                        .trim()
                        .isEmpty()) {

            return "Đã xảy ra lỗi";
        }

        return exception.getMessage();
    }

    private int dp(int value) {
        return Math.round(
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (callbackManager != null) {
            callbackManager.onActivityResult(
                    requestCode,
                    resultCode,
                    data
            );
        }
    }
}