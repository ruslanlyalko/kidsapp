package com.ruslanlyalko.kidsapp.presentation.ui.notifications.adapter;

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
import com.ruslanlyalko.kidsapp.data.models.Notif;
import com.ruslanlyalko.kidsapp.data.models.Notification;
import com.ruslanlyalko.kidsapp.presentation.ui.notifications.NotificationDetailsActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.MyViewHolder> {

    private Context mContext;
    private List<Notification> notificationList;
    private List<Notif> mNotifs = new ArrayList<>();

    public NotificationsAdapter(Context mContext, List<Notification> notificationList) {
        this.mContext = mContext;
        this.notificationList = notificationList;
    }

    public void updateNotifs(final List<Notif> notifs) {
        mNotifs.clear();
        mNotifs.addAll(notifs);
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
        final Notification notification = notificationList.get(position);
        holder.bindData(notification);
    }

    /*
        private void removeNotification(Notif notification, int position, MyViewHolder holder) {
            //Close panel
            editNotification(notification, holder, false);

            notificationList.remove(position);
            mDatabase.getReference(Constants.DB_NOTIFICATIONS)
                    .child(notification.getKey()).removeValue();
            notifyItemRemoved(position);
        }


        private void updateMk(Notif updatedNotification) {
            mDatabase.getReference(Constants.DB_NOTIFICATIONS)
                    .child(updatedNotification.getKey()).setValue(updatedNotification);
        }
    */
    @Override
    public int getItemCount() {
        return notificationList.size();
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

        void bindData(final Notification notification) {
            boolean isNew = mNotifs.contains(new Notif(notification.getKey()));
            mImageView.setImageDrawable(mImageView.getResources().getDrawable(isNew
                    ? R.drawable.ic_comment_primary : R.drawable.ic_comment));
            title1.setText(notification.getTitle1());
            title2.setText(notification.getTitle2());
            date.setText(notification.getDate());
            panelItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, NotificationDetailsActivity.class);
                    intent.putExtra(Keys.Extras.EXTRA_NOT_ID, notification.getKey());
                    mContext.startActivity(intent);
                }
            });
        }
    }
}