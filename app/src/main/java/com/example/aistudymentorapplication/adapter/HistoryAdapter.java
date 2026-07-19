package com.example.aistudymentorapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aistudymentorapplication.R;
import com.example.aistudymentorapplication.model.QuizResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<QuizResult> list;

    public HistoryAdapter(List<QuizResult> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        QuizResult result=list.get(position);

        holder.tvSubject.setText(result.getSubject());

        holder.tvLevel.setText("Level : "+result.getLevel());

        holder.tvScore.setText("Score : "+result.getScore()+"/"+result.getTotal());

        holder.tvDuration.setText("Duration : "+result.getDurationSec()+" sec");

        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        holder.tvDate.setText(
                sdf.format(new Date(result.getCreatedAt()))
        );

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvSubject,tvLevel,tvScore,tvDuration,tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSubject=itemView.findViewById(R.id.tvSubject);
            tvLevel=itemView.findViewById(R.id.tvLevel);
            tvScore=itemView.findViewById(R.id.tvScore);
            tvDuration=itemView.findViewById(R.id.tvDuration);
            tvDate=itemView.findViewById(R.id.tvDate);

        }
    }

}