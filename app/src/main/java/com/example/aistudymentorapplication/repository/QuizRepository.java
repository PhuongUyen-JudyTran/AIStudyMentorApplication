package com.example.aistudymentorapplication.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.aistudymentorapplication.database.DatabaseHelper;
import com.example.aistudymentorapplication.model.ChatMessage;
import com.example.aistudymentorapplication.model.Question;
import com.example.aistudymentorapplication.model.QuizResult;
import com.example.aistudymentorapplication.network.GeminiApiClient;
import com.example.aistudymentorapplication.network.GeminiApiService;
import com.example.aistudymentorapplication.network.GeminiRequest;
import com.example.aistudymentorapplication.network.GeminiResponse;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.aistudymentorapplication.database.DatabaseHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizRepository {
    private GeminiApiService apiService;
    private ExecutorService executorService;
    private Handler mainHandler;
    private Gson gson;
    private FirebaseFirestore db;

    private DatabaseHelper dbHelper;

    public interface OnQuestionsLoadedListener {
        void onSuccess(List<Question> questions);
        void onError(String errorMessage);
    }

    public interface OnResultSavedListener {
        void onSaved(long resultId);
    }

    public interface OnDeletionListener {
        void onSuccess();
        void onError(String message);
    }

    public QuizRepository(Application application) {
        apiService = GeminiApiClient.getApiService();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        gson = new Gson();
        db = FirebaseFirestore.getInstance();
        dbHelper = new DatabaseHelper(application);
    }

    public void generateQuiz(String apiKey,
                             String subject,
                             String level,
                             int questionCount,
                             OnQuestionsLoadedListener listener)  {

        executorService.execute(() -> {

            //=============================
            // 1. Lấy lịch sử chat từ SQLite
            //=============================

            List<String> history =
                    dbHelper.getRecentUserQuestions(10);

            Log.d("QUIZ", "History size = " + history.size());

            if (history.size() > 10) {
                history = history.subList(history.size() - 10, history.size());
            }

            StringBuilder contextBuilder = new StringBuilder();

            for (String q : history) {
                if (q.length() < 10) {
                    continue;
                }
                contextBuilder.append("- ")
                        .append(q)
                        .append("\n");
            }

            if (!history.isEmpty()) {

                contextBuilder.append("Recent study history of the student:\n");

                for (String question : history) {
                    contextBuilder.append("- ")
                            .append(question)
                            .append("\n");
                }

                contextBuilder.append("\n")
                        .append("Requirements:\n")
                        .append("- Use the above history only if it relates to the requested subject and level.\n")
                        .append("- If history is relevant, prioritize 60-70% of questions based on those topics.\n")
                        .append("- The remaining 30-40% should cover other important curriculum knowledge.\n")
                        .append("- If history is irrelevant, ignore it and create a standard quiz.\n")
                        .append("- Do not recreate identical questions from the student's history.\n");
            }

            String lang =
                    "English".equalsIgnoreCase(subject)
                            ? "English"
                            : "Vietnamese";

            String systemPrompt =
                    "You are AI Tutor - An intelligent study mentor.\n\n"
                            + contextBuilder.toString()

                            + "\nMISSION:\n"
                            + "Create "
                            + questionCount
                            + " multiple-choice questions (4 options) for the subject "
                            + subject
                            + " for students at "
                            + level
                            + " level in "
                            + lang
                            + ".\n\n"

                            + "MANDATORY REQUIREMENTS:\n"

                            + "- Only use knowledge within the official curriculum for "
                            + level
                            + ".\n"

                            + "- Absolutely DO NOT use knowledge from higher grades.\n"

                            + "- If the study history is relevant to the subject and level, at least 70% of questions must be based on those history topics.\n"

                            + "- If the study history belongs to a higher grade or is irrelevant to the subject, ignore it.\n"

                            + "- Do not recreate exactly the same questions found in history.\n"

                            + "- Each question must have exactly 4 options.\n"

                            + "- Only 1 correct answer.\n"

                            + "- Provide a concise explanation.\n"

                            + "- Difficulty level appropriate for "
                            + level
                            + " students.\n"

                            + "- Return only valid JSON, no markdown or extra text.\n\n"

                            + "JSON Format:\n"
                            + "[\n"
                            + "  {\n"
                            + "    \"question\": \"...\",\n"
                            + "    \"options\": [\"A\",\"B\",\"C\",\"D\"],\n"
                            + "    \"correctIndex\": 0,\n"
                            + "    \"explanation\": \"...\"\n"
                            + "  }\n"
                            + "]";

            String userPrompt =
                    "Tạo bộ đề trắc nghiệm môn "
                            + subject
                            + " cho "
                            + level + ".";

            GeminiRequest request =
                    new GeminiRequest(
                            userPrompt,
                            systemPrompt,
                            true);

            apiService.generateContent(apiKey, request)
                    .enqueue(new Callback<GeminiResponse>() {

                        @Override
                        public void onResponse(Call<GeminiResponse> call,
                                               Response<GeminiResponse> response) {

                            if (response.isSuccessful()
                                    && response.body() != null) {

                                String jsonResponse =
                                        response.body().getText();

                                try {

                                    Type listType =
                                            new TypeToken<ArrayList<QuizQuestion>>() {
                                            }.getType();

                                    List<QuizQuestion> rawQuestions =
                                            gson.fromJson(
                                                    jsonResponse,
                                                    listType);

                                    List<Question> finalQuestions =
                                            new ArrayList<>();

                                    for (QuizQuestion rq : rawQuestions) {

                                        if (rq.options != null
                                                && rq.options.size() == 4
                                                && rq.correctIndex >= 0
                                                && rq.correctIndex <= 3) {

                                            finalQuestions.add(

                                                    new Question(

                                                            rq.question,

                                                            rq.options,

                                                            rq.correctIndex,

                                                            rq.explanation));
                                        }
                                    }

                                    if (finalQuestions.size() >= 3) {

                                        mainHandler.post(() ->
                                                listener.onSuccess(
                                                        finalQuestions));

                                    } else {

                                        mainHandler.post(() ->
                                                listener.onError(
                                                        "Gemini returned insufficient or invalid questions."));
                                    }

                                } catch (Exception e) {

                                    mainHandler.post(() ->
                                            listener.onError(
                                                    "Failed to parse AI response."));
                                }

                            } else {

                                String error =
                                        "API Error: "
                                                + response.code();

                                try {

                                    if (response.errorBody() != null) {

                                        error += "\n"
                                                + response.errorBody().string();
                                    }

                                } catch (Exception ignored) {
                                }

                                String finalError = error;

                                mainHandler.post(() ->
                                        listener.onError(finalError));
                            }
                        }

                        @Override
                        public void onFailure(Call<GeminiResponse> call,
                                              Throwable t) {

                            String msg =
                                    "Network Failure: "
                                            + t.getMessage();

                            if (t instanceof IOException) {

                                msg =
                                        "No network connection. Please check again.";
                            }

                            String finalMsg = msg;

                            mainHandler.post(() ->
                                    listener.onError(finalMsg));
                        }
                    });
        });
    }
    public void saveQuizResult(QuizResult result, OnResultSavedListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        db.collection("Users")
                .document(user.getUid())
                .collection("history")
                .add(result)
                .addOnSuccessListener(documentReference -> {
                    if (listener != null) {
                        listener.onSaved(0);
                    }
                });
    }

    public void deleteQuizResult(String documentId, OnDeletionListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || documentId == null) return;

        db.collection("Users")
                .document(user.getUid())
                .collection("history")
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    public void getAllQuizResults(
            ChatRepository.OnDataLoadedListener<List<QuizResult>> listener) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            listener.onLoaded(new ArrayList<>());
            return;
        }

        db.collection("Users")
                .document(user.getUid())
                .collection("history")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<QuizResult> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        QuizResult result = doc.toObject(QuizResult.class);
                        list.add(result);
                    }
                    listener.onLoaded(list);

                });
    }

    // Helper class for JSON parsing
    private static class QuizQuestion {
        String question;
        List<String> options;
        int correctIndex;
        String explanation;
    }
}
