package com.example.ai_study_assistant_android.ui.result;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ai_study_assistant_android.R;
import com.example.ai_study_assistant_android.model.QuizQuestion;
import com.example.ai_study_assistant_android.model.StudySession;
import com.example.ai_study_assistant_android.review.ReviewLaterStore;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

public class QuizFragment extends Fragment {

    private static final String ARG_SESSION = "session";

    private List<QuizQuestion> questions;
    private int currentIndex = 0;
    private int score = 0;
    private boolean answered = false;

    private TextView tvCounter, tvScore, tvQuestion, tvExplanation;
    private LinearLayout layoutChoices;
    private LinearProgressIndicator progressQuiz;
    private View cardExplanation;
    private MaterialButton btnNext;

    private StudySession session;

    public static QuizFragment newInstance(StudySession session) {
        QuizFragment f = new QuizFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SESSION, session);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCounter = view.findViewById(R.id.tv_counter);
        tvScore = view.findViewById(R.id.tv_score);
        tvQuestion = view.findViewById(R.id.tv_question);
        tvExplanation = view.findViewById(R.id.tv_explanation);
        layoutChoices = view.findViewById(R.id.layout_choices);
        progressQuiz = view.findViewById(R.id.progress_quiz);
        cardExplanation = view.findViewById(R.id.card_explanation);
        btnNext = view.findViewById(R.id.btn_next);

        session = getArguments() != null
                ? (StudySession) getArguments().getSerializable(ARG_SESSION) : null;

        if (session == null || session.getQuestions() == null || session.getQuestions().isEmpty()) {
            tvQuestion.setText("No questions available.");
            return;
        }

        questions = session.getQuestions();
        progressQuiz.setMax(questions.size());

        btnNext.setOnClickListener(v -> {
            currentIndex++;
            if (currentIndex < questions.size()) {
                loadQuestion();
            } else {
                showScoreDialog();
            }
        });

        loadQuestion();
    }

    private void loadQuestion() {
        answered = false;
        cardExplanation.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        layoutChoices.removeAllViews();

        QuizQuestion q = questions.get(currentIndex);
        int total = questions.size();

        tvCounter.setText(getString(R.string.quiz_question_counter, currentIndex + 1, total));
        tvScore.setText("Score: " + score);
        progressQuiz.setProgress(currentIndex + 1);
        tvQuestion.setText(q.getQuestion());

        if (q.getChoices() == null) return;

        for (int i = 0; i < q.getChoices().size(); i++) {
            final int choiceIdx = i;
            MaterialButton btn = new MaterialButton(requireContext(),
                    null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btn.setText(q.getChoices().get(i));
            btn.setTextColor(requireContext().getColor(R.color.color_on_background));
            btn.setStrokeColor(android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(R.color.color_stroke)));
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(R.color.color_card)));
            btn.setCornerRadius(12);
            btn.setAllCaps(false);
            btn.setTypeface(null, Typeface.NORMAL);
            btn.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 12);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                if (answered) return;
                answered = true;
                handleAnswer(choiceIdx, q);
            });

            layoutChoices.addView(btn);
        }
    }

    private void handleAnswer(int selectedIdx, QuizQuestion q) {
        int correct = q.getCorrectIndex();
        boolean isCorrect = selectedIdx == correct;
        if (isCorrect) score++;

        // Color the buttons
        for (int i = 0; i < layoutChoices.getChildCount(); i++) {
            MaterialButton btn = (MaterialButton) layoutChoices.getChildAt(i);
            btn.setEnabled(false);
            if (i == correct) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        requireContext().getColor(R.color.color_correct_bg)));
                btn.setStrokeColor(android.content.res.ColorStateList.valueOf(
                        requireContext().getColor(R.color.color_correct)));
                btn.setTextColor(requireContext().getColor(R.color.color_correct));
            } else if (i == selectedIdx) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        requireContext().getColor(R.color.color_wrong_bg)));
                btn.setStrokeColor(android.content.res.ColorStateList.valueOf(
                        requireContext().getColor(R.color.color_wrong)));
                btn.setTextColor(requireContext().getColor(R.color.color_wrong));
            }
        }

        tvScore.setText("Score: " + score);

        // Show explanation
        if (q.getExplanation() != null && !q.getExplanation().isEmpty()) {
            tvExplanation.setText(q.getExplanation());
            cardExplanation.setVisibility(View.VISIBLE);
        }

        // Show next/finish
        boolean isLast = currentIndex == questions.size() - 1;
        btnNext.setText(isLast ? getString(R.string.quiz_btn_finish) : getString(R.string.quiz_btn_next));
        btnNext.setVisibility(View.VISIBLE);
    }

    private void showScoreDialog() {
        if (!isAdded()) return;
        int total = questions.size();
        ReviewLaterStore.getInstance().recordQuizAttempt(requireContext(), session, score, total);

        String message = getString(R.string.quiz_score_message, score, total);
        if (total > 0 && score * 100 <= total * 50) {
            message += "\n\n" + getString(R.string.quiz_tracked_review_extra);
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.quiz_score_title))
                .setMessage(message)
                .setPositiveButton("Done", (d, w) -> requireActivity().finish())
                .setCancelable(false)
                .show();
    }
}
