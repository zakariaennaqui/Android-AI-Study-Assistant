package com.example.ai_study_assistant_android.review;

/** Local record for a summary the user chose to review later. */
public class SummaryReviewEntry {

    public String sessionId;
    public String subject;
    public long addedAtMs;

    public SummaryReviewEntry() {
    }

    public SummaryReviewEntry(String sessionId, String subject, long addedAtMs) {
        this.sessionId = sessionId;
        this.subject = subject;
        this.addedAtMs = addedAtMs;
    }
}
