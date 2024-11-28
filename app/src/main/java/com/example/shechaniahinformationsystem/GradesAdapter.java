package com.example.shechaniahinformationsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GradesAdapter extends RecyclerView.Adapter<GradesAdapter.GradeViewHolder> {
    private List<Grade> gradesList;

    public GradesAdapter(List<Grade> gradesList) {
        this.gradesList = gradesList;
    }

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new GradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        Grade grade = gradesList.get(position);

        if (grade != null) {
            // Set the subject text
            holder.subjectTextView.setText(grade.getSubject());

            // Create a single string for all grades and set it to the gradesTextView
            String gradesString = " " + grade.getG1() + "     " + grade.getG2() +
                    "      " + grade.getG3() + "      " + grade.getG4() +
                    "      " + grade.getFinalGrade();

            holder.gradesTextView.setText(gradesString);
        }
    }

    @Override
    public int getItemCount() {
        return gradesList.size();
    }

    static class GradeViewHolder extends RecyclerView.ViewHolder {
        TextView subjectTextView, gradesTextView;

        public GradeViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectTextView = itemView.findViewById(R.id.subject);
            gradesTextView = itemView.findViewById(R.id.gradesTextView);
        }
    }
}
