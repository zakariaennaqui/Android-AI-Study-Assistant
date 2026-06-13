package com.example.ai_study_assistant_android.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class QuizQuestion implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("question")
    private String question;

    @SerializedName("choices")
    private List<String> choices;

    @SerializedName("correctIndex")
    private int correctIndex;

    @SerializedName("explanation")
    private String explanation;

    public String getQuestion() { return question; }
    public List<String> getChoices() { return choices; }
    public int getCorrectIndex() { return correctIndex; }
    public String getExplanation() { return explanation; }
}