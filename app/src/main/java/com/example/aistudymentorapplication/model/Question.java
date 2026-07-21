package com.example.aistudymentorapplication.model;

import java.util.List;

public class Question {
    private String text;
    private List<String> options;
    private int correctOptionIndex;
    private String explanation;

    public Question(String text, List<String> options, int correctOptionIndex, String explanation) {
        this.text = text;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.explanation = (explanation != null) ? explanation : "";
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }
    public String getExplanation() {
        return explanation;
    }
}
