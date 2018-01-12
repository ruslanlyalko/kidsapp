package com.ruslanlyalko.kidsapp.presentation.ui.notifications;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.Utils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Notification;
import com.ruslanlyalko.kidsapp.data.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotificationsEditActivity extends AppCompatActivity {

    private EditText textDescription;
    private EditText textTitle1;
    private EditText textTitle2;
    private EditText textLink;
    private ImageView imageView;
    private ProgressBar progressBar;

    // VARIABLES
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String notKey;
    Notification notification = new Notification();
    boolean isNew = false;
    boolean needToSave = false;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.nothing);
        setContentView(R.layout.activity_notification_edit);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            notKey = bundle.getString(Keys.Extras.EXTRA_ITEM_ID);
        }
        isNew = notKey == null;
        initRef();
        loadMkItem();
        updateUI();
    }

    private void initRef() {
        progressBar = findViewById(R.id.progress_bar);
        textTitle1 = findViewById(R.id.edit_title1);
        textTitle2 = findViewById(R.id.edit_title2);
        textLink = findViewById(R.id.edit_link);
        textDescription = findViewById(R.id.edit_description);
        imageView = findViewById(R.id.image_view);

        /*
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, Constants.REQUEST_CODE_GALLERY);
            }
        });*/
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                needToSave = true;
            }
        };
        textTitle1.addTextChangedListener(watcher);
        textTitle2.addTextChangedListener(watcher);
        textLink.addTextChangedListener(watcher);
        textDescription.addTextChangedListener(watcher);
    }
/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data == null)
                return;

            needToSave = true;
            progressBar.setVisibility(View.VISIBLE);

            //upload to imageView view
            Uri selectedImage = data.getData();
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageURI(selectedImage);

            final String imageName = (key != null ? key : "newMK")
                    + new SimpleDateFormat("_ddMMyyyy_HHmmss").format(new Date()) + ".jpg";

            // save in database
            notification.setImageUri(imageName);

            // upload to storage
            StorageReference ref = storage.getReference(Constants.STORAGE_MK).child(imageName);
            ref.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    progressBar.setVisibility(View.GONE);
                }
            });

        }
    }*/

    private void loadMkItem() {
        if (isNew) return;
        DatabaseReference ref = database.getReference(DefaultConfigurations.DB_NOTIFICATIONS).child(notKey);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notification = dataSnapshot.getValue(Notification.class);
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void updateNotModel() {
        notification.setTitle1(textTitle1.getText().toString());
        notification.setTitle2(textTitle2.getText().toString());
        notification.setLink(textLink.getText().toString());
        notification.setDescription(textDescription.getText().toString());
    }

    private void addNotification() {
        updateNotModel();
        isNew = false;
        notKey = database.getReference(DefaultConfigurations.DB_NOTIFICATIONS).push().getKey();
        notification.setKey(notKey);
        notification.setDate(new SimpleDateFormat("d-M-yyyy").format(new Date()));
        notification.setUserId(auth.getCurrentUser().getUid());
        notification.setUserName(auth.getCurrentUser().getDisplayName());
        database.getReference(DefaultConfigurations.DB_NOTIFICATIONS)
                .child(notKey).setValue(notification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Utils.updateNotificationsForAllUsers(notKey);
                Snackbar.make(imageView, getString(R.string.not_added), Snackbar.LENGTH_SHORT).show();
            }
        });
        needToSave = false;
    }

    private void updateNotification() {
        updateNotModel();
        database.getReference(DefaultConfigurations.DB_NOTIFICATIONS)
                .child(notification.getKey()).setValue(notification).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Utils.updateNotificationsForAllUsers(notKey);
                Snackbar.make(imageView, getString(R.string.mk_updated), Snackbar.LENGTH_SHORT).show();
            }
        });
        needToSave = false;
    }

    private void updateUI() {
        if (isNew) {
            setTitle(R.string.title_activity_add);
        } else {
            setTitle(R.string.title_activity_edit);
            if (notification != null) {
                textTitle1.setText(notification.getTitle1());
                textTitle2.setText(notification.getTitle2());
                textLink.setText(notification.getLink());
                textDescription.setText(notification.getDescription());

                /*
                if (notification.getImageUri() != null && !notification.getImageUri().isEmpty()) {

                    StorageReference ref = storage.getReference(Constants.STORAGE_MK).child(notification.getImageUri());
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Glide.with(this).using(new FirebaseImageLoader()).load(ref).into(imageView);
                }*/
            }
        }
        needToSave = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.action_save) {
            if (isNew)
                addNotification();
            else
                updateNotification();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, R.string.photo_uploading, Toast.LENGTH_SHORT).show();
            return;
        }
        if (needToSave) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NotificationsEditActivity.this);
            builder.setTitle(R.string.dialog_report_save_before_close_title)
                    .setMessage(R.string.dialog_mk_edit_text)
                    .setPositiveButton("ЗБЕРЕГТИ ЗМІНИ", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (isNew)
                                addNotification();
                            else
                                updateNotification();
                            onBackPressed();
                        }
                    })
                    .setNegativeButton("НЕ ЗБЕРІГАТИ", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            needToSave = false;
                            onBackPressed();
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.nothing, R.anim.fadeout);
        }
    }
}
