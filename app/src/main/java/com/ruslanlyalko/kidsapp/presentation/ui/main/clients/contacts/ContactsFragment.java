package com.ruslanlyalko.kidsapp.presentation.ui.main.clients.contacts;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Contact;
import com.ruslanlyalko.kidsapp.presentation.ui.main.clients.contacts.adapter.ContactsAdapter;
import com.ruslanlyalko.kidsapp.presentation.ui.main.clients.contacts.adapter.OnContactClickListener;
import com.ruslanlyalko.kidsapp.presentation.ui.main.clients.contacts.details.ContactDetailsActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

/**
 * Created by Ruslan Lyalko
 * on 29.01.2018.
 */
public class ContactsFragment extends Fragment implements OnContactClickListener {

    @BindView(R.id.list_contacts) RecyclerView mListContacts;
    @BindView(R.id.edit_filter_name) EditText mEditFilterName;
    @BindView(R.id.edit_filter_phone) EditText mEditFilterPhone;
    private ContactsAdapter mContactsAdapter = new ContactsAdapter(this);

    public ContactsFragment() {
    }

    public static ContactsFragment newInstance() {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setupRecycler();
        loadContacts();
    }

    private void setupRecycler() {
        mListContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        mListContacts.setAdapter(mContactsAdapter);
    }

    private void loadContacts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DefaultConfigurations.DB_CONTACTS);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                List<Contact> contacts = new ArrayList<>();
                for (DataSnapshot clientSS : dataSnapshot.getChildren()) {
                    Contact contact = clientSS.getValue(Contact.class);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                }
                mContactsAdapter.setData(contacts);
                onFilterTextChanged();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    @OnTextChanged({R.id.edit_filter_name, R.id.edit_filter_phone})
    void onFilterTextChanged() {
        mContactsAdapter.getFilter().filter(mEditFilterName.getText().toString().trim() + "/" + mEditFilterPhone.getText().toString().trim());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getView() != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClicked(final int position) {
        startActivity(ContactDetailsActivity.getLaunchIntent(getContext(), mContactsAdapter.getItem(position)));
    }
}
