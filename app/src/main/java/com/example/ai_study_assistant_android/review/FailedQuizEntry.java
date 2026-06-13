package com.example.ai_study_assistant_android.review;

/** Local record for a quiz session that scored at most 50%; removed when score exceeds half. */
public class FailedQuizEntry {

    public String sessionId;
    public String subject;
    public int lastScore;
    public int lastTotal;
    public long updatedAtMs;

    public FailedQuizEntry() {
    }

    public FailedQuizEntry(String sessionId, String subject, int lastScore, int lastTotal, long updatedAtMs) {
        this.sessionId = sessionId;
        this.subject = subject;
        this.lastScore = lastScore;
        this.lastTotal = lastTotal;
        this.updatedAtMs = updatedAtMs;
    }
}
