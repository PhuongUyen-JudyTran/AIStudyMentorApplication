package com.example.aistudymentorapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aistudymentorapplication.adapter.SessionAdapter;
import com.example.aistudymentorapplication.model.ChatSession;
import com.example.aistudymentorapplication.repository.ChatRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private FloatingActionButton btnNewChat;
    private SessionAdapter adapter;
    private ChatRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
        btnNewChat = findViewById(R.id.btnNewChat);

        repository = new ChatRepository(getApplication());
        setupRecyclerView();

        repository.getAllSessions().observe(this, sessions -> {
            if (sessions != null) {
                adapter.setSessions(sessions);
            }
        });

        btnNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("SESSION_ID", -1L);
            startActivity(intent);
            finish();
        });
    }

    private void setupRecyclerView() {
        adapter = new SessionAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);

        adapter.setOnSessionClickListener(new SessionAdapter.OnSessionClickListener() {
            @Override
            public void onSessionClick(ChatSession session) {
                Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
                intent.putExtra("SESSION_ID", session.sessionId);
                startActivity(intent);
                finish();
            }

            @Override
            public void onSessionDelete(ChatSession session) {
                repository.deleteSession(session);
            }

            @Override
            public void onSessionRename(ChatSession session) {
                showRenameDialog(session);
            }
        });
    }

    private void showRenameDialog(ChatSession session) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Rename Chat");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(session.getTitle());
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                session.title = newTitle;
                repository.updateSession(session);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
