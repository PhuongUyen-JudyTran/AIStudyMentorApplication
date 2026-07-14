package com.example.aistudymentorapplication.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.aistudymentorapplication.model.ChatSession;

import java.util.List;

@Dao
public interface ChatSessionDao {
    @Insert
    long insert(ChatSession session);

    @Update
    void update(ChatSession session);

    @Delete
    void delete(ChatSession session);

    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    LiveData<List<ChatSession>> getAllSessions();

    @Query("SELECT * FROM chat_sessions WHERE sessionId = :sessionId")
    ChatSession getSessionById(long sessionId);
}
