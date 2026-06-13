package com.example.ai_study_assistant_android.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Flashcard implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("front")
    private String front;

    @SerializedName("back")
    private String back;

    public String getFront() { return front; }
    public String getBack() { return back; }
}