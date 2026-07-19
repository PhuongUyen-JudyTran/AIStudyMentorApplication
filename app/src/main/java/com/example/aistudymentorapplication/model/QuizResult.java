package com.example.aistudymentorapplication.model;

public class QuizResult {

    private String subject;
    private String level;
    private int score;
    private int total;
    private int durationSec;
    private long createdAt;

    public QuizResult() {

    }

    public QuizResult(String subject, String level,
                      int score, int total,
                      int durationSec, long createdAt) {
        this.subject = subject;
        this.level = level;
        this.score = score;
        this.total = total;
        this.durationSec = durationSec;
        this.createdAt = createdAt;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}