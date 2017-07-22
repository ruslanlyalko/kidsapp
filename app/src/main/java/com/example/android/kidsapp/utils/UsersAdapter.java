package com.example.android.kidsapp.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.kidsapp.R;
import com.example.android.kidsapp.UserActivity;
import com.google.firebase.database.FirebaseDatabase;

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

            textUserName = (TextView) view.findViewById(R.id.text_user_name);
            textPositionTitle = (TextView) view.findViewById(R.id.text_position_title);
            linearUser = (LinearLayout) view.findViewById(R.id.linear_user);

        }
    }

    public UsersAdapter(Context mContext, List<User> users) {
        this.mContext = mContext;
        this.users = users;

    }

    @Override
    public UsersAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_user, parent, false);

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
                Intent intent = new Intent(mContext, UserActivity.class);
                intent.putExtra(Constants.EXTRA_UID, user.getUserId());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}