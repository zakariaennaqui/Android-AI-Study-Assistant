package com.example.ai_study_assistant_android.ui.review;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_study_assistant_android.R;
import com.example.ai_study_assistant_android.model.StudySession;
import com.example.ai_study_assistant_android.network.ApiClient;
import com.example.ai_study_assistant_android.review.FailedQuizEntry;
import com.example.ai_study_assistant_android.review.ReviewLaterStore;
import com.example.ai_study_assistant_android.review.SummaryReviewEntry;
import com.example.ai_study_assistant_android.ui.result.ResultActivity;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewFragment extends Fragment {

    private RecyclerView rvReview;
    private LinearProgressIndicator progress;
    private ReviewListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvReview = view.findViewById(R.id.rv_review);
        progress = view.findViewById(R.id.progress);

        adapter = new ReviewListAdapter(new ReviewListAdapter.Listener() {
            @Override
            public void onOpenQuiz(FailedQuizEntry entry) {
                openSessionById(entry.sessionId);
            }

            @Override
            public void onOpenSummary(SummaryReviewEntry entry) {
                openSessionById(entry.sessionId);
            }

            @Override
            public void onRemoveSummary(SummaryReviewEntry entry) {
                ReviewLaterStore.getInstance().removeSummaryForReview(requireContext(), entry.sessionId);
                refreshList();
            }
        });
        rvReview.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReview.setAdapter(adapter);
        refreshList();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        if (adapter == null) return;
        ReviewLaterStore store = ReviewLaterStore.getInstance();
        adapter.setData(store.getFailedQuizzes(requireContext()), store.getSummariesForReview(requireContext()));
    }

    private void openSessionById(String sessionId) {
        progress.setVisibility(View.VISIBLE);
        ApiClient.getInstance(requireContext()).getApiService()
                .getSession(sessionId)
                .enqueue(new Callback<StudySession>() {
                    @Override
                    public void onResponse(Call<StudySession> call, Response<StudySession> response) {
                        if (!isAdded()) return;
                        progress.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            Intent intent = new Intent(requireContext(), ResultActivity.class);
                            intent.putExtra(ResultActivity.EXTRA_SESSION, response.body());
                            startActivity(intent);
                        } else {
                            Snackbar.make(requireView(), R.string.error_server, Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<StudySession> call, Throwable t) {
                        if (!isAdded()) return;
                        progress.setVisibility(View.GONE);
                        Snackbar.make(requireView(), R.string.error_network, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }
}
