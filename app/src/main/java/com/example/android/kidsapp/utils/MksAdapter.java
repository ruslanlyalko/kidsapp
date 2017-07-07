package com.example.android.kidsapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.kidsapp.R;

import java.util.List;

public class MksAdapter extends RecyclerView.Adapter<MksAdapter.MyViewHolder> {

    private Context mContext;
    private List<Mk> mkList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title1, title2, largeText, date, count;
        public ImageView image1, imageExpand;
        public LinearLayout expandPanel;
        public Button buttonAdd;

        public MyViewHolder(View view) {
            super(view);
            title1 = (TextView) view.findViewById(R.id.title1);
            title2 = (TextView) view.findViewById(R.id.title2);
            date = (TextView) view.findViewById(R.id.text_date);
            count = (TextView) view.findViewById(R.id.text_cout);
            largeText = (TextView) view.findViewById(R.id.large_text);
            image1 = (ImageView) view.findViewById(R.id.image1);
            expandPanel = (LinearLayout) view.findViewById(R.id.panel_expand);
            imageExpand = (ImageView) view.findViewById(R.id.image_expand);
            buttonAdd = (Button) view.findViewById(R.id.button_add_zvit);

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
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Mk mk = mkList.get(position);
        holder.title1.setText(mk.getTitle1());
        holder.title2.setText(mk.getTitle2());
        holder.largeText.setText(mk.getLargeText());
        holder.date.setText(mk.getDate());
        holder.count.setText(mk.getCount());

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
                Snackbar sn = Snackbar.make(holder.buttonAdd, "Добавлено. Перейти у звіт?", Snackbar.LENGTH_LONG)
                        .setAction("ПЕРЕЙТИ", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).setActionTextColor(Color.RED);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    sn.setActionTextColor(mContext.getColor(R.color.colorPrimary));
                }
                sn.show();
            }
        });


    }


    @Override
    public int getItemCount() {
        return mkList.size();
    }
}