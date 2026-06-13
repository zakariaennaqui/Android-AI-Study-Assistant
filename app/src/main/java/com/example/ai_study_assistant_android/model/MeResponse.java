package com.example.ai_study_assistant_android.model;

import com.google.gson.annotations.SerializedName;

public class MeResponse {
    @SerializedName("userId")
    private String userId;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}
