package com.example.android.kidsapp.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.kidsapp.MkItemActivity;
import com.example.android.kidsapp.R;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class MksAdapter extends RecyclerView.Adapter<MksAdapter.MyViewHolder> {

    private Context mContext;
    private List<Mk> mkList;
    private FirebaseStorage storage = FirebaseStorage.getInstance();


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textTitle1, textTitle2, textDescription;
        public ImageView imageView;
        public ImageButton buttonExpand, buttonShare;
        public LinearLayout expandPanel;

        public MyViewHolder(View view) {
            super(view);
            buttonShare = (ImageButton) view.findViewById(R.id.button_share);
            textTitle1 = (TextView) view.findViewById(R.id.text_title1);
            textTitle2 = (TextView) view.findViewById(R.id.text_title2);
            textDescription = (TextView) view.findViewById(R.id.text_description);


            imageView = (ImageView) view.findViewById(R.id.image_view);
            expandPanel = (LinearLayout) view.findViewById(R.id.panel_expand);
            buttonExpand = (ImageButton) view.findViewById(R.id.button_expand);
        }
    }


    public MksAdapter(Context mContext, List<Mk> mkList) {
        this.mContext = mContext;
        this.mkList = mkList;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_mk, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Mk mk = mkList.get(position);
        holder.textTitle1.setText(mk.getTitle1());
        holder.textTitle2.setText(mk.getTitle2());
        holder.textDescription.setText(mk.getDescription());

        //load image if already defined
        if (mk.getImageUri() != null && !mk.getImageUri().isEmpty()) {

            StorageReference ref = storage.getReference(Constants.FIREBASE_STORAGE_MK).child(mk.getImageUri());
            Glide.with(mContext).using(new FirebaseImageLoader()).load(ref).into(holder.imageView);
        }
        // button share
        holder.buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, mk.getTitle1());
                sendIntent.putExtra(Intent.EXTRA_TEXT, mk.getTitle1()+ "\n"+mk.getTitle2()+ "\n\n"+mk.getDescription());
                sendIntent.setType("text/plain");
                mContext.startActivity(sendIntent);
            }
        });

        // expand to show description
        holder.buttonExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.expandPanel.getVisibility() == View.VISIBLE) {
                    holder.buttonExpand.setImageResource(R.drawable.ic_action_expand_more);
                    holder.expandPanel.setVisibility(View.GONE);
                } else {
                    holder.buttonExpand.setImageResource(R.drawable.ic_action_expand_less);
                    holder.expandPanel.setVisibility(View.VISIBLE);
                }
            }
        });
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MkItemActivity.class);
                intent.putExtra(Constants.EXTRA_MK_ID, mk.getKey());
                mContext.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return mkList.size();
    }
}