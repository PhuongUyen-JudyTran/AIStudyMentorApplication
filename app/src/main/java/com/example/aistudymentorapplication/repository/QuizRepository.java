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

                contextBuilder.append("Đây là lịch sử học tập gần đây của học sinh:\n");

                for (String question : history) {
                    contextBuilder.append("- ")
                            .append(question)
                            .append("\n");
                }

                contextBuilder.append("\n")
                        .append("Yêu cầu:\n")
                        .append("- Chỉ sử dụng lịch sử trên nếu nó liên quan đến môn học và lớp được yêu cầu.\n")
                        .append("- Nếu lịch sử liên quan, hãy ưu tiên khoảng 60-70% số câu hỏi từ các chủ đề đó.\n")
                        .append("- 30-40% câu hỏi còn lại nên bao quát các kiến thức quan trọng khác.\n")
                        .append("- Nếu lịch sử không liên quan thì bỏ qua và tạo bộ câu hỏi bình thường.\n")
                        .append("- Không tạo lại các câu hỏi giống hệt học sinh đã hỏi.\n");
            }

            String lang =
                    "English".equalsIgnoreCase(subject)
                            ? "English"
                            : "Vietnamese";

            String systemPrompt =
                    "Bạn là AI Tutor - Gia sư học tập thông minh.\n\n"
                            + contextBuilder.toString()

                            + "\nNHIỆM VỤ:\n"
                            + "Tạo "
                            + questionCount
                            + " câu hỏi trắc nghiệm (4 đáp án) môn "
                            + subject
                            + " dành cho học sinh "
                            + level
                            + " bằng ngôn ngữ "
                            + lang
                            + ".\n\n"

                            + "YÊU CẦU BẮT BUỘC:\n"

                            + "- Chỉ sử dụng kiến thức thuộc chương trình chính thức của "
                            + level
                            + ".\n"

                            + "- Tuyệt đối KHÔNG sử dụng kiến thức của lớp cao hơn.\n"

                            + "- Nếu lịch sử học tập liên quan đến môn học và đúng trình độ thì ít nhất 70% số câu hỏi phải dựa trên các chủ đề trong lịch sử.\n"

                            + "- Nếu lịch sử học tập thuộc lớp cao hơn hoặc không liên quan đến môn học thì bỏ qua lịch sử đó.\n"

                            + "- Không tạo lại đúng các câu hỏi đã xuất hiện trong lịch sử.\n"

                            + "- Mỗi câu có đúng 4 đáp án.\n"

                            + "- Chỉ có 1 đáp án đúng.\n"

                            + "- Có lời giải thích ngắn gọn.\n"

                            + "- Độ khó phù hợp với học sinh "
                            + level
                            + ".\n"

                            + "- Chỉ trả về JSON hợp lệ, không thêm markdown hoặc giải thích.\n\n"

                            + "Định dạng JSON:\n"
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
        String explanation; // MỚI
    }
}
