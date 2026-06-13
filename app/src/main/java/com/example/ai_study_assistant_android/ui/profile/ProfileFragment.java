package com.example.ai_study_assistant_android.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.ai_study_assistant_android.R;
import com.example.ai_study_assistant_android.model.MeResponse;
import com.example.ai_study_assistant_android.network.ApiClient;
import com.example.ai_study_assistant_android.network.TokenManager;
import com.example.ai_study_assistant_android.ui.main.MainActivity;
import com.google.android.material.radiobutton.MaterialRadioButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String PREF_NAME = "ai_study_prefs";
    private static final String KEY_THEME = "theme_mode";

    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    private TextView tvAvatarInitial, tvUsername, tvUserEmail;
    private MaterialRadioButton rbSystem, rbLight, rbDark;
    private boolean updatingRadios = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAvatarInitial = view.findViewById(R.id.tv_avatar_initial);
        tvUsername = view.findViewById(R.id.tv_username);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        rbSystem = view.findViewById(R.id.rb_system);
        rbLight = view.findViewById(R.id.rb_light);
        rbDark = view.findViewById(R.id.rb_dark);

        TokenManager tm = TokenManager.getInstance(requireContext());
        String username = tm.getUsername();
        setUserDisplay(username, null);

        int saved = getThemePreference();
        setRadioSelection(saved);

        View.OnClickListener themeClick = v -> {
            if (updatingRadios) return;
            int mode;
            if (v == rbSystem) mode = THEME_SYSTEM;
            else if (v == rbLight) mode = THEME_LIGHT;
            else mode = THEME_DARK;
            setRadioSelection(mode);
            applyTheme(mode);
        };
        rbSystem.setOnClickListener(themeClick);
        rbLight.setOnClickListener(themeClick);
        rbDark.setOnClickListener(themeClick);

        view.findViewById(R.id.card_edit_profile).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));

        view.findViewById(R.id.card_sign_out).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).logout();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchMe();
    }

    private void setUserDisplay(String username, String email) {
        if (username != null && !username.isEmpty()) {
            tvUsername.setText(username);
            tvAvatarInitial.setText(String.valueOf(username.charAt(0)).toUpperCase());
        } else {
            tvUsername.setText("Student");
            tvAvatarInitial.setText("S");
        }
        if (email != null && !email.isEmpty()) {
            tvUserEmail.setVisibility(View.VISIBLE);
            tvUserEmail.setText(email);
        } else {
            tvUserEmail.setVisibility(View.GONE);
        }
    }

    private void fetchMe() {
        ApiClient.getInstance(requireContext()).getApiService()
                .me()
                .enqueue(new Callback<MeResponse>() {
                    @Override
                    public void onResponse(Call<MeResponse> call, Response<MeResponse> res) {
                        if (!isAdded()) return;
                        if (res.isSuccessful() && res.body() != null) {
                            MeResponse me = res.body();
                            TokenManager tm = TokenManager.getInstance(requireContext());
                            String freshName = me.getUsername();
                            if (freshName == null || freshName.isEmpty()) {
                                freshName = tm.getUsername();
                            }
                            tm.saveSession(tm.getToken(), freshName != null ? freshName : "", me.getUserId());
                            setUserDisplay(freshName, me.getEmail());
                        }
                    }

                    @Override
                    public void onFailure(Call<MeResponse> call, Throwable t) { }
                });
    }

    private void setRadioSelection(int mode) {
        updatingRadios = true;
        rbSystem.setChecked(mode == THEME_SYSTEM);
        rbLight.setChecked(mode == THEME_LIGHT);
        rbDark.setChecked(mode == THEME_DARK);
        updatingRadios = false;
    }

    private void applyTheme(int mode) {
        saveThemePreference(mode);
        switch (mode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private int getThemePreference() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, 0);
        return prefs.getInt(KEY_THEME, THEME_SYSTEM);
    }

    private void saveThemePreference(int mode) {
        requireContext().getSharedPreferences(PREF_NAME, 0)
                .edit()
                .putInt(KEY_THEME, mode)
                .apply();
    }

    public static void applySavedTheme(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, 0);
        int mode = prefs.getInt(KEY_THEME, THEME_SYSTEM);
        switch (mode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
