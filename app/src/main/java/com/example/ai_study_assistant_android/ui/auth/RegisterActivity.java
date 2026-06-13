package com.example.ai_study_assistant_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_study_assistant_android.R;
import com.example.ai_study_assistant_android.model.AuthResponse;
import com.example.ai_study_assistant_android.model.RegisterRequest;
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

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilEmail, tilPassword;
    private TextInputEditText etUsername, etEmail, etPassword;
    private MaterialButton btnRegister;
    private TextView tvError, tvLoginLink;
    private CircularProgressIndicator progress;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tokenManager = TokenManager.getInstance(this);

        tilUsername = findViewById(R.id.til_username);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        tvError = findViewById(R.id.tv_error);
        tvLoginLink = findViewById(R.id.tv_login_link);
        progress = findViewById(R.id.progress);

        tvLoginLink.setText(Html.fromHtml(getString(R.string.link_to_login), Html.FROM_HTML_MODE_LEGACY));

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tvError.setVisibility(View.GONE);

        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        boolean valid = true;
        if (username.isEmpty()) {
            tilUsername.setError("Username is required");
            valid = false;
        } else if (username.length() < 3) {
            tilUsername.setError("Username must be at least 3 characters");
            valid = false;
        }
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            valid = false;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            valid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("At least 6 characters required");
            valid = false;
        }
        if (!valid)
            return;

        setLoading(true);

        ApiClient.getInstance(this).getApiService()
                .register(new RegisterRequest(username, email, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse body = response.body();
                            tokenManager.saveSession(body.getToken(), body.getUsername(), body.getUserId());
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else if (response.code() == 409) {
                            showError(getString(R.string.error_email_taken));
                        } else {
                            showError(getString(R.string.error_server));
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        setLoading(false);
                        showError(getString(R.string.error_network));
                    }
                });
    }

    private void setLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
