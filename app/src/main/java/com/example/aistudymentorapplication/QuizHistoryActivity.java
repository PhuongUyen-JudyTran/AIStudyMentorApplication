package com.example.aistudymentorapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aistudymentorapplication.adapter.HistoryAdapter;
import com.example.aistudymentorapplication.model.QuizResult;
import com.example.aistudymentorapplication.repository.QuizRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class QuizHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private HistoryAdapter adapter;
    private ArrayList<QuizResult> list;
    private QuizRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_history);
        repository = new QuizRepository(getApplication());
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        recyclerView=findViewById(R.id.recyclerHistory);

        list=new ArrayList<>();

        adapter=new HistoryAdapter(list, new HistoryAdapter.OnItemDeleteListener() {
            @Override
            public void onDeleteClick(QuizResult result, int position) {
                deleteHistoryItem(result, position);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

        loadHistory();

    }

    private void loadHistory(){

        String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(uid)
                .collection("history")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    list.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        QuizResult result = doc.toObject(QuizResult.class);

                        if (result != null) {
                            result.setDocumentId(doc.getId());
                            list.add(result);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("History", "Error loading history", e);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private void deleteHistoryItem(QuizResult result, int position) {
        repository.deleteQuizResult(result.getDocumentId(), new QuizRepository.OnDeletionListener() {
            @Override
            public void onSuccess() {
                list.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, list.size());
                Toast.makeText(QuizHistoryActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(QuizHistoryActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

}