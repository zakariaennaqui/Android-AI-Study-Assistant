package com.example.ai_study_assistant_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_study_assistant_android.R;
import com.example.ai_study_assistant_android.model.AuthRequest;
import com.example.ai_study_assistant_android.model.AuthResponse;
import com.example.ai_study_assistant_android.network.ApiClient;
import com.example.ai_study_assistant_android.network.TokenManager;
import com.example.ai_study_assistant_android.ui.main.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ai_study_assistant_android.ui.profile.ProfileFragment;

import java.io.IOException;
import java.io.InputStream;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvError, tvRegisterLink;
    private CircularProgressIndicator progress;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ProfileFragment.applySavedTheme(this);
        super.onCreate(savedInstanceState);

        tokenManager = TokenManager.getInstance(this);

        // Already logged in → go straight to main
        if (tokenManager.isLoggedIn()) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        tryLoadLoginBannerHero();

        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvError = findViewById(R.id.tv_error);
        tvRegisterLink = findViewById(R.id.tv_register_link);
        progress = findViewById(R.id.progress);

        tvRegisterLink.setText(Html.fromHtml(getString(R.string.link_to_register), Html.FROM_HTML_MODE_LEGACY));

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    /** Loads optional JPEG from assets (synced from repo-root login_image.jpg at build time). */
    private void tryLoadLoginBannerHero() {
        ImageView iv = findViewById(R.id.iv_login_hero);
        if (iv == null) {
            return;
        }
        try (InputStream in = getAssets().open("login_banner.jpg")) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 2;
            Bitmap bmp = BitmapFactory.decodeStream(in, null, opts);
            if (bmp != null) {
                iv.setImageBitmap(bmp);
            } else {
                iv.setImageResource(R.drawable.login_hero);
            }
        } catch (IOException ignored) {
            // Use @drawable/login_hero from layout
        }
    }

    private void attemptLogin() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        tvError.setVisibility(View.GONE);

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            return;
        }

        setLoading(true);

        ApiClient.getInstance(this).getApiService()
                .login(new AuthRequest(email, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse body = response.body();
                            tokenManager.saveSession(body.getToken(), body.getUsername(), body.getUserId());
                            goToMain();
                        } else {
                            int code = response.code();
                            if (code == 401 || code == 403) {
                                showError(getString(R.string.error_invalid_credentials));
                            } else {
                                showError(getString(R.string.error_server));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        setLoading(false);
                        showError(getString(R.string.error_network));
                    }
                });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
