package com.example.aistudymentorapplication.model;

/**
 * Model representing the result of a completed quiz session.
 */
public class QuizResult {
    private int resultId;
    private String subject;
    private String level;
    private int score;
    private int total;
    private int durationSec;
    private long createdAt;

    public QuizResult(String subject, String level, int score, int total, int durationSec, long createdAt) {
        this.subject = subject;
        this.level = level;
        this.score = score;
        this.total = total;
        this.durationSec = durationSec;
        this.createdAt = createdAt;
    }

    public QuizResult(int resultId, String subject, String level, int score, int total, int durationSec, long createdAt) {
        this.resultId = resultId;
        this.subject = subject;
        this.level = level;
        this.score = score;
        this.total = total;
        this.durationSec = durationSec;
        this.createdAt = createdAt;
    }

    public int getResultId() { return resultId; }
    public String getSubject() { return subject; }
    public String getLevel() { return level; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public int getDurationSec() { return durationSec; }
    public long getCreatedAt() { return createdAt; }
}
