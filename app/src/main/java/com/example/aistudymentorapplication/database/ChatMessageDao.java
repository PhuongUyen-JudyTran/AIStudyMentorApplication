package com.example.aistudymentorapplication.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.aistudymentorapplication.model.ChatMessage;

import java.util.List;

@Dao
public interface ChatMessageDao {
    @Insert
    void insert(ChatMessage message);

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    LiveData<List<ChatMessage>> getMessagesForSession(long sessionId);

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    void deleteMessagesForSession(long sessionId);
}
