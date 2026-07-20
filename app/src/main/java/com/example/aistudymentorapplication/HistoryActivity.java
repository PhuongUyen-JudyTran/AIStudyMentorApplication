package com.example.aistudymentorapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aistudymentorapplication.adapter.SessionAdapter;
import com.example.aistudymentorapplication.model.ChatSession;
import com.example.aistudymentorapplication.repository.ChatRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * HistoryActivity displays the list of past chat sessions stored in SQLite.
 */
public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private SessionAdapter adapter;
    private ChatRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
        FloatingActionButton btnNewChat = findViewById(R.id.btnNewChat);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        repository = new ChatRepository(getApplication());
        setupRecyclerView();

        // Initial load of sessions
        loadSessions();

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
                intent.putExtra("SESSION_ID", session.getSessionId());
                startActivity(intent);
                finish();
            }

            @Override
            public void onSessionDelete(ChatSession session) {
                repository.deleteSession(session.getSessionId(), () -> loadSessions());
            }

            @Override
            public void onSessionRename(ChatSession session) {
                showRenameDialog(session);
            }
        });
    }

    private void loadSessions() {
        repository.getAllSessions(sessions -> {
            if (sessions != null) {
                adapter.setSessions(sessions);
            }
        });
    }

    private void showRenameDialog(ChatSession session) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Chat");

        final EditText input = new EditText(this);
        input.setText(session.getTitle());
        
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = params.rightMargin = 50; 
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                session.setTitle(newTitle);
                repository.updateSession(session);
                loadSessions(); // Refresh list
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
