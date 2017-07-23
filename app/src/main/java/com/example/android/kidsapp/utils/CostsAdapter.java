package com.example.android.kidsapp.utils;

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

import com.example.android.kidsapp.R;
import com.example.android.kidsapp.ShowImageActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CostsAdapter extends RecyclerView.Adapter<CostsAdapter.MyViewHolder> {
    private Context mContext;
    private List<Cost> costList;
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

            textTitle1 = (TextView) view.findViewById(R.id.text_title1);
            textTitle2 = (TextView) view.findViewById(R.id.text_title2);
            textDate = (TextView) view.findViewById(R.id.text_date);
            textPrice = (TextView) view.findViewById(R.id.text_total);

            swipeLayout = (SwipeLayout) view.findViewById(R.id.swipe_layout);

            buttonDelete = (ImageButton) view.findViewById(R.id.button_delete);
            linearUser = (LinearLayout) view.findViewById(R.id.linear_user);
            linearMenu = (LinearLayout) view.findViewById(R.id.swipe_menu);
            imageView = (ImageView) view.findViewById(R.id.image_view);

        }
    }

    public CostsAdapter(Context mContext, List<Cost> reportList) {
        this.mContext = mContext;
        this.costList = reportList;
    }

    @Override
    public CostsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_cost, parent, false);

        return new CostsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CostsAdapter.MyViewHolder holder, final int position) {
        final Cost cost = costList.get(position);

        boolean isCurrentUserCost = cost.getUserId().endsWith(mAuth.getCurrentUser().getUid());

        holder.textTitle1.setText(cost.getTitle1());
        holder.textTitle2.setText(isCurrentUserCost? cost.getTitle2():cost.getTitle2()+"  ("+cost.getUserName()+")");
        holder.textPrice.setText(cost.getPrice() + " грн");
        holder.textDate.setText(cost.getDate().substring(0,cost.getDate().length()-5)+" о "+cost.getTime());

        // click on item to open photo
        if (cost.getUri() != null && !cost.getUri().isEmpty()) {
            holder.linearUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // open new Activity and load image from storage
                    String uri = cost.getUri();

                    Intent intent = new Intent(mContext, ShowImageActivity.class);
                    intent.putExtra(Constants.EXTRA_URI, uri);
                    intent.putExtra(Constants.EXTRA_USER_NAME, cost.getUserName());
                    mContext.startActivity(intent);
                }
            });
            // change icon
            holder.imageView.setImageResource(R.drawable.ic_image_light);
        }

        //check if just added
        int diff = getDifference(cost.getDate(), cost.getTime());
        boolean justAdded = (diff <= Constants.COST_EDIT_MIN);

        // Avoid delete
        if (!Utils.isIsAdmin() && justAdded) {
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

        if (Utils.isIsAdmin() || justAdded) {
            holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu);
            holder.swipeLayout.setRightSwipeEnabled(true);
            holder.swipeLayout.setBottomSwipeEnabled(false);


            holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.isIsAdmin() || cost.getUserId().equals(mAuth.getCurrentUser().getUid())) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(R.string.dialog_cost_delete_title)
                                .setMessage(R.string.dialog_cost_delete_message)
                                .setPositiveButton("Видалити", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        removeCost(cost, holder.buttonDelete);
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
        }else
        {
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

    private void removeCost(Cost cost, final View view) {
        // costList.remove(position);


        mDatabase.getReference(Constants.FIREBASE_REF_COSTS)
                .child(getYearFromStr(cost.date)).child(getMonthFromStr(cost.date))
                .child(cost.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
        return costList.size();
    }
}