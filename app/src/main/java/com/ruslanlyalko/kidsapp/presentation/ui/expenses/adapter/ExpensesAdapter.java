package com.ruslanlyalko.kidsapp.presentation.ui.expenses.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Constants;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.Utils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Expense;
import com.ruslanlyalko.kidsapp.presentation.widget.PhotoPreviewActivity;
import com.ruslanlyalko.kidsapp.presentation.widget.SwipeLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.MyViewHolder> {

    private Context mContext;
    private List<Expense> mExpenseList;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView textTitle1, textTitle2, textPrice, textDate;
        public ImageButton buttonDelete;
        public LinearLayout linearUser, linearMenu;
        public SwipeLayout swipeLayout;
        public ImageView imageView;

        public MyViewHolder(View view) {
            super(view);
            textTitle1 = view.findViewById(R.id.text_title1);
            textTitle2 = view.findViewById(R.id.text_title2);
            textDate = view.findViewById(R.id.text_date);
            textPrice = view.findViewById(R.id.text_total);
            swipeLayout = view.findViewById(R.id.swipe_layout);
            buttonDelete = view.findViewById(R.id.button_comment);
            linearUser = view.findViewById(R.id.linear_user);
            linearMenu = view.findViewById(R.id.swipe_menu);
            imageView = view.findViewById(R.id.image_view);
        }
    }

    public ExpensesAdapter(Context mContext, List<Expense> reportList) {
        this.mContext = mContext;
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
        boolean isCurrentUserCost = expense.getUserId().endsWith(mAuth.getCurrentUser().getUid());
        holder.textTitle1.setText(expense.getTitle1());
        holder.textTitle2.setText(isCurrentUserCost ? expense.getTitle2() : expense.getTitle2() + "  (" + expense.getUserName() + ")");
        holder.textPrice.setText(expense.getPrice() + " грн");
        holder.textDate.setText(expense.getDate().substring(0, expense.getDate().length() - 5) + " о " + expense.getTime());
        // click on item to open photo
        if (expense.getUri() != null && !expense.getUri().isEmpty()) {
            holder.linearUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // open new Activity and load image from storage
                    String uri = expense.getUri();
                    Intent intent = new Intent(mContext, PhotoPreviewActivity.class);
                    intent.putExtra(Keys.Extras.EXTRA_URI, uri);
                    intent.putExtra(Keys.Extras.EXTRA_USER_NAME, expense.getUserName());
                    mContext.startActivity(intent);
                }
            });
            // change icon
            holder.imageView.setImageResource(R.drawable.ic_image_light);
        }
        //check if just added
        int diff = getDifference(expense.getDate(), expense.getTime());
        boolean justAdded = (diff <= Constants.COST_EDIT_MIN);
        // Avoid delete
        if (!Utils.isAdmin() && justAdded) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    holder.swipeLayout.close();
                    holder.swipeLayout.setRightSwipeEnabled(false);
                    holder.linearMenu.setVisibility(View.GONE);
                }
                // start this code after 5* minutes
            }, (Constants.COST_EDIT_MIN - diff + 1) * 60 * 1000);
        }
        if (Utils.isAdmin() || justAdded) {
            holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu);
            holder.swipeLayout.setRightSwipeEnabled(true);
            holder.swipeLayout.setBottomSwipeEnabled(false);
            holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.isAdmin() || expense.getUserId().equals(mAuth.getCurrentUser().getUid())) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(R.string.dialog_cost_delete_title)
                                .setMessage(R.string.dialog_cost_delete_message)
                                .setPositiveButton("Видалити", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        removeCost(expense, holder.buttonDelete);
                                        holder.swipeLayout.close();
                                    }
                                })
                                .setNegativeButton("Повернутись", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .show();
                    }
                }
            });
        } else {
            // not admin
            holder.linearMenu.setVisibility(View.GONE);
        }
    }

    private int getDifference(String date, String time) {
        if (time == null || time.isEmpty()) return 10;
        SimpleDateFormat format = new SimpleDateFormat("d-M-yyyy HH:mm");
        Date d1 = new Date();
        Date d2 = null;
        try {
            d2 = format.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Get msec from each, and subtract.
        long diff = d1.getTime() - d2.getTime();
        int diffMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff);
        ;
        return diffMinutes;
    }

    private void removeCost(Expense expense, final View view) {
        // mExpenseList.remove(position);
        mDatabase.getReference(DefaultConfigurations.DB_COSTS)
                .child(getYearFromStr(expense.date)).child(getMonthFromStr(expense.date))
                .child(expense.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(view, mContext.getString(R.string.snack_deleted), Snackbar.LENGTH_LONG).show();
            }
        });
        //notifyDataSetChanged();
    }

    private String getDayFromStr(String date) {
        int first = date.indexOf('-');
        return date.substring(0, first);
    }

    private String getMonthFromStr(String date) {
        int first = date.indexOf('-');
        int last = date.lastIndexOf('-');
        return date.substring(first + 1, last);
    }

    private String getYearFromStr(String date) {
        int last = date.lastIndexOf('-');
        return date.substring(last + 1);
    }

    @Override
    public int getItemCount() {
        return mExpenseList.size();
    }
}