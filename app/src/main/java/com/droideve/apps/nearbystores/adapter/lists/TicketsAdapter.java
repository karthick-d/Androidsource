package com.droideve.apps.nearbystores.adapter.lists;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.animation.ImageLoaderAnimation;
import com.droideve.apps.nearbystores.classes.Coupon;
import com.droideve.apps.nearbystores.classes.Ticket;

import java.util.ArrayList;
import java.util.List;

public class TicketsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Ticket> items = new ArrayList<>();
    private Context ctx;
    private ClickListener mClickListener;

    public TicketsAdapter(Context context, List<Ticket> items) {
        this.items = items;
        ctx = context;
    }

    public List<Ticket> getItems() {
        return items;
    }

    public void setItems(List<Ticket> items) {
        this.items = items;
    }

    public void setClickListener(final ClickListener mItemClickListener) {
        this.mClickListener = mItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        vh = new TicketViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof TicketViewHolder) {

            TicketViewHolder view = (TicketViewHolder) holder;
            final Ticket object = items.get(position);

            if (object.getListImages() != null
                    && object.getListImages().size()> 0
                    && object.getListImages().get(0).getUrl200_200() != null
            ) {
                Glide.with(ctx)
                        .load(object.getListImages()
                                .get(0).getUrl200_200())
                        .dontTransform()
                        .placeholder(ImageLoaderAnimation.glideLoader(ctx))
                        .into(((TicketViewHolder) holder).image);
            } else {
                Glide.with(ctx).load(R.drawable.def_logo).into(((TicketViewHolder) holder).image);
            }

            ((TicketViewHolder) holder).eventName.setText(object.getEventName());
            ((TicketViewHolder) holder).ticketId.setText(String.format(ctx.getString(R.string.ticketId),  "#"+this.padLeftZeros(String.valueOf(object.getId()),6)   ) );

            if(object.getStatus() == 0){
                ((TicketViewHolder) holder).status.setText(ctx.getString(R.string.coupon_unverified));
                ((TicketViewHolder) holder).status.setBackgroundTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.orange_600)));
            }else  if(object.getStatus() == 1){
                ((TicketViewHolder) holder).status.setText(ctx.getString(R.string.ticketConfirmed));
                ((TicketViewHolder) holder).status.setBackgroundTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.green)));
            }else  if(object.getStatus() == 2){
                ((TicketViewHolder) holder).status.setText(ctx.getString(R.string.coupon_used));
                ((TicketViewHolder) holder).status.setBackgroundTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.blue)));
            }else{
                ((TicketViewHolder) holder).status.setText(ctx.getString(R.string.coupon_canceled));
                ((TicketViewHolder) holder).status.setBackgroundTintList(ColorStateList.valueOf(ctx.getResources().getColor(R.color.red)));
            }

            if(!object.getAttachementUrl().equals("")){
                ((TicketViewHolder) holder).attachmentBadge.setVisibility(View.VISIBLE);
            }else{
                ((TicketViewHolder) holder).attachmentBadge.setVisibility(View.GONE);
            }

        }
    }

    public String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }

    public void addItem(Ticket item) {
        if (item != null) {
            if (items.add(item)) {
                notifyDataSetChanged();
            }
        }

    }

    public void updateItem(final int position, final Ticket item) {
        if (item != null && position >= 0) {
            items.set(position, item);
            notifyDataSetChanged();
        }
    }

    public void removeItem(final int noti_id, final int position) {
        items.remove(position);
        notifyDataSetChanged();
    }

    public void addAll(final List<Ticket> productList) {
        items.addAll(productList);
        notifyDataSetChanged();
    }

    public void removeAll() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public interface ClickListener {
        void onItemClick(View view, int pos);

        void onMoreButtonClick(View view, int position);

    }

    public class TicketViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView ticketId;
        TextView eventName;
        ImageView image;
        TextView status;
        TextView attachmentBadge;
        LinearLayout layout;
        ImageView menu;

        public TicketViewHolder(View v) {
            super(v);

            layout = v.findViewById(R.id.layout);
            ticketId = v.findViewById(R.id.ticketId);
            eventName = v.findViewById(R.id.eventName);
            menu = v.findViewById(R.id.menu);
            image = v.findViewById(R.id.image);
            status = v.findViewById(R.id.status);
            attachmentBadge = v.findViewById(R.id.attachmentBadge);

            //set click listeners
            layout.setOnClickListener(this);
            menu.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.container) {
                if (mClickListener != null) {
                    mClickListener.onItemClick(view, getLayoutPosition());
                }
            } else if (view.getId() == R.id.menu) {
                if (mClickListener != null) {
                    mClickListener.onMoreButtonClick(view, getLayoutPosition());
                }
            }

        }
    }


}