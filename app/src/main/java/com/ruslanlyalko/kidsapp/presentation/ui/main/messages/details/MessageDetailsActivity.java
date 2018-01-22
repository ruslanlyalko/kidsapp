package com.ruslanlyalko.kidsapp.presentation.ui.main.messages.details;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import com.ruslanlyalko.kidsapp.presentation.ui.main.messages.details.adapter.OnCommentClickListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MessageDetailsActivity extends AppCompatActivity implements OnCommentClickListener {

    @BindView(R.id.text_description) TextView textDescription;
    @BindView(R.id.list_comments) RecyclerView mListComments;
    @BindView(R.id.card_comments_send) CardView mCardCommentsSend;
    @BindView(R.id.edit_comment) EditText mEditComment;
    @BindView(R.id.button_send) FloatingActionButton mButtonSend;
    @BindView(R.id.fab) FloatingActionButton mFab;

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
        setContentView(R.layout.activity_message_details);
        ButterKnife.bind(this);
        parseExtras();
        setupRecycler();
        FirebaseUtils.markNotificationsAsRead(mMessageKey);
        loadDetailsFromDB();
        loadCommentsFromDB();
        mListComments.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                mListComments.postDelayed(() -> mListComments.smoothScrollToPosition(mCommentsAdapter.getItemCount()), 500);
            }
        });
        mCommentsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mListComments.smoothScrollToPosition(mCommentsAdapter.getItemCount());
            }
        });
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
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<MessageComment> list = new ArrayList<>();
                        for (DataSnapshot commentSS : dataSnapshot.getChildren()) {
                            MessageComment messageComment = commentSS.getValue(MessageComment.class);
                            if (messageComment != null) {
                                list.add(messageComment);
                            }
                        }
                        mCommentsAdapter.clearAll();
                        mCommentsAdapter.addAll(list);
                        new Handler().postDelayed(() -> {
                            if (mMessage.getCommentsEnabled())
                                mListComments.scrollToPosition(mCommentsAdapter.getItemCount());
                            loadMoreCommentsFromDB();
                        }, 300);
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
            //fab.setVisibility((mMessage.getLink() != null && !mMessage.getLink().isEmpty()) ? View.VISIBLE : View.GONE);
        } else {
            getSupportActionBar().setTitle(R.string.title_activity_notification_item);
        }
    }

    private void loadMoreCommentsFromDB() {
        database.getReference(DefaultConfigurations.DB_MESSAGES_COMMENTS)
                .child(mMessageKey)
                .orderByChild("date/time").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, final String s) {
                MessageComment messageComment = dataSnapshot.getValue(MessageComment.class);
                if (messageComment != null) {
                    mCommentsAdapter.add(messageComment);
                    FirebaseUtils.markNotificationsAsRead(mMessageKey);
                    if (mMessage.getCommentsEnabled())
                        mListComments.postDelayed(() -> mListComments.smoothScrollToPosition(mCommentsAdapter.getItemCount()), 500);
                }
            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, final String s) {
                MessageComment messageComment = dataSnapshot.getValue(MessageComment.class);
                if (messageComment != null) {
                    mCommentsAdapter.update(messageComment);
                    FirebaseUtils.markNotificationsAsRead(mMessageKey);
                }
            }

            @Override
            public void onChildRemoved(final DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(final DataSnapshot dataSnapshot, final String s) {
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_EDIT) {
            loadDetailsFromDB();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mListComments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private static final int HIDE_THRESHOLD = 20;
            private int scrolledDistance = 0;
            private boolean controlsVisible = true;

            @Override
            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                    mFab.hide();
                    controlsVisible = false;
                    scrolledDistance = 0;
                } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                    mFab.show();
                    controlsVisible = true;
                    scrolledDistance = 0;
                }
                if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
                    scrolledDistance += dy;
                }
            }
        });
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
                "EEEE dd.MM.yyyy").toUpperCase(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongClicked(final int position) {
        MessageComment item = mCommentsAdapter.getItemAtPostion(position);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_remove_title)
                .setMessage(item.getMessage())
                .setPositiveButton("Видалити", (dialog, which) -> {
                    removeMessage(item);
                })
                .setNegativeButton("Скасувати", null)
                .show();
    }

    private void removeMessage(final MessageComment item) {
        database.getReference(DefaultConfigurations.DB_MESSAGES_COMMENTS)
                .child(mMessageKey)
                .child(item.getKey())
                .child("removed")
                .setValue(true);
    }

    @OnClick(R.id.fab)
    public void onDownButtonClicked() {
        mListComments.smoothScrollToPosition(mCommentsAdapter.getItemCount());
        mFab.hide();
    }

    @OnClick(R.id.button_send)
    public void onSendButtonClicked() {
        String comment = mEditComment.getText().toString().trim();
        mEditComment.setText("");
        if (comment.isEmpty()) return;
        if (mMessageKey.isEmpty()) return;
        sendComment(comment);
    }

    private void sendComment(String comment) {
        String pushMessage = mUser.getDisplayName() + ": " + comment;
        DatabaseReference ref = database.getReference(DefaultConfigurations.DB_MESSAGES_COMMENTS)
                .child(mMessageKey)
                .push();
        ref.setValue(new MessageComment(ref.getKey(), comment, mUser));
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("lastComment", pushMessage);
        childUpdates.put("updatedAt", new Date());
        database.getReference(DefaultConfigurations.DB_MESSAGES)
                .child(mMessageKey)
                .updateChildren(childUpdates);
        FirebaseUtils.updateNotificationsForAllUsers(mMessageKey,
                mMessage.getTitle1(),
                pushMessage,
                MessageType.COMMENTS);
    }
}
