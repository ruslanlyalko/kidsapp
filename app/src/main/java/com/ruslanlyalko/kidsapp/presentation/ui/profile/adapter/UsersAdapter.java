package com.ruslanlyalko.kidsapp.presentation.ui.profile.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.models.User;
import com.ruslanlyalko.kidsapp.presentation.ui.profile.ProfileActivity;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {

    private Context mContext;
    private List<User> users;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView textUserName, textPositionTitle;
        public LinearLayout linearUser;

        public MyViewHolder(View view) {
            super(view);
            textUserName = view.findViewById(R.id.text_user_name);
            textPositionTitle = view.findViewById(R.id.text_position_title);
            linearUser = view.findViewById(R.id.linear_user);
        }
    }

    public UsersAdapter(Context mContext, List<User> users) {
        this.mContext = mContext;
        this.users = users;
    }

    @Override
    public UsersAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_profile, parent, false);
        return new UsersAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final UsersAdapter.MyViewHolder holder, final int position) {
        final User user = users.get(position);
        holder.textUserName.setText(user.getUserName());
        holder.textPositionTitle.setText(user.getUserPositionTitle());
        holder.linearUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(Keys.Extras.EXTRA_UID, user.getUserId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}