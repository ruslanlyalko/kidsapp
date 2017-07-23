package com.example.android.kidsapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.kidsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.MyViewHolder> {

    private Context mContext;
    private List<Notification> notificationList;

    boolean isEdit = false;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title1, title2, largeText, date;
        public ImageView imageExpand;
        public LinearLayout expandPanel;
        public Button buttonEdit, buttonDelete;
        public EditText inputTitle1, inputTitle2, inputTextLarge;

        public MyViewHolder(View view) {
            super(view);
            title1 = (TextView) view.findViewById(R.id.title1);
            inputTitle1 = (EditText) view.findViewById(R.id.input_title1);
            title2 = (TextView) view.findViewById(R.id.title2);
            inputTitle2 = (EditText) view.findViewById(R.id.input_title2);

            date = (TextView) view.findViewById(R.id.text_date);
            largeText = (TextView) view.findViewById(R.id.text_description);
            inputTextLarge = (EditText) view.findViewById(R.id.input_text_large);


            expandPanel = (LinearLayout) view.findViewById(R.id.panel_expand);
            imageExpand = (ImageView) view.findViewById(R.id.image_expand);
            buttonEdit = (Button) view.findViewById(R.id.button_edit);
            buttonDelete = (Button) view.findViewById(R.id.button_delete);
        }
    }


    public NotificationsAdapter(Context mContext, List<Notification> notificationList) {
        this.mContext = mContext;
        this.notificationList = notificationList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Notification notification = notificationList.get(position);
        holder.title1.setText(notification.getTitle1());
        holder.title2.setText(notification.getTitle2());
        holder.largeText.setText(notification.getLargeText());
        holder.date.setText(notification.getDate());

        holder.buttonEdit.setVisibility(Utils.isIsAdmin() || notification.getUserId().equals(mAuth.getCurrentUser().getUid()) ?
                View.VISIBLE : View.GONE);

        holder.imageExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.expandPanel.getVisibility() == View.VISIBLE) {
                    holder.imageExpand.setImageResource(R.drawable.ic_action_expand_more);
                    holder.expandPanel.setVisibility(View.GONE);
                } else {
                    holder.imageExpand.setImageResource(R.drawable.ic_action_expand_less);
                    holder.expandPanel.setVisibility(View.VISIBLE);
                }
            }
        });


        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.dialog_delete_notification_title)
                        .setMessage(R.string.dialog_delete_notification_message)
                        .setPositiveButton("Видалити", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                removeNotification(notification, position, holder);
                            }

                        })
                        .setNegativeButton("Повернутись", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
            }
        });

        holder.buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNotification(notification, holder, true);
            }
        });


    }


    private void editNotification(Notification notification, MyViewHolder holder, boolean save) {
        if (isEdit) {
            holder.buttonEdit.setText("РЕДАГУВАТИ");
            holder.buttonDelete.setVisibility(View.GONE);

            holder.title1.setVisibility(View.VISIBLE);
            holder.title2.setVisibility(View.VISIBLE);
            holder.largeText.setVisibility(View.VISIBLE);

            holder.inputTitle1.setVisibility(View.GONE);
            holder.inputTitle2.setVisibility(View.GONE);
            holder.inputTextLarge.setVisibility(View.GONE);

            String newTitle1 = holder.inputTitle1.getText().toString();
            String newTitle2 = holder.inputTitle2.getText().toString();
            String newTextLarge = holder.inputTextLarge.getText().toString();

            holder.title1.setText(newTitle1);
            holder.title2.setText(newTitle2);
            holder.largeText.setText(newTextLarge);

            notification.setTitle1(newTitle1);
            notification.setTitle2(newTitle2);
            notification.setLargeText(newTextLarge);

            //holder.buttonExpand.setImageResource(R.drawable.ic_action_expand_more);
            //holder.expandPanel.setVisibility(View.GONE);
            if (save)
                updateMk(notification);

            isEdit = false;
            notifyDataSetChanged();
        } else {
            holder.buttonEdit.setText("ЗБЕРЕГТИ");
            holder.buttonDelete.setVisibility(View.VISIBLE);

            holder.title1.setVisibility(View.GONE);
            holder.title2.setVisibility(View.GONE);
            holder.largeText.setVisibility(View.GONE);

            holder.inputTitle1.setVisibility(View.VISIBLE);
            holder.inputTitle2.setVisibility(View.VISIBLE);
            holder.inputTextLarge.setVisibility(View.VISIBLE);

            String oldTitle1 = holder.title1.getText().toString();
            String oldTitle2 = holder.title2.getText().toString();
            String oldTextLarge = holder.largeText.getText().toString();

            holder.inputTitle1.setText(oldTitle1);
            holder.inputTitle2.setText(oldTitle2);
            holder.inputTextLarge.setText(oldTextLarge);

            holder.imageExpand.setImageResource(R.drawable.ic_action_expand_less);
            holder.expandPanel.setVisibility(View.VISIBLE);

            isEdit = true;
        }
    }

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

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
}