package com.example.aistudymentorapplication.model;

/**
 * ChatSession model representing a single conversation thread.
 */
public class ChatSession {
    private long sessionId;
    private String title;
    private long createdAt;
    private long updatedAt;

    // Constructor for creating a new session
    public ChatSession(String title, long createdAt, long updatedAt) {
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor for existing session from database
    public ChatSession(long sessionId, String title, long createdAt, long updatedAt) {
        this.sessionId = sessionId;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getSessionId() { return sessionId; }
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
