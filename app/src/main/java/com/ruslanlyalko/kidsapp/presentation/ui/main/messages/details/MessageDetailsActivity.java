package com.ruslanlyalko.kidsapp.presentation.ui.main.messages.details;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Constants;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.FirebaseUtils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Message;
import com.ruslanlyalko.kidsapp.data.models.MessageComment;
import com.ruslanlyalko.kidsapp.data.models.MessageType;
import com.ruslanlyalko.kidsapp.presentation.ui.main.messages.MessageEditActivity;
import com.ruslanlyalko.kidsapp.presentation.ui.main.messages.details.adapter.CommentsAdapter;
import com.ruslanlyalko.kidsapp.presentation.widget.OnItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MessageDetailsActivity extends AppCompatActivity implements OnItemClickListener {

    @BindView(R.id.text_description) TextView textDescription;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.list_comments) RecyclerView mListComments;
    @BindView(R.id.card_comments_send) CardView mCardCommentsSend;
    @BindView(R.id.edit_comment) EditText mEditComment;
    @BindView(R.id.button_send) ImageButton mButtonSend;
    @BindView(R.id.scroll_view) ScrollView mScrollView;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private CommentsAdapter mCommentsAdapter = new CommentsAdapter(this);
    private String mMessageKey;
    private Message mMessage;

    public static Intent getLaunchIntent(final Context launchIntent, final String messageId) {
        Intent intent = new Intent(launchIntent, MessageDetailsActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_NOT_ID, messageId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);
        ButterKnife.bind(this);
        parseExtras();
        setupRecycler();
        FirebaseUtils.markNotificationsAsRead(mMessageKey);
        loadDetailsFromDB();
        loadCommentsFromDB();
    }

    private void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mMessageKey = bundle.getString(Keys.Extras.EXTRA_NOT_ID);
        }
    }

    private void setupRecycler() {
        mListComments.setLayoutManager(new LinearLayoutManager(this));
        mListComments.setAdapter(mCommentsAdapter);
    }

    private void loadDetailsFromDB() {
        if (mMessageKey == null || mMessageKey.isEmpty()) return;
        database.getReference(DefaultConfigurations.DB_MESSAGES)
                .child(mMessageKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mMessage = dataSnapshot.getValue(Message.class);
                        updateUI();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void loadCommentsFromDB() {
        if (mMessageKey == null || mMessageKey.isEmpty()) return;
        database.getReference(DefaultConfigurations.DB_MESSAGES_COMMENTS)
                .child(mMessageKey)
                .orderByChild("date/time")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean scroll = mCommentsAdapter.getItemCount() > 0;
                        mCommentsAdapter.clearAll();
                        for (DataSnapshot commentSS : dataSnapshot.getChildren()) {
                            MessageComment messageComment = commentSS.getValue(MessageComment.class);
                            if (messageComment != null) {
                                mCommentsAdapter.add(messageComment);
                            }
                        }
                        if (scroll)
                            mScrollView.fullScroll(View.FOCUS_DOWN);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void updateUI() {
        if (mMessage != null) {
            invalidateOptionsMenu();
            mCardCommentsSend.setVisibility(mMessage.getCommentsEnabled() ? View.VISIBLE : View.GONE);
            getSupportActionBar().setTitle(mMessage.getTitle1());
            textDescription.setText(mMessage.getDescription());
            fab.setVisibility((mMessage.getLink() != null && !mMessage.getLink().isEmpty())
                    ? View.VISIBLE : View.GONE);
        } else {
            getSupportActionBar().setTitle(R.string.title_activity_notification_item);
        }
    }

    @OnClick(R.id.fab)
    void onFabClicked() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        builder.setToolbarColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        customTabsIntent.launchUrl(MessageDetailsActivity.this, Uri.parse(mMessage.getLink()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_EDIT) {
            loadDetailsFromDB();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mMessage != null) {
            menu.findItem(R.id.action_delete).setVisible(FirebaseUtils.isAdmin()
                    || mMessage.getUserId().equals(mUser.getUid()));
            menu.findItem(R.id.action_edit).setVisible(FirebaseUtils.isAdmin()
                    || mMessage.getUserId().equals(mUser.getUid()));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_edit:
                if (FirebaseUtils.isAdmin()
                        || mMessage.getUserId().equals(mUser.getUid())) {
                    editMk();
                }
                break;
            case R.id.action_delete:
                if (FirebaseUtils.isAdmin()
                        || mMessage.getUserId().equals(mUser.getUid())) {
                    deleteMk();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editMk() {
        Intent intent = new Intent(MessageDetailsActivity.this, MessageEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, mMessageKey);
        startActivityForResult(intent, Constants.REQUEST_CODE_EDIT);
    }

    private void deleteMk() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MessageDetailsActivity.this);
        AlertDialog dialog = builder.setTitle(R.string.dialog_delete_notification_title)
                .setMessage(R.string.dialog_delete_notification_message)
                .setPositiveButton("Видалити", (dialog1, which) -> {
                    database.getReference(DefaultConfigurations.DB_MESSAGES)
                            .child(mMessage.getKey())
                            .removeValue();
                    FirebaseUtils.clearNotificationsForAllUsers(mMessage.getKey());
                    onBackPressed();
                })
                .setNegativeButton("Повернутись", null).create();
        dialog.show();
    }

    @Override
    public void onItemClicked(final int position) {
        Toast.makeText(this, DateUtils.toString(mCommentsAdapter.getItemAtPostion(position).getDate(),
                "HH:mm   d.MM EEEE ").toUpperCase(), Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button_send)
    public void onSendButtonClicked() {
        String comment = mEditComment.getText().toString().trim();
        mEditComment.setText("");
        if (comment.isEmpty()) return;
        if (mMessageKey.isEmpty()) return;
        DatabaseReference ref = database.getReference(DefaultConfigurations.DB_MESSAGES_COMMENTS)
                .child(mMessageKey)
                .push();
        ref.setValue(new MessageComment(ref.getKey(), comment, mUser));
        FirebaseUtils.updateNotificationsForAllUsers(mMessageKey, mMessage.getTitle1(), "Добавлено новий коментар", MessageType.COMMENTS);
    }
}
