package com.example.android.kidsapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.kidsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CostsAdapter extends RecyclerView.Adapter<CostsAdapter.MyViewHolder> {
    private Context mContext;
    private List<Cost> costList;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private boolean isAdmin = false;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textTitle1, textTitle2, textPrice, textDate;
        public ImageButton buttonDelete;
        public SwipeLayout swipeLayout;


        public MyViewHolder(View view) {
            super(view);

            textTitle1 = (TextView) view.findViewById(R.id.text_title1);
            textTitle2 = (TextView) view.findViewById(R.id.text_title2);
            textDate = (TextView) view.findViewById(R.id.text_date);
            textPrice = (TextView) view.findViewById(R.id.text_total);

            swipeLayout = (SwipeLayout) view.findViewById(R.id.swipe_layout);

            buttonDelete = (ImageButton) view.findViewById(R.id.button_delete);

        }
    }

    public CostsAdapter(Context mContext, List<Cost> reportList, boolean isAdmin) {
        this.mContext = mContext;
        this.costList = reportList;
        this.isAdmin = isAdmin;
    }

    @Override
    public CostsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cost_card, parent, false);

        return new CostsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CostsAdapter.MyViewHolder holder, final int position) {
        final Cost cost = costList.get(position);


        holder.textPrice.setText(cost.getPrice() + " грн");
        holder.textTitle1.setText(cost.getTitle1());
        holder.textTitle2.setText(cost.getTitle2());
        holder.textDate.setText(cost.getDate());

        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu);
        holder.swipeLayout.setRightSwipeEnabled(true);
        holder.swipeLayout.setBottomSwipeEnabled(false);


        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdmin || cost.getUserId().equals(mAuth.getCurrentUser().getUid())) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.dialog_cost_delete_title)
                            .setMessage(R.string.dialog_cost_delete_message)
                            .setPositiveButton("Видалити", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeCost(cost, position);
                                }

                            })
                            .setNegativeButton("Повернутись", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();

                } else {
                    Snackbar.make(holder.buttonDelete, mContext.getString(R.string.cant_delete), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void removeCost(Cost cost, int position) {
        costList.remove(position);


        mDatabase.getReference(Constants.FIREBASE_REF_COSTS)
                .child(getYearFromStr(cost.date)).child(getMonthFromStr(cost.date))
                .child(cost.getKey()).removeValue();

        notifyDataSetChanged();

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