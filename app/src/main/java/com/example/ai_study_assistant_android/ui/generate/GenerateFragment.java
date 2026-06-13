package com.example.ai_study_assistant_android.ui.generate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.ai_study_assistant_android.R;
import com.example.ai_study_assistant_android.model.GenerateRequest;
import com.example.ai_study_assistant_android.model.StudySession;
import com.example.ai_study_assistant_android.network.ApiClient;
import com.example.ai_study_assistant_android.ui.result.ResultActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateFragment extends Fragment {

    private ChipGroup chipGroupType;
    private Chip chipSummary, chipQuiz, chipFlashcards;
    private ChipGroup chipGroupSummaryDepth;
    private View panelSummaryDepth, panelQuizCount;
    private SeekBar seekQuizCount;
    private TextView tvQuizCountValue;
    private TextInputLayout tilSubject, tilText;
    private TextInputEditText etSubject, etText;
    private TabLayout tabInput;
    private LinearLayout panelImage;
    private MaterialButton btnCamera, btnGallery, btnGenerate;
    private ImageView ivPreview;
    private TextView tvOcrStatus, tvCharCount, tvError, tvGenerating;
    private LinearProgressIndicator progress;
    private androidx.cardview.widget.CardView cardImagePreview;

    private Uri cameraImageUri;
    private String pendingType = null;

    // Camera launcher
    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) {
                    showImageAndRunOcr(cameraImageUri);
                }
            });

    // Gallery launcher
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    showImageAndRunOcr(uri);
                }
            });

    // Camera permission launcher
    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted)
                    launchCamera();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chipGroupType = view.findViewById(R.id.chip_group_type);
        chipSummary = view.findViewById(R.id.chip_summary);
        chipQuiz = view.findViewById(R.id.chip_quiz);
        chipFlashcards = view.findViewById(R.id.chip_flashcards);
        chipGroupSummaryDepth = view.findViewById(R.id.chip_group_summary_depth);
        panelSummaryDepth = view.findViewById(R.id.panel_summary_depth);
        panelQuizCount = view.findViewById(R.id.panel_quiz_count);
        seekQuizCount = view.findViewById(R.id.seek_quiz_count);
        tvQuizCountValue = view.findViewById(R.id.tv_quiz_count_value);
        tilSubject = view.findViewById(R.id.til_subject);
        tilText = view.findViewById(R.id.til_text);
        etSubject = view.findViewById(R.id.et_subject);
        etText = view.findViewById(R.id.et_text);
        tabInput = view.findViewById(R.id.tab_input);
        panelImage = view.findViewById(R.id.panel_image);
        btnCamera = view.findViewById(R.id.btn_camera);
        btnGallery = view.findViewById(R.id.btn_gallery);
        btnGenerate = view.findViewById(R.id.btn_generate);
        ivPreview = view.findViewById(R.id.iv_preview);
        tvOcrStatus = view.findViewById(R.id.tv_ocr_status);
        tvCharCount = view.findViewById(R.id.tv_char_count);
        tvError = view.findViewById(R.id.tv_error);
        tvGenerating = view.findViewById(R.id.tv_generating);
        progress = view.findViewById(R.id.progress);
        cardImagePreview = view.findViewById(R.id.card_image_preview);

        // Default selection
        chipSummary.setChecked(true);
        chipGroupSummaryDepth.check(R.id.chip_depth_balanced);
        refreshQuizCountLabel();

        seekQuizCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                refreshQuizCountLabel();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        chipGroupType.setOnCheckedChangeListener((group, checkedId) -> updateTypeOptionPanels());

        // Apply any pre-selected type from HomeFragment
        if (pendingType != null) {
            applyType(pendingType);
            pendingType = null;
        }

        // Tab switcher: text vs image
        tabInput.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    tilText.setVisibility(View.VISIBLE);
                    panelImage.setVisibility(View.GONE);
                } else {
                    tilText.setVisibility(View.GONE);
                    panelImage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Char counter
        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tvCharCount.setText(s.length() + " / 20000");
            }
        });

        btnCamera.setOnClickListener(v -> requestCameraAndLaunch());
        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnGenerate.setOnClickListener(v -> attemptGenerate());

        updateTypeOptionPanels();
    }

    /** Called from MainActivity when returning from a result with follow-up actions */
    public void applyPrefillFromResult(String sourceText, String subject, String targetType) {
        if (etText == null) {
            return;
        }
        if (sourceText != null) {
            etText.setText(sourceText);
        }
        if (subject != null) {
            etSubject.setText(subject);
        }
        if (targetType != null) {
            applyType(targetType);
        }
        tabInput.selectTab(tabInput.getTabAt(0));
        tilText.setVisibility(View.VISIBLE);
        panelImage.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        updateTypeOptionPanels();
    }

    private void refreshQuizCountLabel() {
        if (tvQuizCountValue == null || seekQuizCount == null) {
            return;
        }
        int n = seekQuizCount.getProgress() + 3;
        tvQuizCountValue.setText(getString(R.string.quiz_count_value_format, n));
    }

    private void updateTypeOptionPanels() {
        if (panelSummaryDepth == null || panelQuizCount == null) {
            return;
        }
        String type = getSelectedType();
        if ("SUMMARY".equals(type)) {
            panelSummaryDepth.setVisibility(View.VISIBLE);
            panelQuizCount.setVisibility(View.GONE);
        } else if ("QUIZ".equals(type)) {
            panelSummaryDepth.setVisibility(View.GONE);
            panelQuizCount.setVisibility(View.VISIBLE);
        } else {
            panelSummaryDepth.setVisibility(View.GONE);
            panelQuizCount.setVisibility(View.GONE);
        }
    }

    private String getSelectedSummaryDepth() {
        int id = chipGroupSummaryDepth.getCheckedChipId();
        if (id == R.id.chip_depth_essentials) {
            return "ESSENTIALS";
        }
        if (id == R.id.chip_depth_thorough) {
            return "THOROUGH";
        }
        return "BALANCED";
    }

    /** Called from MainActivity when a Home card is tapped */
    public void setPreselectedType(String type) {
        if (chipSummary == null) {
            pendingType = type; // view not ready yet
        } else {
            applyType(type);
            updateTypeOptionPanels();
        }
    }

    private void applyType(String type) {
        if (type == null)
            return;
        switch (type) {
            case "QUIZ":
                chipQuiz.setChecked(true);
                break;
            case "FLASHCARDS":
                chipFlashcards.setChecked(true);
                break;
            default:
                chipSummary.setChecked(true);
                break;
        }
    }

    private String getSelectedType() {
        int id = chipGroupType.getCheckedChipId();
        if (id == R.id.chip_quiz)
            return "QUIZ";
        if (id == R.id.chip_flashcards)
            return "FLASHCARDS";
        return "SUMMARY";
    }

    private void attemptGenerate() {
        tvError.setVisibility(View.GONE);

        String text = etText.getText() != null ? etText.getText().toString().trim() : "";
        if (text.length() < 20) {
            showError(getString(R.string.error_text_too_short));
            return;
        }
        if (text.length() > 20000) {
            showError(getString(R.string.error_text_too_long));
            return;
        }

        String type = getSelectedType();
        String subject = etSubject.getText() != null ? etSubject.getText().toString().trim() : null;
        if (subject != null && subject.isEmpty())
            subject = null;

        Integer quizCount = "QUIZ".equals(type) ? seekQuizCount.getProgress() + 3 : null;
        String summaryDepth = "SUMMARY".equals(type) ? getSelectedSummaryDepth() : null;

        setLoading(true);

        ApiClient.getInstance(requireContext()).getApiService()
                .generate(new GenerateRequest(text, type, subject, quizCount, summaryDepth))
                .enqueue(new Callback<StudySession>() {
                    @Override
                    public void onResponse(Call<StudySession> call, Response<StudySession> response) {
                        if (!isAdded())
                            return;
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            Intent intent = new Intent(requireContext(), ResultActivity.class);
                            intent.putExtra(ResultActivity.EXTRA_SESSION, response.body());
                            startActivity(intent);
                            // Clear the form after success
                            etText.setText("");
                            etSubject.setText("");
                        } else {
                            int code = response.code();
                            if (code == 429) {
                                showError("Rate limit reached. Please wait a moment.");
                            } else {
                                showError(getString(R.string.error_server));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StudySession> call, Throwable t) {
                        if (!isAdded())
                            return;
                        setLoading(false);
                        showError(getString(R.string.error_network));
                    }
                });
    }

    // ── OCR ──────────────────────────────────────────────────────────────────

    private void requestCameraAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            File photoFile = File.createTempFile("photo_", ".jpg",
                    requireContext().getCacheDir());
            cameraImageUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            showError("Could not create image file.");
        }
    }

    private void showImageAndRunOcr(Uri uri) {
        // Show preview
        cardImagePreview.setVisibility(View.VISIBLE);
        ivPreview.setImageURI(uri);
        tvOcrStatus.setVisibility(View.VISIBLE);
        tvOcrStatus.setText(getString(R.string.ocr_scanning));

        // Switch to text tab so user can see result
        tabInput.selectTab(tabInput.getTabAt(0));
        tilText.setVisibility(View.VISIBLE);
        panelImage.setVisibility(View.GONE);

        try {
            InputImage image = InputImage.fromFilePath(requireContext(), uri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(image)
                    .addOnSuccessListener(result -> {
                        if (!isAdded())
                            return;
                        String recognized = result.getText();
                        if (recognized.isEmpty()) {
                            showError(getString(R.string.error_ocr_failed));
                        } else {
                            etText.setText(recognized);
                        }
                        tvOcrStatus.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded())
                            return;
                        showError(getString(R.string.error_ocr_failed));
                        tvOcrStatus.setVisibility(View.GONE);
                    });
        } catch (IOException e) {
            showError(getString(R.string.error_ocr_failed));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        btnGenerate.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        tvGenerating.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
