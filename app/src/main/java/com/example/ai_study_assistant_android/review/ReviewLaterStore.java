package com.example.ai_study_assistant_android.review;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.ai_study_assistant_android.model.StudySession;
import com.example.ai_study_assistant_android.network.TokenManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks quizzes that scored at most 50% (in-app list only — no push notifications)
 * and summaries marked for review later. Stored per logged-in user on device.
 */
public class ReviewLaterStore {

    private static final String PREF_NAME = "review_later_v1";
    private static final String KEY_FAILED_QUIZZES = "failed_quizzes";
    private static final String KEY_SUMMARY_REVIEW = "summary_review";

    private static ReviewLaterStore instance;
    private final Gson gson = new Gson();

    private ReviewLaterStore() {
    }

    public static synchronized ReviewLaterStore getInstance() {
        if (instance == null) {
            instance = new ReviewLaterStore();
        }
        return instance;
    }

    private SharedPreferences prefs(Context context) {
        String userKey = TokenManager.getInstance(context).getUserId();
        if (TextUtils.isEmpty(userKey)) {
            userKey = TokenManager.getInstance(context).getUsername();
        }
        if (TextUtils.isEmpty(userKey)) {
            userKey = "default";
        }
        return context.getApplicationContext()
                .getSharedPreferences(PREF_NAME + "_" + userKey, Context.MODE_PRIVATE);
    }

    /** Score strictly greater than half of questions clears tracking; at or below half keeps or adds. */
    public void recordQuizAttempt(Context context, StudySession session, int score, int total) {
        if (session == null || TextUtils.isEmpty(session.getSessionId()) || total <= 0) {
            return;
        }
        String id = session.getSessionId();
        SharedPreferences p = prefs(context);
        List<FailedQuizEntry> list = readFailed(p);

        if (score * 100 > total * 50) {
            removeBySessionId(list, id);
        } else {
            removeBySessionId(list, id);
            String subject = session.getSubject();
            list.add(0, new FailedQuizEntry(
                    id,
                    subject != null ? subject : "",
                    score,
                    total,
                    System.currentTimeMillis()));
        }
        writeFailed(p, list);
    }

    public void addSummaryForReview(Context context, String sessionId, String subject) {
        if (TextUtils.isEmpty(sessionId)) return;
        SharedPreferences p = prefs(context);
        List<SummaryReviewEntry> list = readSummaries(p);
        removeSummaryById(list, sessionId);
        list.add(0, new SummaryReviewEntry(sessionId, subject != null ? subject : "", System.currentTimeMillis()));
        writeSummaries(p, list);
    }

    public void removeSummaryForReview(Context context, String sessionId) {
        if (TextUtils.isEmpty(sessionId)) return;
        SharedPreferences p = prefs(context);
        List<SummaryReviewEntry> list = readSummaries(p);
        removeSummaryById(list, sessionId);
        writeSummaries(p, list);
    }

    public boolean isSummaryMarkedForReview(Context context, String sessionId) {
        if (TextUtils.isEmpty(sessionId)) return false;
        for (SummaryReviewEntry e : readSummaries(prefs(context))) {
            if (sessionId.equals(e.sessionId)) return true;
        }
        return false;
    }

    public List<FailedQuizEntry> getFailedQuizzes(Context context) {
        return new ArrayList<>(readFailed(prefs(context)));
    }

    public List<SummaryReviewEntry> getSummariesForReview(Context context) {
        return new ArrayList<>(readSummaries(prefs(context)));
    }

    private static void removeBySessionId(List<FailedQuizEntry> list, String sessionId) {
        Iterator<FailedQuizEntry> it = list.iterator();
        while (it.hasNext()) {
            if (sessionId.equals(it.next().sessionId)) {
                it.remove();
            }
        }
    }

    private static void removeSummaryById(List<SummaryReviewEntry> list, String sessionId) {
        Iterator<SummaryReviewEntry> it = list.iterator();
        while (it.hasNext()) {
            if (sessionId.equals(it.next().sessionId)) {
                it.remove();
            }
        }
    }

    private List<FailedQuizEntry> readFailed(SharedPreferences p) {
        String json = p.getString(KEY_FAILED_QUIZZES, "[]");
        Type type = new TypeToken<List<FailedQuizEntry>>() {
        }.getType();
        List<FailedQuizEntry> out = gson.fromJson(json, type);
        return out != null ? out : new ArrayList<>();
    }

    private void writeFailed(SharedPreferences p, List<FailedQuizEntry> list) {
        p.edit().putString(KEY_FAILED_QUIZZES, gson.toJson(list)).apply();
    }

    private List<SummaryReviewEntry> readSummaries(SharedPreferences p) {
        String json = p.getString(KEY_SUMMARY_REVIEW, "[]");
        Type type = new TypeToken<List<SummaryReviewEntry>>() {
        }.getType();
        List<SummaryReviewEntry> out = gson.fromJson(json, type);
        return out != null ? out : new ArrayList<>();
    }

    private void writeSummaries(SharedPreferences p, List<SummaryReviewEntry> list) {
        p.edit().putString(KEY_SUMMARY_REVIEW, gson.toJson(list)).apply();
    }
}
