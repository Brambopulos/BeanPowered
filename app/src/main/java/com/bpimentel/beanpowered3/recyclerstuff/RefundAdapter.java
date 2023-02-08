package com.bpimentel.beanpowered3.recyclerstuff;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.objects.Refund;

import java.util.ArrayList;

public class RefundAdapter extends RecyclerView.Adapter<RefundAdapter.RefundHolder>{
    private final ItemClicker mRefundListener;
    private Context mContext;
    private ArrayList<Refund> mRefunds;
    private int position;

    public RefundAdapter(Context context, ArrayList<Refund> refunds, ItemClicker refundListener){
        this.mContext = context;
        this.mRefunds = refunds;
        this.mRefundListener = refundListener;
    }

    public int getPosition() {return position;}

    public void setPosition(int position){this.position = position; }

    @NonNull
    @Override
    public RefundHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(mContext).inflate
                (R.layout.refund_recycler,parent,false);

        return new RefundHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RefundAdapter.RefundHolder holder, int position) {
        Refund refund = mRefunds.get(position);
        holder.setDetails(refund);
    }

    @Override
    public int getItemCount() {
        return mRefunds.size();
    }


    class RefundHolder extends RecyclerView.ViewHolder{
        private TextView gameTitle, userName, refundReason;

        public RefundHolder(@NonNull View itemView){
            super(itemView);
            gameTitle = itemView.findViewById(R.id.refundGameTitle);
            userName = itemView.findViewById(R.id.refundUserName);
            refundReason = itemView.findViewById(R.id.refundReason);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mRefundListener != null){
                        int position = getAdapterPosition();

                        if(position != RecyclerView.NO_POSITION){
                            mRefundListener.onItemClick(view, position);
                        }
                    }
                }
            });

        }

        void setDetails(Refund refund){
            gameTitle.setText(refund.getGame());
            userName.setText(refund.getUser());
            refundReason.setText(refund.getReason());
        }
    }
}