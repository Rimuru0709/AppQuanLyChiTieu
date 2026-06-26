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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.auth.GoogleAuthProvider;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.home.HomeActivity;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private SharedPreferences pref;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(data);

                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (Exception e) {
                            Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupSafeArea();

        auth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();
        pref = getSharedPreferences("USER", MODE_PRIVATE);

        if (pref.getBoolean("isLogin", false)) {
            goToHome();
            return;
        }

        TextView tvSignUp = findViewById(R.id.tvSignUp);
        TextView btnLogin = findViewById(R.id.btnLogin);

        MaterialButton btnGoogle = findViewById(R.id.btnGoogle);
        MaterialButton btnFacebook = findViewById(R.id.btnFacebook);

        EditText edtEmail = findViewById(R.id.edtEmail);
        EditText edtPassword = findViewById(R.id.edtPassword);

        setupGoogleSignIn();
        setupFacebookLogin();

        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
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
                        saveLogin(email);
                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        goToHome();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });

        btnFacebook.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(
                    LoginActivity.this,
                    Arrays.asList("public_profile")
            );
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

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    dp(28),
                    systemBars.top + dp(30),
                    dp(28),
                    dp(28)
            );

            return insets;
        });
    }

    private void setupGoogleSignIn() {
        String webClientId =
                "600184737039-bdt87j2h981fjkgvtlj0bui82uge6o31.apps.googleusercontent.com";

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(webClientId)
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Toast.makeText(this, "Không lấy được Google Token", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    String email = "";

                    if (auth.getCurrentUser() != null && auth.getCurrentUser().getEmail() != null) {
                        email = auth.getCurrentUser().getEmail();
                    }

                    saveLogin(email);
                    Toast.makeText(this, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show();
                    goToHome();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupFacebookLogin() {
        LoginManager.getInstance().registerCallback(
                callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AuthCredential credential =
                                FacebookAuthProvider.getCredential(
                                        loginResult.getAccessToken().getToken()
                                );

                        auth.signInWithCredential(credential)
                                .addOnSuccessListener(authResult -> {
                                    String email = "";

                                    if (auth.getCurrentUser() != null
                                            && auth.getCurrentUser().getEmail() != null) {
                                        email = auth.getCurrentUser().getEmail();
                                    }

                                    saveLogin(email);
                                    Toast.makeText(
                                            LoginActivity.this,
                                            "Đăng nhập Facebook thành công",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    goToHome();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(
                                            LoginActivity.this,
                                            "Lỗi Facebook: " + e.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                });
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(
                                LoginActivity.this,
                                "Bạn đã hủy đăng nhập Facebook",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(
                                LoginActivity.this,
                                "Lỗi Facebook: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void saveLogin(String email) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isLogin", true);
        editor.putString("email", email);
        editor.apply();
    }

    private void goToHome() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle(EditText edtPassword) {
        final boolean[] isVisible = {false};

        edtPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();

                if (edtPassword.getCompoundDrawables()[2] != null &&
                        event.getRawX() >= edtPassword.getRight()
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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}