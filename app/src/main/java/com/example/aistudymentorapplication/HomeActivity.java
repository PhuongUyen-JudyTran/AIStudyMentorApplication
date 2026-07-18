package com.example.aistudymentorapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aistudymentorapplication.adapter.ChatAdapter;
import com.example.aistudymentorapplication.model.ChatMessage;
import com.example.aistudymentorapplication.model.ChatSession;
import com.example.aistudymentorapplication.repository.ChatRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.aistudymentorapplication.BuildConfig;

/**
 * HomeActivity handles the main AI Chat interface using SQLite and Gemini API.
 */
public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private FloatingActionButton btnSend;
    private ImageButton btnHistory, btnSignOut;
    private ProgressBar pbLoading;

    private ChatAdapter adapter;
    private ChatRepository repository;
    private long currentSessionId = -1;
    private BottomNavigationView bottomNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupRecyclerView();
        repository = new ChatRepository(getApplication());
        bottomNavigation = findViewById(R.id.bottomNavigation);
        setupNavigation();
        currentSessionId = getIntent().getLongExtra("SESSION_ID", -1);
        if (currentSessionId != -1) {
            loadMessages();
        }

        btnSend.setOnClickListener(v -> sendMessage());
        btnSignOut.setOnClickListener(v -> signOut());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_chat);
        }
    }

    private void initViews() {
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnSignOut = findViewById(R.id.btnSignOut);
        pbLoading = findViewById(R.id.pbLoading);
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter();
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);
    }

    /**
     * Loads messages from SQLite and updates the RecyclerView.
     */
    private void loadMessages() {
        repository.getMessagesForSession(currentSessionId, messages -> {
            if (messages != null) {
                adapter.setMessages(messages);
                if (!messages.isEmpty()) {
                    rvChat.smoothScrollToPosition(messages.size() - 1);
                }
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        etMessage.setText("");
        btnSend.setEnabled(false);

        if (currentSessionId == -1) {
            // New Session: Generate title from first user message
            String title = text.length() > 30 ? text.substring(0, 27) + "..." : text;
            ChatSession session = new ChatSession(title, System.currentTimeMillis(), System.currentTimeMillis());

            repository.createSession(session, sessionId -> {
                currentSessionId = sessionId;
                saveAndSend(text);
            });
        } else {
            saveAndSend(text);
        }
    }

    private void saveAndSend(String text) {
        // Save user message
        ChatMessage userMsg = new ChatMessage(currentSessionId, "user", text, System.currentTimeMillis());
        repository.saveMessage(userMsg);
        repository.updateSessionTime(currentSessionId);

        // Update UI immediately for user message
        adapter.addMessage(userMsg);
        rvChat.smoothScrollToPosition(adapter.getItemCount() - 1);

        pbLoading.setVisibility(View.VISIBLE);

        // 2. Call Gemini API via Repository with session context
        repository.getAiResponse(BuildConfig.GEMINI_API_KEY, currentSessionId, text, new ChatRepository.OnResponseListener() {
            @Override
            public void onSuccess(String response) {
                // Save AI response to database
                ChatMessage aiMsg = new ChatMessage(currentSessionId, "ai", response, System.currentTimeMillis());
                repository.saveMessage(aiMsg);
                repository.updateSessionTime(currentSessionId);

                runOnUiThread(() -> {
                    pbLoading.setVisibility(View.GONE);
                    btnSend.setEnabled(true);
                    adapter.addMessage(aiMsg);
                    rvChat.smoothScrollToPosition(adapter.getItemCount() - 1);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(HomeActivity.this, error, Toast.LENGTH_SHORT).show();
                    btnSend.setEnabled(true);
                });
            }
        });
    }

    private void signOut() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_chat);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_chat) {
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
                return true;
            } else if (id == R.id.nav_quiz) {
                startActivity(new Intent(HomeActivity.this, QuizActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }
}