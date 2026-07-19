package com.example.aistudymentorapplication;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;

import com.example.aistudymentorapplication.model.Question;
import com.example.aistudymentorapplication.model.QuizResult;
import com.example.aistudymentorapplication.repository.QuizRepository;
import com.example.aistudymentorapplication.util.NetworkUtils;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.res.ColorStateList;
import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    // Setup UI
    private LinearLayout layoutQuizSetup;
    private AutoCompleteTextView atvLevel, atvSubject;
    private MaterialButton btnCreateQuiz;

    // Quiz Content UI
    private ConstraintLayout layoutQuizContent;
    private ProgressBar progressBar;
    private TextView tvProgressText, tvQuestion, tvTimer;
    private RadioGroup rgOptions;
    private RadioButton[] rbOptions = new RadioButton[4];
    private MaterialButton btnNext;
    private ImageButton btnBack;

    // Result UI
    private LinearLayout layoutQuizResult;
    private TextView tvScoreDetail;
    private MaterialButton btnBackToHome;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 600000; // 5 minutes (300 seconds)

    private QuizRepository quizRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        quizRepository = new QuizRepository(getApplication());
        initViews();
        setupDropdowns();
        
        btnCreateQuiz.setOnClickListener(v -> startQuiz());
        btnNext.setOnClickListener(v -> handleNextQuestion());
        btnBack.setOnClickListener(v -> finish());
        btnBackToHome.setOnClickListener(v -> finish());
    }

    private void initViews() {
        // Setup Views
        layoutQuizSetup = findViewById(R.id.layoutQuizSetup);
        atvLevel = findViewById(R.id.atvLevel);
        atvSubject = findViewById(R.id.atvSubject);
        btnCreateQuiz = findViewById(R.id.btnCreateQuiz);

        // Content Views
        layoutQuizContent = findViewById(R.id.layoutQuizContent);
        progressBar = findViewById(R.id.quizProgressBar);
        tvProgressText = findViewById(R.id.tvProgressText);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvTimer = findViewById(R.id.tvTimer);
        rgOptions = findViewById(R.id.rgOptions);
        rbOptions[0] = findViewById(R.id.rbOption1);
        rbOptions[1] = findViewById(R.id.rbOption2);
        rbOptions[2] = findViewById(R.id.rbOption3);
        rbOptions[3] = findViewById(R.id.rbOption4);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        // Result Views
        layoutQuizResult = findViewById(R.id.layoutQuizResult);
        tvScoreDetail = findViewById(R.id.tvScoreDetail);
        btnBackToHome = findViewById(R.id.btnBackToHome);
    }

    private void setupDropdowns() {
        String[] levels = {getString(R.string.level_1), getString(R.string.level_2), getString(R.string.level_3)};
        String[] subjects = {getString(R.string.subject_math), getString(R.string.subject_science), getString(R.string.subject_history), getString(R.string.subject_english)};

        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, levels);
        atvLevel.setAdapter(levelAdapter);

        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects);
        atvSubject.setAdapter(subjectAdapter);
    }

    private void startQuiz() {
        String level = atvLevel.getText().toString();
        String subject = atvSubject.getText().toString();

        if (level.isEmpty() || subject.isEmpty()) {
            Toast.makeText(this, "Please select level and subject", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Loading state
        btnCreateQuiz.setEnabled(false);
        btnCreateQuiz.setText("Create quiz...");

        quizRepository.generateQuiz(BuildConfig.GEMINI_API_KEY, subject, level, 10, new QuizRepository.OnQuestionsLoadedListener() {
            @Override
            public void onSuccess(List<Question> questions) {
                btnCreateQuiz.setEnabled(true);
                btnCreateQuiz.setText(getString(R.string.btn_create_quiz));
                
                questionList = questions;
                currentQuestionIndex = 0;
                score = 0;
                timeLeftInMillis = 300000; // Reset timer to 5 mins

                layoutQuizSetup.setVisibility(View.GONE);
                layoutQuizContent.setVisibility(View.VISIBLE);
                layoutQuizResult.setVisibility(View.GONE);

                startTimer();
                displayQuestion();
            }

            @Override
            public void onError(String errorMessage) {
                btnCreateQuiz.setEnabled(true);
                btnCreateQuiz.setText(getString(R.string.btn_create_quiz));
                Toast.makeText(QuizActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                showResult();
            }
        }.start();
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimer.setText(timeFormatted);
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            Question question = questionList.get(currentQuestionIndex);
            tvQuestion.setText(question.getText());
            List<String> options = question.getOptions();
            for (int i = 0; i < 4; i++) {
                rbOptions[i].setText(options.get(i));
                rbOptions[i].setEnabled(true);
            }
            rgOptions.clearCheck();

            int progress = (int) (((float) (currentQuestionIndex + 1) / questionList.size()) * 100);
            progressBar.setProgress(progress);
            tvProgressText.setText((currentQuestionIndex + 1) + "/" + questionList.size());

            if (currentQuestionIndex == questionList.size() - 1) {
                btnNext.setText("Finish");
            } else {
                btnNext.setText("Next");
            }
        }
    }

    private void handleNextQuestion() {
        int selectedId = rgOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedIndex = -1;
        for (int i = 0; i < 4; i++) {
            if (rbOptions[i].getId() == selectedId) {
                selectedIndex = i;
                break;
            }
        }

        int correctIndex = questionList.get(currentQuestionIndex).getCorrectOptionIndex();


        for (RadioButton rb : rbOptions) rb.setEnabled(false);
        btnNext.setEnabled(false);


        ViewCompat.setBackgroundTintList(
                rbOptions[correctIndex],
                ColorStateList.valueOf(Color.parseColor("#4CAF50")));

        if (selectedIndex != correctIndex) {
            ViewCompat.setBackgroundTintList(
                    rbOptions[selectedIndex],
                    ColorStateList.valueOf(Color.parseColor("#E53935")));
        } else {
            score++;
        }

        new android.os.Handler().postDelayed(() -> {

            for (RadioButton rb : rbOptions) {
                ViewCompat.setBackgroundTintList(rb, null);
                rb.setEnabled(true);
            }
            btnNext.setEnabled(true);

            currentQuestionIndex++;
            if (currentQuestionIndex < questionList.size()) {
                displayQuestion();
            } else {
                showResult();
            }
        }, 1200);
    }

    private void showResult() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        layoutQuizSetup.setVisibility(View.GONE);
        layoutQuizContent.setVisibility(View.GONE);
        layoutQuizResult.setVisibility(View.VISIBLE);

        tvScoreDetail.setText("Your Score: " + score + "/" + questionList.size());

        int durationSec = (int) (300 - (timeLeftInMillis / 1000));

        QuizResult result = new QuizResult(
                atvSubject.getText().toString(),
                atvLevel.getText().toString(),
                score,
                questionList.size(),
                durationSec,
                System.currentTimeMillis()
        );

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users")
                .document(uid)
                .collection("history")
                .add(result)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(QuizActivity.this, "Quiz saved successfully", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(QuizActivity.this, e.getMessage(), Toast.LENGTH_LONG
                    ).show();

                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
