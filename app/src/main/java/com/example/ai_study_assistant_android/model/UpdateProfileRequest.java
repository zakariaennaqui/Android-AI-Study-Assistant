package com.example.ai_study_assistant_android.model;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {

    @SerializedName("username")
    private final String username;

    @SerializedName("email")
    private final String email;

    @SerializedName("currentPassword")
    private final String currentPassword;

    @SerializedName("newPassword")
    private final String newPassword;

    public UpdateProfileRequest(String username, String email, String currentPassword, String newPassword) {
        this.username = username;
        this.email = email;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}
