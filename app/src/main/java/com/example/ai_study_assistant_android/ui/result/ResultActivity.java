package com.example.ai_study_assistant_android.ui.result;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ai_study_assistant_android.R;
import com.example.ai_study_assistant_android.model.StudySession;
import com.example.ai_study_assistant_android.ui.main.MainActivity;
import com.google.android.material.button.MaterialButton;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION = "extra_session";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        StudySession session = (StudySession) getIntent()
                .getSerializableExtra(EXTRA_SESSION);

        if (session == null) {
            finish();
            return;
        }

        TextView tvSubject = findViewById(R.id.tv_subject);
        TextView tvTypeLabel = findViewById(R.id.tv_type_label);
        MaterialButton btnBack = findViewById(R.id.btn_back);

        String subject = session.getSubject();
        tvSubject.setText((subject != null && !subject.isEmpty()) ? subject : "Result");

        String type = session.getType() != null ? session.getType() : "SUMMARY";
        tvTypeLabel.setText(type);

        int labelColor;
        switch (type) {
            case "QUIZ":
                labelColor = getColor(R.color.color_badge_quiz);
                break;
            case "FLASHCARDS":
                labelColor = getColor(R.color.color_badge_flashcards);
                break;
            default:
                labelColor = getColor(R.color.color_badge_summary);
                break;
        }
        tvTypeLabel.setTextColor(labelColor);

        btnBack.setOnClickListener(v -> finish());

        setupFollowUpActions(session, type);

        if (savedInstanceState == null) {
            Fragment fragment;
            switch (type) {
                case "QUIZ":
                    fragment = QuizFragment.newInstance(session);
                    break;
                case "FLASHCARDS":
                    fragment = FlashcardsFragment.newInstance(session);
                    break;
                default:
                    fragment = SummaryFragment.newInstance(session);
                    break;
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.result_container, fragment)
                    .commit();
        }
    }

    private void setupFollowUpActions(StudySession session, String currentType) {
        View panel = findViewById(R.id.panel_followup);
        String source = session.getSourceText();
        if (source == null || source.trim().length() < 20) {
            panel.setVisibility(View.GONE);
            return;
        }

        panel.setVisibility(View.VISIBLE);
        MaterialButton btnSummary = findViewById(R.id.btn_follow_summary);
        MaterialButton btnQuiz = findViewById(R.id.btn_follow_quiz);
        MaterialButton btnFlash = findViewById(R.id.btn_follow_flashcards);

        btnSummary.setVisibility("SUMMARY".equals(currentType) ? View.GONE : View.VISIBLE);
        btnQuiz.setVisibility("QUIZ".equals(currentType) ? View.GONE : View.VISIBLE);
        btnFlash.setVisibility("FLASHCARDS".equals(currentType) ? View.GONE : View.VISIBLE);

        btnSummary.setOnClickListener(v -> launchGenerateWith(session, "SUMMARY"));
        btnQuiz.setOnClickListener(v -> launchGenerateWith(session, "QUIZ"));
        btnFlash.setOnClickListener(v -> launchGenerateWith(session, "FLASHCARDS"));
    }

    private void launchGenerateWith(StudySession session, String targetType) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.EXTRA_PREFILL_SOURCE_TEXT, session.getSourceText());
        String subj = session.getSubject();
        if (subj != null && !subj.isEmpty()) {
            intent.putExtra(MainActivity.EXTRA_PREFILL_SUBJECT, subj);
        }
        intent.putExtra(MainActivity.EXTRA_PREFILL_TARGET_TYPE, targetType);
        startActivity(intent);
    }
}
