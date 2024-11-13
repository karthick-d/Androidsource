package com.droideve.apps.nearbystores.adapter.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.classes.WTransaction;
import com.droideve.apps.nearbystores.utils.DateUtils;

import java.util.List;


public class TransactionWalletAdapter extends RecyclerView.Adapter<TransactionWalletAdapter.mViewHolder> {


    private final LayoutInflater infalter;
    private final List<WTransaction> data;
    private final Context context;
    private ClickListener clickListener;
    private int selectedPos = RecyclerView.NO_POSITION;
    private final int lastPosition = -1;
    private final boolean on_attach = true;

    private final int parent_width = 0;
    private final int rest = 0;

    public TransactionWalletAdapter(Context context, List<WTransaction> data) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
    }


    public List<WTransaction> getData() {
        return data;
    }

    @Override
    public mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rootView = infalter.inflate(R.layout.item_wallet_transaction, parent, false);
        mViewHolder holder = new mViewHolder(rootView);
        return holder;
    }


    @Override
    public void onBindViewHolder(mViewHolder holder, int position) {

        WTransaction mPG = data.get(position);

        holder.operation.setText(mPG.getOperation());
        holder.date.setText(DateUtils.prepareOutputDate(mPG.getDate(), "dd MMMM yyyy hh:mm", context));
        holder.transactionId.setText("#"+mPG.getNo());

        String note = "";

        if(!mPG.getNote().equals("") && !mPG.getNote().equals("--") && !mPG.getNote().equals("null")){
            note = " ("+mPG.getNote()+")";
        }else{
            note = "";
        }


        if(mPG.getOperation().equals("receive")){
            holder.operation.setText(context.getString(R.string.received)+note);
            holder.value.setText("+"+mPG.getAmount());
            holder.value.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.green,null));
        }else if(mPG.getOperation().equals("send")){
            holder.operation.setText(context.getString(R.string.sent)+note);
            holder.value.setText("-"+mPG.getAmount());
            holder.value.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.red,null));
        }else if(mPG.getOperation().equals("top-up")){
            holder.operation.setText(context.getString(R.string.added_balance_to));
            holder.value.setText("+"+mPG.getAmount());
            holder.value.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.green,null));
        }else{
            holder.operation.setText(mPG.getOperation()+note);
            holder.value.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.grey,null));
        }
    }


    public void removeAll() {

        int size = this.data.size();

        if (size > 0) {
            for (int i = 0; i < size; i++) {
                this.data.remove(0);
            }

            if (size > 0)
                this.notifyItemRangeRemoved(0, size);
        }
    }

    public WTransaction getItemDetail(int position) {
        if (position >= 0) {
            return data.get(position);
        }
        return null;
    }

    public void addItem(WTransaction item) {

        int index = (data.size());
        data.add(item);
        notifyItemInserted(index);
    }


    public void addAll(final List<WTransaction> list) {
        int size = list.size();

        data.clear();
        if (size > 0) {
            //remove all data before adding new items
            for (int i = 0; i < size; i++) {
                data.add(list.get(i));
            }

            notifyDataSetChanged();
        }


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setClickListener(ClickListener clicklistener) {

        this.clickListener = clicklistener;

    }


    public interface ClickListener {
        void itemClicked(View view, int position);
    }

    public class mViewHolder extends RecyclerView.ViewHolder {

        TextView date;
        TextView operation;
        TextView value;
        TextView transactionId;

        public mViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            operation = itemView.findViewById(R.id.operation);
            value = itemView.findViewById(R.id.value);
            transactionId = itemView.findViewById(R.id.transactionId);

        }


    }


}
