package com.ruslanlyalko.kidsapp.presentation.ui.main.expenses.adapter;

import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Constants;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.data.FirebaseUtils;
import com.ruslanlyalko.kidsapp.data.models.Expense;
import com.ruslanlyalko.kidsapp.presentation.widget.SwipeLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.MyViewHolder> {

    private OnExpenseClickListener mOnExpenseClickListener;
    private List<Expense> mExpenseList;
    private FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

    public ExpensesAdapter(OnExpenseClickListener onExpenseClickListener, List<Expense> reportList) {
        this.mOnExpenseClickListener = onExpenseClickListener;
        this.mExpenseList = reportList;
    }

    @Override
    public ExpensesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_expense, parent, false);
        return new ExpensesAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ExpensesAdapter.MyViewHolder holder, final int position) {
        final Expense expense = mExpenseList.get(position);
        holder.bindData(expense);
    }

    @Override
    public int getItemCount() {
        return mExpenseList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private final Resources mResources;

        @BindView(R.id.text_title1) TextView mTextView;
        @BindView(R.id.text_title2) TextView mTextTitle2;
        @BindView(R.id.text_total) TextView mTextPrice;
        @BindView(R.id.text_date) TextView mTextDate;
        @BindView(R.id.button_comment) ImageButton mButtonDelete;
        @BindView(R.id.linear_user) LinearLayout mUserLayout;
        @BindView(R.id.swipe_menu) LinearLayout mMenuLayout;
        @BindView(R.id.swipe_layout) SwipeLayout mSwipeLayout;
        @BindView(R.id.image_view) ImageView mLogoImage;

        MyViewHolder(View view) {
            super(view);
            mResources = view.getResources();
            ButterKnife.bind(this, view);
        }

        void bindData(final Expense expense) {
            boolean isCurrentUserCost = expense.getUserId().endsWith(mCurrentUser.getUid());
            mTextView.setText(expense.getTitle1());
            mTextTitle2.setText(isCurrentUserCost ? expense.getTitle2() : expense.getTitle2() + "  (" + expense.getUserName() + ")");
            mTextPrice.setText(mResources.getString(R.string.hrn, expense.getPrice() + ""));
            String date = expense.getDate().substring(0, expense.getDate().length() - 5) + " о " + expense.getTime();
            mTextDate.setText(date);
            mLogoImage.setImageResource(expense.getUri() != null && !expense.getUri().isEmpty() ?
                    R.drawable.ic_image_light : R.drawable.ic_action_cost);
            int diff = DateUtils.getDifference(expense.getDate(), expense.getTime());
            boolean justAdded = (diff <= Constants.COST_EDIT_MIN);
            // Avoid delete
            if (!FirebaseUtils.isAdmin() && justAdded) {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mSwipeLayout.close();
                        mSwipeLayout.setRightSwipeEnabled(false);
                        mMenuLayout.setVisibility(View.GONE);
                    }
                    // start this code after 5* minutes
                }, (Constants.COST_EDIT_MIN - diff + 1) * 60 * 1000);
            }
            if (FirebaseUtils.isAdmin() || justAdded) {
                mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu);
                mSwipeLayout.setRightSwipeEnabled(true);
                mSwipeLayout.setBottomSwipeEnabled(false);
            } else {
                mMenuLayout.setVisibility(View.GONE);
            }
        }

        @OnClick(R.id.linear_user)
        void onPhotoPreviewClicked() {
            if (mOnExpenseClickListener != null) {
                Expense expense = mExpenseList.get(getAdapterPosition());
                if (expense.getUri() != null && !expense.getUri().isEmpty()) {
                    mOnExpenseClickListener.onPhotoPreviewClicked(expense);
                }
            }
        }

        @OnClick(R.id.button_comment)
        void onRemoveClicked() {
            if (mOnExpenseClickListener != null) {
                mOnExpenseClickListener.onRemoveClicked(mExpenseList.get(getAdapterPosition()));
                mSwipeLayout.close();
            }
        }
    }
}