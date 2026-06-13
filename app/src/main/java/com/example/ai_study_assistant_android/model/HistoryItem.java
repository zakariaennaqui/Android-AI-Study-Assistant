package com.example.ai_study_assistant_android.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class HistoryItem implements Serializable {

    @SerializedName("sessionId")
    private String sessionId;

    @SerializedName("type")
    private String type;   // "SUMMARY" | "QUIZ" | "FLASHCARDS"

    @SerializedName("subject")
    private String subject;

    @SerializedName("createdAt")
    private String createdAt;

    public String getSessionId() { return sessionId; }
    public String getType() { return type; }
    public String getSubject() { return subject; }
    public String getCreatedAt() { return createdAt; }
}
