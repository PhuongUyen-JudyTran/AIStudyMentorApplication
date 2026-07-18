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

import com.example.aistudymentorapplication.model.Question;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
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
    private long timeLeftInMillis = 300000; // 5 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

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

        loadDummyQuestions(subject, level);
        
        layoutQuizSetup.setVisibility(View.GONE);
        layoutQuizContent.setVisibility(View.VISIBLE);
        layoutQuizResult.setVisibility(View.GONE);
        
        startTimer();
        displayQuestion();
    }

    private void startTimer() {
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

    private void loadDummyQuestions(String subject, String level) {
        questionList = new ArrayList<>();
        questionList.add(new Question("What is the " + level + " concept in " + subject + "?",
                Arrays.asList("Concept A", "Concept B", "Concept C", "Concept D"), 1));
        questionList.add(new Question("Which of these is related to " + subject + "?",
                Arrays.asList("Topic 1", "Topic 2", "Topic 3", "Topic 4"), 2));
        questionList.add(new Question("How do you apply " + level + " skills in " + subject + "?",
                Arrays.asList("Method 1", "Method 2", "Method 3", "Method 4"), 0));
        questionList.add(new Question("Explain a core rule in " + subject + " for " + level + " students.",
                Arrays.asList("Rule 1", "Rule 2", "Rule 3", "Rule 4"), 3));
        questionList.add(new Question("Final check for " + subject + " at " + level + " level.",
                Arrays.asList("All correct", "Half correct", "Mostly correct", "None of these"), 0));
        questionList.add(new Question("Final check for " + subject + " at " + level + " level.",
                Arrays.asList("All correct", "Half correct", "Mostly correct", "None of these"), 1));
        questionList.add(new Question("Final check for " + subject + " at " + level + " level.",
                Arrays.asList("All correct", "Half correct", "Mostly correct", "None of these"), 3));
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            Question question = questionList.get(currentQuestionIndex);
            tvQuestion.setText(question.getText());
            List<String> options = question.getOptions();
            for (int i = 0; i < 4; i++) {
                rbOptions[i].setText(options.get(i));
            }
            rgOptions.clearCheck();

            int progress = (int) (((float) (currentQuestionIndex + 1) / questionList.size()) * 100);
            progressBar.setProgress(progress);
            tvProgressText.setText(getString(R.string.label_question, currentQuestionIndex + 1) + "/" + questionList.size());

            if (currentQuestionIndex == questionList.size() - 1) {
                btnNext.setText(R.string.btn_finish);
            } else {
                btnNext.setText(R.string.btn_next);
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

        if (selectedIndex == questionList.get(currentQuestionIndex).getCorrectOptionIndex()) {
            score++;
        }

        currentQuestionIndex++;
        if (currentQuestionIndex < questionList.size()) {
            displayQuestion();
        } else {
            showResult();
        }
    }

    private void showResult() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        layoutQuizSetup.setVisibility(View.GONE);
        layoutQuizContent.setVisibility(View.GONE);
        layoutQuizResult.setVisibility(View.VISIBLE);
        
        tvScoreDetail.setText(getString(R.string.label_score_detail, score, questionList.size()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
