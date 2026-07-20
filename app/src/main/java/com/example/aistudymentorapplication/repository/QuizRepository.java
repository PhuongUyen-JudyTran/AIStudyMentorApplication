package com.example.aistudymentorapplication.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.aistudymentorapplication.database.DatabaseHelper;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizRepository {
    private GeminiApiService apiService;
    private ExecutorService executorService;
    private Handler mainHandler;
    private Gson gson;
    private FirebaseFirestore db;

    public interface OnQuestionsLoadedListener {
        void onSuccess(List<Question> questions);
        void onError(String errorMessage);
    }

    public interface OnResultSavedListener {
        void onSaved(long resultId);
    }

    public QuizRepository(Application application) {
        apiService = GeminiApiClient.getApiService();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        gson = new Gson();
        db = FirebaseFirestore.getInstance();
    }

    public void generateQuiz(String apiKey, String subject, String level, int questionCount, OnQuestionsLoadedListener listener) {
        executorService.execute(() -> {
            // 1. Get recent user questions for personalization
            //StringBuilder contextBuilder = new StringBuilder();
//            if (!recentQuestions.isEmpty()) {
//                contextBuilder.append("\nDưới đây là một số câu hỏi học sinh đã từng hỏi gần đây:\n");
//                for (String q : recentQuestions) {
//                    contextBuilder.append("- ").append(q).append("\n");
//                }
//                contextBuilder.append("Nếu có câu nào liên quan đến môn ").append(subject)
//                        .append(", hãy ưu tiên tạo câu hỏi ôn tập xoay quanh các chủ đề đó.\n");
//            }

            // 2. Prepare System Prompt
            String lang = "English".equalsIgnoreCase(subject) ? "English" : "Vietnamese";
            String systemPrompt = "Bạn là AI Tutor - Gia sư học tập thông minh. Nhiệm vụ của bạn là tạo " + questionCount + 
                    " câu hỏi trắc nghiệm (4 đáp án) cho " + level + " về môn " + subject + 
                    " bằng ngôn ngữ " + lang + "."
                    + //contextBuilder.toString() +
                    "\nTrả về kết quả dưới dạng JSON array của các object { \"question\": string, \"options\": [string, string, string, string], \"correctIndex\": integer (0-3) }.";

            String userPrompt = "Tạo bộ đề thi trắc nghiệm môn " + subject + " cho " + level + ".";

            GeminiRequest request = new GeminiRequest(userPrompt, systemPrompt, true);

            // 3. API Call
            apiService.generateContent(apiKey, request).enqueue(new Callback<GeminiResponse>() {
                @Override
                public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().getText();
                        try {
                            Type listType = new TypeToken<ArrayList<QuizQuestion>>(){}.getType();
                            List<QuizQuestion> rawQuestions = gson.fromJson(jsonResponse, listType);
                            
                            List<Question> finalQuestions = new ArrayList<>();
                            for (QuizQuestion rq : rawQuestions) {
                                if (rq.options != null && rq.options.size() == 4 && rq.correctIndex >= 0 && rq.correctIndex <= 3) {
                                    finalQuestions.add(new Question(rq.question, rq.options, rq.correctIndex));
                                }
                            }

                            if (finalQuestions.size() >= 3) {
                                mainHandler.post(() -> listener.onSuccess(finalQuestions));
                            } else {
                                mainHandler.post(() -> listener.onError("Gemini returned insufficient or invalid questions. Please try again."));
                            }
                        } catch (Exception e) {
                            mainHandler.post(() -> listener.onError("Failed to parse AI response."));
                        }
                    } else {
                        String errorMsg = "API Error: " + response.code();
                        if (response.code() == 400 || response.code() == 403) {
                            errorMsg = "Invalid API Key or Bad Request.";
                        }
                        String finalErrorMsg = errorMsg;
                        mainHandler.post(() -> listener.onError(finalErrorMsg));
                    }
                }

                @Override
                public void onFailure(Call<GeminiResponse> call, Throwable t) {
                    String msg = "Network Failure: " + t.getMessage();
                    if (t instanceof IOException) {
                        msg = "No network connection. Please check again.";
                    }
                    String finalMsg = msg;
                    mainHandler.post(() -> listener.onError(finalMsg));
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

    public void getAllQuizResults(
            ChatRepository.OnDataLoadedListener<List<QuizResult>> listener) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            listener.onLoaded(new ArrayList<>());
            return;
        }

        db.collection("users")
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
    }
}
