package com.example.ai_study_assistant_android.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_study_assistant_android.R;
import com.example.ai_study_assistant_android.model.HistoryItem;
import com.example.ai_study_assistant_android.network.ApiClient;
import com.example.ai_study_assistant_android.network.TokenManager;
import com.example.ai_study_assistant_android.ui.history.HistoryAdapter;
import com.example.ai_study_assistant_android.ui.main.MainActivity;
import com.example.ai_study_assistant_android.ui.result.ResultActivity;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.ai_study_assistant_android.model.StudySession;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvNoRecent, tvSeeAll;
    private RecyclerView rvRecent;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvNoRecent = view.findViewById(R.id.tv_no_recent);
        tvSeeAll = view.findViewById(R.id.tv_see_all);
        rvRecent = view.findViewById(R.id.rv_recent);

        String username = TokenManager.getInstance(requireContext()).getUsername();
        tvGreeting.setText(getString(R.string.home_greeting, username.isEmpty() ? "Student" : username));

        // Feature cards
        view.findViewById(R.id.card_summary).setOnClickListener(v -> openGenerate("SUMMARY"));
        view.findViewById(R.id.card_quiz).setOnClickListener(v -> openGenerate("QUIZ"));
        view.findViewById(R.id.card_flashcards).setOnClickListener(v -> openGenerate("FLASHCARDS"));

        // See all → switch to History tab
        tvSeeAll.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openGenerateWithType(null);
                requireActivity().findViewById(R.id.bottom_nav)
                        .findViewById(R.id.nav_history).performClick();
            }
        });

        // Setup RecyclerView
        adapter = new HistoryAdapter(new ArrayList<>(), item -> openSession(item.getSessionId()));
        rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecent.setAdapter(adapter);
        rvRecent.setNestedScrollingEnabled(false);

        loadRecent();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecent();
    }

    private void loadRecent() {
        ApiClient.getInstance(requireContext()).getApiService()
                .listHistory()
                .enqueue(new Callback<List<HistoryItem>>() {
                    @Override
                    public void onResponse(Call<List<HistoryItem>> call, Response<List<HistoryItem>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            List<HistoryItem> all = response.body();
                            // Show only latest 3
                            List<HistoryItem> recent = all.subList(0, Math.min(3, all.size()));
                            adapter.setItems(recent);
                            tvNoRecent.setVisibility(recent.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<HistoryItem>> call, Throwable t) {
                        // Silent fail — home screen, not critical
                    }
                });
    }

    private void openGenerate(String type) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openGenerateWithType(type);
        }
    }

    private void openSession(String sessionId) {
        ApiClient.getInstance(requireContext()).getApiService()
                .getSession(sessionId)
                .enqueue(new Callback<StudySession>() {
                    @Override
                    public void onResponse(Call<StudySession> call, Response<StudySession> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            Intent intent = new Intent(requireContext(), ResultActivity.class);
                            intent.putExtra(ResultActivity.EXTRA_SESSION, response.body());
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onFailure(Call<StudySession> call, Throwable t) { }
                });
    }
}
