package com.example.ai_study_assistant_android.model;

import com.google.gson.annotations.SerializedName;

public class GenerateRequest {
    private final String text;
    private final String type;
    private final String subject;

    @SerializedName("quizQuestionCount")
    private final Integer quizQuestionCount;

    @SerializedName("summaryDepth")
    private final String summaryDepth;

    public GenerateRequest(String text, String type, String subject) {
        this(text, type, subject, null, null);
    }

    public GenerateRequest(String text, String type, String subject,
            Integer quizQuestionCount, String summaryDepth) {
        this.text = text;
        this.type = type;
        this.subject = subject;
        this.quizQuestionCount = quizQuestionCount;
        this.summaryDepth = summaryDepth;
    }

    public String getText() { return text; }
    public String getType() { return type; }
    public String getSubject() { return subject; }
    public Integer getQuizQuestionCount() { return quizQuestionCount; }
    public String getSummaryDepth() { return summaryDepth; }
}
