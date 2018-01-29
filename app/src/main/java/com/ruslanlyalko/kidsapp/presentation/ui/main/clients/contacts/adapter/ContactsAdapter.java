package com.ruslanlyalko.kidsapp.presentation.ui.main.clients.contacts.adapter;

import android.support.v7.widget.CardView;
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
        @BindView(R.id.text_phone1) TextView mTextPhone1;
        @BindView(R.id.text_phone2) TextView mTextPhone2;
        @BindView(R.id.layout_phones) LinearLayout mLayoutPhones;
        @BindView(R.id.text_child_name1) TextView mTextChildName1;
        @BindView(R.id.text_child_bd1) TextView mTextChildBd1;
        @BindView(R.id.layout_child1) LinearLayout mLayoutChild1;
        @BindView(R.id.text_child_name2) TextView mTextChildName2;
        @BindView(R.id.text_child_bd2) TextView mTextChildBd2;
        @BindView(R.id.layout_child2) LinearLayout mLayoutChild2;
        @BindView(R.id.text_child_name3) TextView mTextChildName3;
        @BindView(R.id.text_child_bd3) TextView mTextChildBd3;
        @BindView(R.id.layout_child3) LinearLayout mLayoutChild3;
        @BindView(R.id.card_root) CardView mCardRoot;
        @BindView(R.id.text_description) TextView mTextDescription;

        MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bindData(final Contact contact) {
            mTextName.setText(contact.getName());
            mTextPhone1.setText(contact.getPhone());
            mTextPhone2.setText(contact.getPhone2());
            mTextPhone2.setVisibility(contact.getPhone2() != null & !contact.getPhone2().isEmpty() ? View.VISIBLE : View.GONE);
            if (contact.getChildName1() != null && !contact.getChildName1().isEmpty()) {
                mTextChildName1.setText(contact.getChildName1());
                mTextChildBd1.setText(DateUtils.toString(contact.getChildBd1(), "dd.MM.yyyy"));
                mLayoutChild1.setVisibility(View.VISIBLE);
            } else {
                mLayoutChild1.setVisibility(View.GONE);
            }
            if (contact.getChildName2() != null && !contact.getChildName2().isEmpty()) {
                mTextChildName2.setText(contact.getChildName2());
                mTextChildBd2.setText(DateUtils.toString(contact.getChildBd2(), "dd.MM.yyyy"));
                mLayoutChild2.setVisibility(View.VISIBLE);
            } else {
                mLayoutChild2.setVisibility(View.GONE);
            }
            if (contact.getChildName3() != null && !contact.getChildName3().isEmpty()) {
                mTextChildName3.setText(contact.getChildName3());
                mTextChildBd3.setText(DateUtils.toString(contact.getChildBd3(), "dd.MM.yyyy"));
                mLayoutChild3.setVisibility(View.VISIBLE);
            } else {
                mLayoutChild3.setVisibility(View.GONE);
            }
            mTextDescription.setText(contact.getDescription());
            mTextDescription.setVisibility(contact.getDescription() != null & !contact.getDescription().isEmpty() ? View.VISIBLE : View.GONE);
        }

        @OnClick(R.id.card_root)
        void onItemClicked() {
            if (mOnContactClickListener != null)
                mOnContactClickListener.onItemClicked(getAdapterPosition());
        }

        @OnClick(R.id.button_edit)
        void onEditClicked() {
            if (mOnContactClickListener != null)
                mOnContactClickListener.onEditClicked(getAdapterPosition());
        }

        @OnClick(R.id.text_phone1)
        void onPhoneClicked() {
            if (mOnContactClickListener != null)
                mOnContactClickListener.onPhoneClicked(getAdapterPosition());
        }

        @OnClick(R.id.text_phone2)
        void onPhone2Clicked() {
            if (mOnContactClickListener != null)
                mOnContactClickListener.onPhone2Clicked(getAdapterPosition());
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