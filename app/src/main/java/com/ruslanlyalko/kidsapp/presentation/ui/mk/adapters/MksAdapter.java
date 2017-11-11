package com.ruslanlyalko.kidsapp.presentation.ui.mk.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Mk;
import com.ruslanlyalko.kidsapp.presentation.ui.mk.MkDetailsActivity;

import java.util.List;

public class MksAdapter extends RecyclerView.Adapter<MksAdapter.MyViewHolder> {

    private Context mContext;
    private List<Mk> mkList;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView textTitle1, textTitle2, textDescription;
        public ImageView imageView;
        public ImageButton buttonExpand;
        public Button buttonShare, buttonLink;
        public LinearLayout expandPanel;

        public MyViewHolder(View view) {
            super(view);
            buttonShare = (Button) view.findViewById(R.id.button_share);
            buttonLink = (Button) view.findViewById(R.id.button_link);
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
            StorageReference ref = storage.getReference(DefaultConfigurations.STORAGE_MK).child(mk.getImageUri());
            Glide.with(mContext).using(new FirebaseImageLoader()).load(ref).into(holder.imageView);
        }
        // button share
        holder.buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, mk.getTitle1());
                sendIntent.putExtra(Intent.EXTRA_TEXT, mk.getTitle1() + "\n" + mk.getTitle2() + "\n\n" + mk.getDescription());
                sendIntent.setType("text/plain");
                mContext.startActivity(sendIntent);
            }
        });
        holder.buttonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mk.getLink() != null && !mk.getLink().isEmpty()) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    builder.setToolbarColor(ResourcesCompat.getColor(mContext.getResources(), R.color.colorPrimary, null));
                    customTabsIntent.launchUrl(mContext, Uri.parse(mk.getLink()));
                }
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
                Intent intent = new Intent(mContext, MkDetailsActivity.class);
                intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, mk.getKey());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mkList.size();
    }
}