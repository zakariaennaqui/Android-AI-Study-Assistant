package com.example.ai_study_assistant_android.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("userId")
    private String userId;

    @SerializedName("username")
    private String username;

    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
}
