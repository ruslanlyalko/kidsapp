package com.example.android.kidsapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.kidsapp.CalendarActivity;
import com.example.android.kidsapp.MkItemActivity;
import com.example.android.kidsapp.R;
import com.example.android.kidsapp.ReportActivity;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.MyViewHolder> {
    private Context mContext;
    private List<Report> reportList;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textUserName, textTotal, textBdayTotal, textRoomTotal, textMkTotal;
        public SwipeLayout swipeLayout;
        public ProgressBar progressBar;
        public ImageButton buttonMk, buttonEdit, buttonDelete;

        public MyViewHolder(View view) {
            super(view);

            textUserName = (TextView) view.findViewById(R.id.text_user_name);
            textTotal = (TextView) view.findViewById(R.id.text_total);
            textBdayTotal = (TextView) view.findViewById(R.id.text_bday_total);
            textRoomTotal = (TextView) view.findViewById(R.id.text_room_total);
            textMkTotal = (TextView) view.findViewById(R.id.text_mk_total);
            swipeLayout = (SwipeLayout) view.findViewById(R.id.swipe_layout);
            progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            buttonMk = (ImageButton) view.findViewById(R.id.button_user);
            buttonEdit = (ImageButton) view.findViewById(R.id.button_edit);
            buttonDelete = (ImageButton) view.findViewById(R.id.button_delete);

        }
    }

    public ReportsAdapter(Context mContext, List<Report> reportList) {
        this.mContext = mContext;
        this.reportList = reportList;

    }

    @Override
    public ReportsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_report, parent, false);

        return new ReportsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ReportsAdapter.MyViewHolder holder, final int position) {
        final Report report = reportList.get(position);

        String mkName = "";
        if (report.getTotalMk() > 0)
            mkName = " (МК)";
        if (report.getMkName() != null && !report.getMkName().isEmpty())
            mkName = " (" + report.getMkName() + ")";

        holder.textUserName.setText(report.userName + mkName);
        holder.textTotal.setText(report.total + " ГРН");
        holder.textRoomTotal.setText(report.totalRoom + " грн");
        holder.textBdayTotal.setText(report.totalBday + " грн");
        holder.textMkTotal.setText(report.totalMk + " грн");

        holder.progressBar.setMax(report.total);
        holder.progressBar.setProgress(report.totalRoom);

        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, R.id.swipe_menu);
        holder.swipeLayout.setRightSwipeEnabled(true);
        holder.swipeLayout.setBottomSwipeEnabled(false);

        // Open MK Item
        holder.buttonMk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (report.getMkRef() != null && !report.getMkRef().isEmpty()) {
                    Intent intent = new Intent(mContext, MkItemActivity.class);
                    intent.putExtra(Constants.EXTRA_ITEM_ID, report.getMkRef());

                    mContext.startActivity(intent);
                }else{
                    Toast.makeText(mContext, R.string.toast_mk_not_set, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //open Report for editing
        holder.buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utils.isAdmin() || Utils.todayOrFuture(report.getDate())) {
                    Intent intent = new Intent(mContext, ReportActivity.class);
                    intent.putExtra(Constants.EXTRA_DATE, report.date);
                    intent.putExtra(Constants.EXTRA_UID, report.userId);
                    intent.putExtra(Constants.EXTRA_USER_NAME, report.userName);

                    ((CalendarActivity)mContext).startActivityForResult(intent,0);
                } else {
                    Toast.makeText(mContext, R.string.toast_edit_impossible, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // show confirm dialog before delete
        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utils.isAdmin() || Utils.todayOrFuture(report.getDate())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.dialog_delete_title)
                            .setMessage(R.string.dialog_delete_message)
                            .setPositiveButton("Видалити", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeReport(report, position);
                                }

                            })
                            .setNegativeButton("Повернутись", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();
                }else{
                    Toast.makeText(mContext, R.string.toast_delete_impossible, Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    private void removeReport(Report report, int position) {
        reportList.remove(position);
        notifyDataSetChanged();

        // Delete item from DB

        mDatabase.getReference(Constants.FIREBASE_REF_REPORTS)
                .child(getYearFromStr(report.date)).child(getMonthFromStr(report.date)).child(getDayFromStr(report.date))
                .child(report.userId).removeValue();

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
        return reportList.size();
    }
}