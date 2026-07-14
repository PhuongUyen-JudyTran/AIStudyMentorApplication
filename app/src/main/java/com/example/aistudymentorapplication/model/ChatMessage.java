package com.example.aistudymentorapplication.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessage {
    @PrimaryKey(autoGenerate = true)
    public long messageId;
    public long sessionId;
    public String sender; // "user" or "ai"
    public String message;
    public long timestamp;

    public ChatMessage(long sessionId, String sender, String message, long timestamp) {
        this.sessionId = sessionId;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public long getMessageId() { return messageId; }
    public long getSessionId() { return sessionId; }
    public String getSender() { return sender; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
