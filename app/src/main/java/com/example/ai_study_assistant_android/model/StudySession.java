package com.example.ai_study_assistant_android.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class StudySession implements Serializable {

    @SerializedName("sessionId")
    private String sessionId;

    @SerializedName("type")
    private String type;   // "SUMMARY" | "QUIZ" | "FLASHCARDS"

    @SerializedName("subject")
    private String subject;

    @SerializedName("summary")
    private String summary;

    @SerializedName("questions")
    private List<QuizQuestion> questions;

    @SerializedName("flashcards")
    private List<Flashcard> flashcards;

    @SerializedName("createdAt")
    private String createdAt;

    /** Original material text; used for follow-up generation from results. */
    @SerializedName("sourceText")
    private String sourceText;

    public String getSessionId() { return sessionId; }
    public String getType() { return type; }
    public String getSubject() { return subject; }
    public String getSummary() { return summary; }
    public List<QuizQuestion> getQuestions() { return questions; }
    public List<Flashcard> getFlashcards() { return flashcards; }
    public String getCreatedAt() { return createdAt; }
    public String getSourceText() { return sourceText; }
}
