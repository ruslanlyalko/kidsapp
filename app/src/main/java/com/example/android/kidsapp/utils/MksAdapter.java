package com.example.android.kidsapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.kidsapp.R;
import com.example.android.kidsapp.ReportActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.List;

public class MksAdapter extends RecyclerView.Adapter<MksAdapter.MyViewHolder> {

    private Context mContext;
    private List<Mk> mkList;

    boolean isEdit = false;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title1, title2, largeText, date, count;
        public ImageView image1, imageExpand;
        public LinearLayout expandPanel;
        public Button buttonAdd, buttonEdit, buttonDelete;
        public EditText inputTitle1, inputTitle2, inputTextLarge;

        public MyViewHolder(View view) {
            super(view);
            title1 = (TextView) view.findViewById(R.id.title1);
            inputTitle1 = (EditText) view.findViewById(R.id.input_title1);
            title2 = (TextView) view.findViewById(R.id.title2);
            inputTitle2 = (EditText) view.findViewById(R.id.input_title2);

            date = (TextView) view.findViewById(R.id.text_date);
            count = (TextView) view.findViewById(R.id.text_cout);
            largeText = (TextView) view.findViewById(R.id.large_text);
            inputTextLarge = (EditText) view.findViewById(R.id.input_text_large);


            image1 = (ImageView) view.findViewById(R.id.image1);
            expandPanel = (LinearLayout) view.findViewById(R.id.panel_expand);
            imageExpand = (ImageView) view.findViewById(R.id.image_expand);
            buttonAdd = (Button) view.findViewById(R.id.button_add_zvit);
            buttonEdit = (Button) view.findViewById(R.id.button_edit);
            buttonDelete = (Button) view.findViewById(R.id.button_delete);
        }
    }


    public MksAdapter(Context mContext, List<Mk> mkList) {
        this.mContext = mContext;
        this.mkList = mkList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mk_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Mk mk = mkList.get(position);
        holder.title1.setText(mk.getTitle1());
        holder.title2.setText(mk.getTitle2());
        holder.largeText.setText(mk.getLargeText());
        holder.date.setText(mk.getDate());
        holder.count.setText(mk.getCount() + " разів");

        // loading mk cover using Glide library
        Glide.with(mContext).load(mk.getImageId()).into(holder.image1);

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

        holder.buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addToReport(mk);
                Snackbar sn = Snackbar.make(holder.buttonAdd, "Добавлено. Перейти у звіт?", Snackbar.LENGTH_LONG)
                        .setAction("ПЕРЕЙТИ", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext, ReportActivity.class);
                                mContext.startActivity(intent);
                            }
                        }).setActionTextColor(Color.YELLOW);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    sn.setActionTextColor(mContext.getColor(R.color.colorPrimary));
                }
                sn.show();

            }
        });
        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.dialog_delete_mk_title)
                        .setMessage(R.string.dialog_delete_mk_message)
                        .setPositiveButton("Видалити", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                removeMk(mk, position, holder);
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
                editMK(mk, holder, true);
            }
        });


    }


    private void addToReport(Mk mk) {
        String userName = mAuth.getCurrentUser().getDisplayName();
        String mUId = mAuth.getCurrentUser().getUid();

        Calendar today = Calendar.getInstance();

        String dateStr = (today.get(Calendar.DAY_OF_MONTH)) + "-" + (today.get(Calendar.MONTH) + 1) + "-" + today.get(Calendar.YEAR);

        String dateDay = today.get(Calendar.DAY_OF_MONTH)+"";
        String dateMonth = (today.get(Calendar.MONTH) + 1)+"";
        String dateYear = today.get(Calendar.YEAR)+"";


        DatabaseReference ref = mDatabase.getReference(Constants.FIREBASE_REF_USER_REPORTS)
                .child(dateYear).child(dateMonth).child(dateDay).child(mUId);

        ref.child("mkRef").setValue(mk.getKey());
        ref.child("mkName").setValue(mk.getTitle1());
        ref.child("userId").setValue(mUId);
        ref.child("date").setValue(dateStr);
        ref.child("userName").setValue(userName);
    }

    private void editMK(Mk mk, MyViewHolder holder, boolean save) {
        if (isEdit) {
            holder.buttonEdit.setText("РЕДАГУВАТИ");
            holder.buttonAdd.setVisibility(View.VISIBLE);
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

            mk.setTitle1(newTitle1);
            mk.setTitle2(newTitle2);
            mk.setLargeText(newTextLarge);

            //holder.imageExpand.setImageResource(R.drawable.ic_action_expand_more);
            //holder.expandPanel.setVisibility(View.GONE);
            if (save)
                updateMk(mk);

            isEdit = false;
            notifyDataSetChanged();
        } else {
            holder.buttonEdit.setText("ЗБЕРЕГТИ");
            holder.buttonAdd.setVisibility(View.GONE);
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

    private void removeMk(Mk mk, int position, MyViewHolder holder) {
        //Close panel
        editMK(mk, holder, false);

        mkList.remove(position);
        FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_REF_MK)
                .child(mk.getKey()).removeValue();
        notifyItemRemoved(position);
    }


    private void updateMk(Mk updatedMk) {
        FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_REF_MK)
                .child(updatedMk.getKey()).setValue(updatedMk);
    }

    @Override
    public int getItemCount() {
        return mkList.size();
    }
}