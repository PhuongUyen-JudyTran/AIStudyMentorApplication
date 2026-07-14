package com.example.aistudymentorapplication.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.aistudymentorapplication.database.AppDatabase;
import com.example.aistudymentorapplication.database.ChatMessageDao;
import com.example.aistudymentorapplication.database.ChatSessionDao;
import com.example.aistudymentorapplication.model.ChatMessage;
import com.example.aistudymentorapplication.model.ChatSession;
import com.example.aistudymentorapplication.network.GeminiApiClient;
import com.example.aistudymentorapplication.network.GeminiApiService;
import com.example.aistudymentorapplication.network.GeminiRequest;
import com.example.aistudymentorapplication.network.GeminiResponse;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private ChatSessionDao sessionDao;
    private ChatMessageDao messageDao;
    private GeminiApiService apiService;
    private ExecutorService executorService;

    public ChatRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        sessionDao = db.chatSessionDao();
        messageDao = db.chatMessageDao();
        apiService = GeminiApiClient.getApiService();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Sessions
    public LiveData<List<ChatSession>> getAllSessions() {
        return sessionDao.getAllSessions();
    }

    public void createSession(ChatSession session, OnSessionCreatedListener listener) {
        executorService.execute(() -> {
            long id = sessionDao.insert(session);
            session.sessionId = id;
            if (listener != null) listener.onCreated(id);
        });
    }

    public void updateSession(ChatSession session) {
        executorService.execute(() -> sessionDao.update(session));
    }

    public void deleteSession(ChatSession session) {
        executorService.execute(() -> {
            messageDao.deleteMessagesForSession(session.sessionId);
            sessionDao.delete(session);
        });
    }

    // Messages
    public LiveData<List<ChatMessage>> getMessagesForSession(long sessionId) {
        return messageDao.getMessagesForSession(sessionId);
    }

    public void saveMessage(ChatMessage message) {
        executorService.execute(() -> messageDao.insert(message));
    }

    public void updateSessionTime(long sessionId) {
        executorService.execute(() -> {
            ChatSession session = sessionDao.getSessionById(sessionId);
            if (session != null) {
                session.updatedAt = System.currentTimeMillis();
                sessionDao.update(session);
            }
        });
    }

    // Gemini API
    public void getAiResponse(String apiKey, String prompt, OnResponseListener listener) {
        GeminiRequest request = new GeminiRequest(prompt);
        apiService.generateContent(apiKey, request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body().getText());
                } else {
                    listener.onError("API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                listener.onError("Network Failure: " + t.getMessage());
            }
        });
    }

    public interface OnSessionCreatedListener {
        void onCreated(long sessionId);
    }

    public interface OnResponseListener {
        void onSuccess(String response);
        void onError(String error);
    }
}
