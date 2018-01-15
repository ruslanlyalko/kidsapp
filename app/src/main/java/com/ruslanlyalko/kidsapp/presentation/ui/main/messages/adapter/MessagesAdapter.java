package com.ruslanlyalko.kidsapp.presentation.ui.main.messages.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.models.Message;
import com.ruslanlyalko.kidsapp.data.models.Notification;
import com.ruslanlyalko.kidsapp.presentation.ui.main.messages.MessageDetailsActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MyViewHolder> {

    private Context mContext;
    private List<Message> mMessageList;
    private List<Notification> mNotifications = new ArrayList<>();

    public MessagesAdapter(Context mContext, List<Message> messageList) {
        this.mContext = mContext;
        this.mMessageList = messageList;
    }

    public void updateNotifs(final List<Notification> notifications) {
        mNotifications.clear();
        mNotifications.addAll(notifications);
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_notification, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Message message = mMessageList.get(position);
        holder.bindData(message);
    }

    /*
        private void removeNotification(Notification notification, int position, MyViewHolder holder) {
            //Close panel
            editNotification(notification, holder, false);

            mMessageList.remove(position);
            mDatabase.getReference(Constants.DB_MESSAGES)
                    .child(notification.getKey()).removeValue();
            notifyItemRemoved(position);
        }


        private void updateMk(Notification updatedNotification) {
            mDatabase.getReference(Constants.DB_MESSAGES)
                    .child(updatedNotification.getKey()).setValue(updatedNotification);
        }
    */
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.title1) TextView title1;
        @BindView(R.id.title2) TextView title2;
        @BindView(R.id.text_date) TextView date;
        @BindView(R.id.panel_item) LinearLayout panelItem;
        @BindView(R.id.image_view) ImageView mImageView;

        MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bindData(final Message message) {
            boolean isNew = mNotifications.contains(new Notification(message.getKey()));
            mImageView.setImageDrawable(mImageView.getResources().getDrawable(isNew
                    ? R.drawable.ic_comment_primary : R.drawable.ic_comment));
            title1.setText(message.getTitle1());
            title2.setText(message.getTitle2());
            date.setText(message.getDate());
            panelItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, MessageDetailsActivity.class);
                    intent.putExtra(Keys.Extras.EXTRA_NOT_ID, message.getKey());
                    mContext.startActivity(intent);
                }
            });
        }
    }
}