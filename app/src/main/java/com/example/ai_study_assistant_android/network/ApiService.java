package com.example.ai_study_assistant_android.network;

import com.example.ai_study_assistant_android.model.AuthRequest;
import com.example.ai_study_assistant_android.model.AuthResponse;
import com.example.ai_study_assistant_android.model.GenerateRequest;
import com.example.ai_study_assistant_android.model.HistoryItem;
import com.example.ai_study_assistant_android.model.MeResponse;
import com.example.ai_study_assistant_android.model.RegisterRequest;
import com.example.ai_study_assistant_android.model.StudySession;
import com.example.ai_study_assistant_android.model.UpdateProfileRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @GET("api/auth/me")
    Call<MeResponse> me();

    @PATCH("api/auth/profile")
    Call<AuthResponse> updateProfile(@Body UpdateProfileRequest body);

    // ── Study Generation ──────────────────────────────────────────────────────

    @POST("api/study/generate")
    Call<StudySession> generate(@Body GenerateRequest request);

    // ── History ───────────────────────────────────────────────────────────────

    @GET("api/history")
    Call<List<HistoryItem>> listHistory();

    @GET("api/history/{sessionId}")
    Call<StudySession> getSession(@Path("sessionId") String sessionId);

    @DELETE("api/history/{sessionId}")
    Call<Void> deleteSession(@Path("sessionId") String sessionId);
}
