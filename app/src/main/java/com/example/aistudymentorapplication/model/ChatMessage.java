package com.example.aistudymentorapplication.model;

/**
 * ChatMessage model representing a single message in a session.
 */
public class ChatMessage {
    private long messageId;
    private long sessionId;
    private String sender; // "user" or "ai"
    private String message;
    private long timestamp;

    // Constructor for new message
    public ChatMessage(long sessionId, String sender, String message, long timestamp) {
        this.sessionId = sessionId;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Constructor for message from database
    public ChatMessage(long messageId, long sessionId, String sender, String message, long timestamp) {
        this.messageId = messageId;
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
