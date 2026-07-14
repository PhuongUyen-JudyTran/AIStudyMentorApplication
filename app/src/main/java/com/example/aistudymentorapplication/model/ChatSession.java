package com.example.aistudymentorapplication.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_sessions")
public class ChatSession {
    @PrimaryKey(autoGenerate = true)
    public long sessionId;
    public String title;
    public long createdAt;
    public long updatedAt;

    public ChatSession(String title, long createdAt, long updatedAt) {
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getSessionId() { return sessionId; }
    public String getTitle() { return title; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
}
