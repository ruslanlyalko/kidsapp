package com.ruslanlyalko.kidsapp.presentation.ui.main.messages.details.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.data.FirebaseUtils;
import com.ruslanlyalko.kidsapp.data.models.MessageComment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyViewHolder> {

    private static final int TYPE_MY = 0;
    private static final int TYPE_ANOTHER = 1;
    private List<MessageComment> mDataSource = new ArrayList<>();
    private OnCommentClickListener mOnItemClickListener;

    public CommentsAdapter(OnCommentClickListener onItemClickListener) {
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
        if (mDataSource.contains(messageComment)) return;
        mDataSource.add(messageComment);
        notifyItemInserted(mDataSource.size());
    }

    public void addAll(final List<MessageComment> messageComments) {
        mDataSource.addAll(messageComments);
        notifyDataSetChanged();
    }

    public void update(final MessageComment messageComment) {
        for (int i = 0; i < mDataSource.size(); i++) {
            MessageComment current = mDataSource.get(i);
            if (messageComment.getKey().equals(current.getKey())) {
                mDataSource.set(i, messageComment);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_user_name) TextView mTextUserName;
        @BindView(R.id.text_comment) TextView mTextComment;
        @BindView(R.id.text_comment_time) TextView mTextCommentTime;
        private MessageComment mMessageComment;
        private OnCommentClickListener mOnCommentClickListener;

        public MyViewHolder(View view, final OnCommentClickListener onCommentClickListener) {
            super(view);
            mOnCommentClickListener = onCommentClickListener;
            ButterKnife.bind(this, view);
        }

        void bindData(final MessageComment messageComment) {
            mMessageComment = messageComment;
            mTextUserName.setText(messageComment.getUserName());
            mTextComment.setText(messageComment.getRemoved() ? "Повідомлення видалено" : messageComment.getMessage());
            mTextComment.setTextColor(ContextCompat.getColor(mTextComment.getContext(),
                    messageComment.getRemoved() ? R.color.colorComment : R.color.colorBlack));
            mTextCommentTime.setText(DateUtils.toString(messageComment.getDate(), "HH:mm"));
        }

        @OnClick(R.id.linear_root)
        void onItemCLick() {
            if (mOnCommentClickListener != null)
                mOnCommentClickListener.onItemClicked(getAdapterPosition());
        }

        @OnLongClick(R.id.linear_root)
        boolean onItemLongCLick() {
            mMessageComment = mDataSource.get(getAdapterPosition());
            if (mMessageComment.getRemoved()) {
                if (FirebaseUtils.isAdmin())
                    Toast.makeText(mTextComment.getContext(), mMessageComment.getMessage(), Toast.LENGTH_SHORT).show();
                return true;
            }
            if (!FirebaseAuth.getInstance().getUid().equals(mMessageComment.getUserId()))
                return false;
            if (mOnCommentClickListener == null) return false;
            mOnCommentClickListener.onItemLongClicked(getAdapterPosition());
            return true;
        }
    }
}