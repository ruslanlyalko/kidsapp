package com.ruslanlyalko.kidsapp.presentation.ui.main.clients.birthdays;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Birthday;
import com.ruslanlyalko.kidsapp.presentation.ui.main.clients.birthdays.adapter.BirthdaysAdapter;
import com.ruslanlyalko.kidsapp.presentation.ui.main.clients.birthdays.adapter.OnBirthdaysClickListener;
import com.ruslanlyalko.kidsapp.presentation.ui.main.clients.birthdays.edit.BirthdaysEditActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.main.clients.contacts.details.ContactDetailsActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Ruslan Lyalko
 * on 29.01.2018.
 */
public class BirthdaysFragment extends Fragment implements OnBirthdaysClickListener {

    @BindView(R.id.text_filter_date1) TextView mTextFilterDate1;
    @BindView(R.id.text_filter_date2) TextView mTextFilterDate2;
    @BindView(R.id.list_birthdays) RecyclerView mListBirthdays;

    BirthdaysAdapter mBirthdaysAdapter = new BirthdaysAdapter(this);

    public BirthdaysFragment() {
    }

    public static BirthdaysFragment newInstance() {
        BirthdaysFragment fragment = new BirthdaysFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scheduled_bd, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        setupRecycler();
        loadBirthdays();
    }

    private void setupRecycler() {
        mListBirthdays.setLayoutManager(new LinearLayoutManager(getContext()));
        mListBirthdays.setAdapter(mBirthdaysAdapter);
    }

    private void loadBirthdays() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DefaultConfigurations.DB_BIRTHDAYS);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                List<Birthday> birthdays = new ArrayList<>();
                for (DataSnapshot birthdaySS : dataSnapshot.getChildren()) {
                    Birthday birthday = birthdaySS.getValue(Birthday.class);
                    if (birthday != null) {
                        birthdays.add(birthday);
                    }
                }
                mBirthdaysAdapter.setData(birthdays);
                //onFilterTextChanged();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onEditClicked(final int position) {
        startActivity(BirthdaysEditActivity.getLaunchIntent(getContext(), mBirthdaysAdapter.getItem(position)));
    }

    @Override
    public void onItemClicked(final int position) {
        startActivity(ContactDetailsActivity.getLaunchIntent(getContext(), mBirthdaysAdapter.getItem(position).getContactKey()));
    }
}
