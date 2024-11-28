package com.example.shechaniahinformationsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.PaymentViewHolder> {
    private List<Payment> paymentList;

    public PaymentsAdapter(List<Payment> paymentList) {
        this.paymentList = paymentList;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);
        holder.dateTextView.setText(payment.getDate());
        holder.amountTextView.setText(payment.getAmount());
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, amountTextView;

        public PaymentViewHolder(View view) {
            super(view);
            dateTextView = view.findViewById(R.id.paymentDate);
            amountTextView = view.findViewById(R.id.paymentAmount);
        }
    }
}