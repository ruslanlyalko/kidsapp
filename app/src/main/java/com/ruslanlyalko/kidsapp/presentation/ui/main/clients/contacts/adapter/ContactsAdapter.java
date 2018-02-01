package com.ruslanlyalko.kidsapp.presentation.ui.main.clients.contacts.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.data.models.Contact;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder>
        implements Filterable {

    private final OnContactClickListener mOnContactClickListener;
    private List<Contact> mDataSource = new ArrayList<>();
    private List<Contact> mDataSourceFiltered = new ArrayList<>();
    private MyFilter mFilter;

    public ContactsAdapter(final OnContactClickListener onContactClickListener) {
        mOnContactClickListener = onContactClickListener;
    }

    public void clearAll() {
        mDataSource.clear();
        mDataSourceFiltered.clear();
        notifyDataSetChanged();
    }

    public void add(final Contact contactComment) {
        if (mDataSource.contains(contactComment)) return;
        mDataSource.add(contactComment);
        mDataSourceFiltered.add(contactComment);
        notifyItemInserted(mDataSource.size());
    }

    public void setData(final List<Contact> contactComments) {
        mDataSource.clear();
        mDataSource.addAll(contactComments);
        mDataSourceFiltered.clear();
        mDataSourceFiltered.addAll(contactComments);
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_contact, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Contact contact = mDataSourceFiltered.get(position);
        holder.bindData(contact);
    }

    @Override
    public int getItemCount() {
        return mDataSourceFiltered.size();
    }

    public Contact getItem(final int position) {
        return mDataSourceFiltered.get(position);
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null)
            mFilter = new MyFilter();
        return mFilter;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_name) TextView mTextName;
        @BindView(R.id.text_phones) TextView mTextPhones;
        @BindView(R.id.text_kids) TextView mTextKids;
        @BindView(R.id.layout_root) LinearLayout mLayoutRoot;

        MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bindData(final Contact contact) {
            mTextName.setText(contact.getName());
            String phones = contact.getPhone();
            if (contact.getPhone2() != null & !contact.getPhone2().isEmpty())
                phones += ", " + contact.getPhone2();
            mTextPhones.setText(phones);
            String kids = "";
            if (contact.getChildName1() != null && !contact.getChildName1().isEmpty()) {
                kids += contact.getChildName1() + DateUtils.toString(contact.getChildBd1(), " dd.MM ") + DateUtils.getChildYears(contact.getChildBd1());
            }
            if (contact.getChildName2() != null && !contact.getChildName2().isEmpty()) {
                kids += contact.getChildName2() + DateUtils.toString(contact.getChildBd2(), " dd.MM ") + DateUtils.getChildYears(contact.getChildBd2());
            }
            if (contact.getChildName3() != null && !contact.getChildName3().isEmpty()) {
                kids += contact.getChildName3() + DateUtils.toString(contact.getChildBd3(), " dd.MM") + DateUtils.getChildYears(contact.getChildBd3());
            }
            mTextKids.setText(kids);
        }

        @OnClick(R.id.layout_root)
        void onItemClicked() {
            if (mOnContactClickListener != null)
                mOnContactClickListener.onItemClicked(getAdapterPosition());
        }
    }

    class MyFilter extends Filter {

        @Override
        protected FilterResults performFiltering(final CharSequence charSequence) {
            FilterResults filterResults = new FilterResults();
            ArrayList<Contact> tempList = new ArrayList<>();
            for (int i = 0; i < mDataSource.size(); i++) {
                Contact contact = mDataSource.get(i);
                if (isMatchFilter(charSequence, contact)) {
                    tempList.add(contact);
                }
            }
            filterResults.count = tempList.size();
            filterResults.values = tempList;
            return filterResults;
        }

        private boolean isMatchFilter(final CharSequence charSequence, final Contact contact) {
            if (charSequence.toString().equals("/")) return true;
            String[] filter = charSequence.toString().split("/", 2);
            String name = filter[0];
            String phone = "";
            if (filter.length > 1)
                phone = filter[1];
            boolean isNameGood = true;
            boolean isPhoneGood = true;
            if (!name.isEmpty() && !contact.getName().toLowerCase().contains(name.toLowerCase())) {
                isNameGood = false;
            }
            if (!phone.isEmpty() && !(contact.getPhone().toLowerCase().contains(phone.toLowerCase()) ||
                    (contact.getPhone2() == null || contact.getPhone2().toLowerCase().contains(phone.toLowerCase())))) {
                isPhoneGood = false;
            }
            return isNameGood && isPhoneGood;
        }

        @Override
        protected void publishResults(final CharSequence charSequence, final FilterResults filterResults) {
            mDataSourceFiltered = (ArrayList<Contact>) filterResults.values;
            notifyDataSetChanged();
            notifyDataSetChanged();
        }
    }
}