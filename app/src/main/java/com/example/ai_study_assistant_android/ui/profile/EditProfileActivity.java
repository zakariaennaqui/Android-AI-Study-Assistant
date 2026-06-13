package com.example.ai_study_assistant_android.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_study_assistant_android.R;
import com.example.ai_study_assistant_android.model.AuthResponse;
import com.example.ai_study_assistant_android.model.MeResponse;
import com.example.ai_study_assistant_android.model.UpdateProfileRequest;
import com.example.ai_study_assistant_android.network.ApiClient;
import com.example.ai_study_assistant_android.network.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilEmail, tilCurrentPassword, tilNewPassword, tilConfirmPassword;
    private TextInputEditText etUsername, etEmail, etCurrentPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnBack, btnSave;
    private TextView tvError;
    private CircularProgressIndicator progress;

    private TokenManager tokenManager;

    private String originalUsername = "";
    private String originalEmail = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ProfileFragment.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        tokenManager = TokenManager.getInstance(this);

        tilUsername = findViewById(R.id.til_username);
        tilEmail = findViewById(R.id.til_email);
        tilCurrentPassword = findViewById(R.id.til_current_password);
        tilNewPassword = findViewById(R.id.til_new_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save);
        tvError = findViewById(R.id.tv_error);
        progress = findViewById(R.id.progress);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> attemptSave());

        loadProfile();
    }

    private void loadProfile() {
        setFormEnabled(false);
        progress.setVisibility(View.VISIBLE);
        ApiClient.getInstance(this).getApiService()
                .me()
                .enqueue(new Callback<MeResponse>() {
                    @Override
                    public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                        progress.setVisibility(View.GONE);
                        setFormEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            MeResponse me = response.body();
                            originalUsername = me.getUsername() != null ? me.getUsername() : "";
                            originalEmail = me.getEmail() != null ? me.getEmail() : "";
                            etUsername.setText(originalUsername);
                            etEmail.setText(originalEmail);
                        } else {
                            showError(getString(R.string.error_server));
                        }
                    }

                    @Override
                    public void onFailure(Call<MeResponse> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        setFormEnabled(true);
                        showError(getString(R.string.error_network));
                    }
                });
    }

    private void attemptSave() {
        clearFieldErrors();
        tvError.setVisibility(View.GONE);

        String username = textOf(etUsername);
        String email = textOf(etEmail);
        String currentPassword = textOf(etCurrentPassword);
        String newPassword = textOf(etNewPassword);
        String confirmPassword = textOf(etConfirmPassword);

        boolean changeUsername = !username.equals(originalUsername);
        boolean changeEmail = !email.equalsIgnoreCase(originalEmail);
        boolean changePassword = !newPassword.isEmpty();

        if (!changeUsername && !changeEmail && !changePassword) {
            showError(getString(R.string.error_profile_no_changes));
            return;
        }

        if (username.isEmpty()) {
            tilUsername.setError(getString(R.string.error_username_required));
            return;
        }

        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.error_email_required));
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            return;
        }

        if (changePassword) {
            if (newPassword.length() < 8) {
                tilNewPassword.setError(getString(R.string.error_new_password_length));
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
                return;
            }
        } else if (!confirmPassword.isEmpty()) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            return;
        }

        if (changeEmail || changePassword) {
            if (currentPassword.isEmpty()) {
                tilCurrentPassword.setError(getString(R.string.error_current_password_required));
                return;
            }
        }

        String usernamePayload = changeUsername ? username : null;
        String emailPayload = changeEmail ? email.trim() : null;
        String newPasswordPayload = changePassword ? newPassword : null;
        String currentPasswordPayload = (changeEmail || changePassword) ? currentPassword : null;

        UpdateProfileRequest body = new UpdateProfileRequest(
                usernamePayload,
                emailPayload,
                currentPasswordPayload,
                newPasswordPayload
        );

        setLoading(true);
        ApiClient.getInstance(this).getApiService()
                .updateProfile(body)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();
                            tokenManager.saveSession(auth.getToken(), auth.getUsername(), auth.getUserId());
                            finish();
                        } else {
                            String msg = parseErrorMessage(response);
                            if (msg != null && !msg.isEmpty()) {
                                showError(msg);
                            } else if (response.code() == 401) {
                                showError(getString(R.string.error_current_password_wrong));
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

    private static String textOf(TextInputEditText et) {
        if (et.getText() == null) return "";
        return et.getText().toString().trim();
    }

    private void clearFieldErrors() {
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilCurrentPassword.setError(null);
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void setFormEnabled(boolean enabled) {
        etUsername.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etCurrentPassword.setEnabled(enabled);
        etNewPassword.setEnabled(enabled);
        etConfirmPassword.setEnabled(enabled);
        btnSave.setEnabled(enabled);
    }

    private void setLoading(boolean loading) {
        btnSave.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    @Nullable
    private static String parseErrorMessage(Response<?> response) {
        ResponseBody err = response.errorBody();
        if (err == null) return null;
        try {
            String raw = err.string();
            JSONObject o = new JSONObject(raw);
            return o.optString("message", null);
        } catch (Exception ignored) {
            return null;
        }
    }
}
