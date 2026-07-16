package com.example.aistudymentorapplication.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.aistudymentorapplication.database.DatabaseHelper;
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

/**
 * ChatRepository mediates between the UI, SQLite Database, and Gemini API.
 */
public class ChatRepository {
    private DatabaseHelper dbHelper;
    private GeminiApiService apiService;
    private ExecutorService executorService;
    private Handler mainHandler;

    public ChatRepository(Application application) {
        dbHelper = new DatabaseHelper(application);
        apiService = GeminiApiClient.getApiService();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // --- Session Operations ---

    public void getAllSessions(OnDataLoadedListener<List<ChatSession>> listener) {
        executorService.execute(() -> {
            List<ChatSession> sessions = dbHelper.getAllSessions();
            mainHandler.post(() -> listener.onLoaded(sessions));
        });
    }

    public void createSession(ChatSession session, OnSessionCreatedListener listener) {
        executorService.execute(() -> {
            long id = dbHelper.insertSession(session);
            session.setSessionId(id);
            mainHandler.post(() -> {
                if (listener != null) listener.onCreated(id);
            });
        });
    }

    public void updateSession(ChatSession session) {
        executorService.execute(() -> dbHelper.updateSession(session));
    }

    public void deleteSession(long sessionId, Runnable onComplete) {
        executorService.execute(() -> {
            dbHelper.deleteSession(sessionId);
            if (onComplete != null) mainHandler.post(onComplete);
        });
    }

    // --- Message Operations ---

    public void getMessagesForSession(long sessionId, OnDataLoadedListener<List<ChatMessage>> listener) {
        executorService.execute(() -> {
            List<ChatMessage> messages = dbHelper.getMessagesBySessionId(sessionId);
            mainHandler.post(() -> listener.onLoaded(messages));
        });
    }

    public void saveMessage(ChatMessage message) {
        executorService.execute(() -> dbHelper.insertMessage(message));
    }

    public void updateSessionTime(long sessionId) {
        executorService.execute(() -> {
            ChatSession session = dbHelper.getSessionById(sessionId);
            if (session != null) {
                session.setUpdatedAt(System.currentTimeMillis());
                dbHelper.updateSession(session);
            }
        });
    }

    // --- Gemini API Operations ---

    public void getAiResponse(String apiKey, String prompt, OnResponseListener listener) {
        GeminiRequest request = new GeminiRequest(prompt);
        apiService.generateContent(apiKey, request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body().getText());
                } else {
                    String errorMsg = "API Error: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) { /* Ignore */ }
                    listener.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                listener.onError("Network Failure: " + t.getMessage());
            }
        });
    }

    public interface OnDataLoadedListener<T> {
        void onLoaded(T data);
    }

    public interface OnSessionCreatedListener {
        void onCreated(long sessionId);
    }

    public interface OnResponseListener {
        void onSuccess(String response);
        void onError(String error);
    }
}
