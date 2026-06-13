package com.example.ai_study_assistant_android.ui.history;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ai_study_assistant_android.R;
import com.example.ai_study_assistant_android.model.HistoryItem;
import com.example.ai_study_assistant_android.model.StudySession;
import com.example.ai_study_assistant_android.network.ApiClient;
import com.example.ai_study_assistant_android.ui.result.ResultActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private SwipeRefreshLayout swipeRefresh;
    private LinearProgressIndicator progress;
    private View layoutEmpty;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHistory = view.findViewById(R.id.rv_history);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progress = view.findViewById(R.id.progress);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        swipeRefresh.setColorSchemeColors(requireContext().getColor(R.color.color_primary));
        swipeRefresh.setProgressBackgroundColorSchemeColor(
                requireContext().getColor(R.color.color_surface));

        adapter = new HistoryAdapter(new ArrayList<>(), this::openSession);
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setAdapter(adapter);

        // Swipe-to-delete
        ItemTouchHelper.SimpleCallback swipeCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView rv,
                                         @NonNull RecyclerView.ViewHolder vh,
                                         @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                        int pos = vh.getAdapterPosition();
                        HistoryItem item = adapter.getItem(pos);
                        confirmDelete(item, pos);
                    }
                };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvHistory);

        swipeRefresh.setOnRefreshListener(this::loadHistory);
        loadHistory();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        progress.setVisibility(View.VISIBLE);
        ApiClient.getInstance(requireContext()).getApiService()
                .listHistory()
                .enqueue(new Callback<List<HistoryItem>>() {
                    @Override
                    public void onResponse(Call<List<HistoryItem>> call,
                                           Response<List<HistoryItem>> response) {
                        if (!isAdded()) return;
                        progress.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<HistoryItem> items = response.body();
                            adapter.setItems(items);
                            layoutEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                            rvHistory.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<HistoryItem>> call, Throwable t) {
                        if (!isAdded()) return;
                        progress.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Snackbar.make(requireView(),
                                R.string.error_network, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDelete(HistoryItem item, int pos) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.history_delete_confirm)
                .setNegativeButton(R.string.btn_cancel, (d, w) -> adapter.notifyItemChanged(pos))
                .setPositiveButton(R.string.btn_delete, (d, w) -> deleteSession(item, pos))
                .setOnCancelListener(d -> adapter.notifyItemChanged(pos))
                .show();
    }

    private void deleteSession(HistoryItem item, int pos) {
        ApiClient.getInstance(requireContext()).getApiService()
                .deleteSession(item.getSessionId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() || response.code() == 204) {
                            adapter.removeItem(pos);
                            if (adapter.getItemCount() == 0) {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                rvHistory.setVisibility(View.GONE);
                            }
                        } else {
                            adapter.notifyItemChanged(pos);
                            Snackbar.make(requireView(),
                                    R.string.error_server, Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (!isAdded()) return;
                        adapter.notifyItemChanged(pos);
                        Snackbar.make(requireView(),
                                R.string.error_network, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void openSession(HistoryItem item) {
        progress.setVisibility(View.VISIBLE);
        ApiClient.getInstance(requireContext()).getApiService()
                .getSession(item.getSessionId())
                .enqueue(new Callback<StudySession>() {
                    @Override
                    public void onResponse(Call<StudySession> call, Response<StudySession> response) {
                        if (!isAdded()) return;
                        progress.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            Intent intent = new Intent(requireContext(), ResultActivity.class);
                            intent.putExtra(ResultActivity.EXTRA_SESSION, response.body());
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onFailure(Call<StudySession> call, Throwable t) {
                        if (!isAdded()) return;
                        progress.setVisibility(View.GONE);
                        Snackbar.make(requireView(),
                                R.string.error_network, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }
}
