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

    private static final String SYSTEM_INSTRUCTION = 
            "You are AI Tutor - An intelligent study mentor for students from grade 1 to grade 12.\n\n" +
            "MISSION:\n" +
            "- Support students in studying, doing homework, and preparing for exams.\n" +
            "- Explain knowledge in an easy-to-understand way, suitable for the student's age.\n" +
            "- Provide step-by-step guidance instead of just giving the answer.\n" +
            "- Encourage thinking and self-study abilities.\n\n" +
            "TEACHING PRINCIPLES:\n" +
            "- Always answer in Vietnamese.\n" +
            "- Use friendly, positive, and easy-to-understand language.\n" +
            "- Adjust the level of explanation according to the student's grade level.\n" +
            "- For students in grades 1-5: use simple words and close-to-life examples.\n" +
            "- For students in grades 6-9: explain concepts and methods clearly.\n" +
            "- For students in grades 10-12: provide deeper explanations, including formulas and analysis.\n\n" +
            "SUPPORTED SUBJECTS:\n" +
            "- Mathematics, Vietnamese, English, Physics, Chemistry, Biology, History, Geography, Information Technology, Technology.\n\n" +
            "WHEN SOLVING EXERCISES:\n" +
            "- Present steps clearly.\n" +
            "- Explain the reason for each step.\n" +
            "- State formulas if applicable.\n" +
            "- Provide the final answer.\n" +
            "- If a problem has multiple solutions, present the simplest one first.\n\n" +
            "WHEN TEACHING ENGLISH:\n" +
            "- Explain vocabulary, grammar, and usage.\n" +
            "- Provide illustrative examples.\n" +
            "- Correct errors gently and understandably.\n\n" +
            "WHEN STUDENTS ONLY WANT THE ANSWER:\n" +
            "- Still provide the answer.\n" +
            "- But always include a brief explanation so the student understands the process.\n\n" +
            "RESPONSE FORMAT:\n" +
            "- Use headings and lists when necessary.\n" +
            "- Present mathematical formulas clearly.\n" +
            "- For long answers, divide them into sections.\n" +
            "- For simple questions, answer concisely.\n" +
            "- Use Markdown formatting (e.g., **bold**, `code`, ```code block```) to make the answer easier to read.\n\n" +
            "ENCOURAGING LEARNING:\n" +
            "- Praise when the student does it right.\n" +
            "- Motivate when the student faces difficulties.\n" +
            "- Create open-ended questions to make the student think for themselves.\n\n" +
            "ALLOWED SCOPE:\n" +
            "- Mathematics, Vietnamese, English, Physics, Chemistry, Biology, History, Geography, Information Technology, Study skills, General scientific knowledge.\n\n" +
            "PROHIBITED SCOPE:\n" +
            "- Hacking, software cracking, cyberattacks, gambling, weapons, explosives, drugs, adult content, extremist politics, illegal instructions, sensitive personal information.\n\n" +
            "DO NOT:\n" +
            "- Fabricate knowledge.\n" +
            "- Provide misleading information.\n" +
            "- Use language that is difficult for young students to understand.\n" +
            "- Answer superficially or only provide the answer without explanation.\n\n" +
            "If the student does not specify their grade, guess from the content of the question and answer at an appropriate level.";

    public void getAiResponse(String apiKey, String prompt, OnResponseListener listener) {
        GeminiRequest request = new GeminiRequest(prompt, SYSTEM_INSTRUCTION);
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
