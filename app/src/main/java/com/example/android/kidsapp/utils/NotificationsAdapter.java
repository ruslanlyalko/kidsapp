package com.example.android.kidsapp.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.kidsapp.NotItemActivity;
import com.example.android.kidsapp.R;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.MyViewHolder> {

    private Context mContext;
    private List<Notification> notificationList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title1, title2, date;
        LinearLayout panelItem;

        MyViewHolder(View view) {
            super(view);
            title1 = (TextView) view.findViewById(R.id.title1);
            title2 = (TextView) view.findViewById(R.id.title2);
            date = (TextView) view.findViewById(R.id.text_date);
            panelItem = (LinearLayout) view.findViewById(R.id.panel_item);

        }
    }


    public NotificationsAdapter(Context mContext, List<Notification> notificationList) {
        this.mContext = mContext;
        this.notificationList = notificationList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_notification, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Notification notification = notificationList.get(position);
        holder.title1.setText(notification.getTitle1());
        holder.title2.setText(notification.getTitle2());
        holder.date.setText(notification.getDate());

        holder.panelItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, NotItemActivity.class);
                intent.putExtra( Constants.EXTRA_NOT_ID, notification.getKey());
                mContext.startActivity(intent);
            }
        });
    }

    /*
        private void removeNotification(Notification notification, int position, MyViewHolder holder) {
            //Close panel
            editNotification(notification, holder, false);

            notificationList.remove(position);
            mDatabase.getReference(Constants.FIREBASE_REF_NOTIFICATIONS)
                    .child(notification.getKey()).removeValue();
            notifyItemRemoved(position);
        }


        private void updateMk(Notification updatedNotification) {
            mDatabase.getReference(Constants.FIREBASE_REF_NOTIFICATIONS)
                    .child(updatedNotification.getKey()).setValue(updatedNotification);
        }
    */
    @Override
    public int getItemCount() {
        return notificationList.size();
    }
}