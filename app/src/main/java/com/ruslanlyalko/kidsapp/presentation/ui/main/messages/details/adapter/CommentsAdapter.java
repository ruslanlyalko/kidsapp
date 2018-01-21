package com.ruslanlyalko.kidsapp.presentation.ui.main.messages.details.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.data.models.MessageComment;
import com.ruslanlyalko.kidsapp.presentation.widget.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyViewHolder> {

    private static final int TYPE_MY = 0;
    private static final int TYPE_ANOTHER = 1;
    private List<MessageComment> mDataSource = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    public CommentsAdapter(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public CommentsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(viewType == TYPE_MY ? R.layout.card_commet_my : R.layout.card_commet, parent, false);
        return new CommentsAdapter.MyViewHolder(itemView, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(final CommentsAdapter.MyViewHolder holder, final int position) {
        final MessageComment user = mDataSource.get(position);
        holder.bindData(user);
    }

    @Override
    public int getItemViewType(final int position) {
        return mDataSource.get(position).getUserId().equals(FirebaseAuth.getInstance().getUid()) ? TYPE_MY : TYPE_ANOTHER;
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }

    public MessageComment getItemAtPostion(final int position) {
        return mDataSource.get(position);
    }

    public void clearAll() {
        mDataSource.clear();
        notifyDataSetChanged();
    }

    public void add(final MessageComment messageComment) {
        mDataSource.add(messageComment);
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_user_name) TextView mTextUserName;
        @BindView(R.id.text_comment) TextView mTextComment;
        private MessageComment mMessageComment;
        private OnItemClickListener mOnItemClickListener;

        public MyViewHolder(View view, final OnItemClickListener onItemClickListener) {
            super(view);
            mOnItemClickListener = onItemClickListener;
            ButterKnife.bind(this, view);
        }

        void bindData(final MessageComment messageComment) {
            mMessageComment = messageComment;
            mTextUserName.setText(messageComment.getUserName());
            mTextComment.setText(messageComment.getMessage());
        }

        @OnClick(R.id.linear_root)
        void onItemCLick() {
            if (mOnItemClickListener != null)
                mOnItemClickListener.onItemClicked(getAdapterPosition());
        }
    }
}