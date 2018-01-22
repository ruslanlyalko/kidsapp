package com.ruslanlyalko.kidsapp.presentation.ui.main.mk;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Constants;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Mk;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MkEditActivity extends AppCompatActivity {

    @BindView(R.id.edit_title1) EditText textTitle1;
    @BindView(R.id.text_title2) TextView textTitle2;
    @BindView(R.id.edit_description) EditText textDescription;
    @BindView(R.id.edit_link) EditText textLink;
    @BindView(R.id.image_view) ImageView imageView;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    boolean mIsNew = false;
    boolean mNeedToSave = false;
    private Mk mMk = new Mk();
    private String mMkKey;
    private String mMkTitle2;
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    public static Intent getLaunchIntent(final Activity launchIntent, final String mkKey) {
        Intent intent = new Intent(launchIntent, MkEditActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_ITEM_ID, mkKey);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.nothing);
        setContentView(R.layout.activity_mk_edit);
        ButterKnife.bind(this);
        parseExtras();
        mIsNew = mMkKey == null;
        initRef();
        loadMkItem();
        updateUI();
    }

    private void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mMkKey = bundle.getString(Keys.Extras.EXTRA_ITEM_ID);
            mMkTitle2 = bundle.getString(Keys.Extras.EXTRA_TITLE2);
        }
    }

    private void initRef() {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, Constants.REQUEST_CODE_GALLERY);
            }
        });
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mNeedToSave = true;
            }
        };
        textTitle1.addTextChangedListener(watcher);
        textTitle2.addTextChangedListener(watcher);
        textLink.addTextChangedListener(watcher);
        textDescription.addTextChangedListener(watcher);
    }

    private void loadMkItem() {
        if (mIsNew) return;
        DatabaseReference ref = database.getReference(DefaultConfigurations.DB_MK).child(mMkKey);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMk = dataSnapshot.getValue(Mk.class);
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void updateUI() {
        if (mIsNew) {
            setTitle(R.string.title_activity_add);
            textTitle2.setText(mMkTitle2);
        } else {
            setTitle(R.string.title_activity_edit);
            if (mMk != null) {
                textTitle1.setText(mMk.getTitle1());
                textTitle2.setText(mMk.getTitle2());
                textLink.setText(mMk.getLink());
                textDescription.setText(mMk.getDescription());
                if (mMk.getImageUri() != null && !mMk.getImageUri().isEmpty()) {
                    StorageReference ref = storage.getReference(DefaultConfigurations.STORAGE_MK).child(mMk.getImageUri());
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ref.getDownloadUrl().addOnSuccessListener(uri ->
                            Glide.with(MkEditActivity.this).load(uri).into(imageView));
                }
            }
        }
        mNeedToSave = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data == null)
                return;
            mNeedToSave = true;
            progressBar.setVisibility(View.VISIBLE);
            //upload to imageView view
            Uri selectedImage = data.getData();
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageURI(selectedImage);
            final String imageName = (mMkKey != null ? mMkKey : "newMK")
                    + new SimpleDateFormat("_ddMMyyyy_HHmmss", Locale.US).format(new Date()) + ".jpg";
            // save in database
            mMk.setImageUri(imageName);
            // upload to storage
            StorageReference ref = storage.getReference(DefaultConfigurations.STORAGE_MK).child(imageName);
            ref.putFile(selectedImage).addOnCompleteListener(task -> progressBar.setVisibility(View.GONE));
        }
    }

    @Override
    public void onBackPressed() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, R.string.photo_uploading, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mNeedToSave) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MkEditActivity.this);
            builder.setTitle(R.string.dialog_discart_changes)
                    .setPositiveButton(R.string.action_discard, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mNeedToSave = false;
                            onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.nothing, R.anim.fadeout);
        }
    }

    private void updateMkModel() {
        mMk.setTitle1(textTitle1.getText().toString());
        mMk.setTitle2(textTitle2.getText().toString());
        mMk.setLink(textLink.getText().toString());
        mMk.setDescription(textDescription.getText().toString());
    }

    private void addMk() {
        updateMkModel();
        mIsNew = false;
        mMkKey = database.getReference(DefaultConfigurations.DB_MK).push().getKey();
        mMk.setKey(mMkKey);
        mMk.setUserId(mUser.getUid());
        mMk.setUserName(mUser.getDisplayName());
        database.getReference(DefaultConfigurations.DB_MK)
                .child(mMkKey).setValue(mMk).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(imageView, getString(R.string.mk_added), Snackbar.LENGTH_SHORT).show();
            }
        });
        mNeedToSave = false;
    }

    private void updateMk() {
        updateMkModel();
        database.getReference(DefaultConfigurations.DB_MK)
                .child(mMk.getKey()).setValue(mMk).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MkEditActivity.this, getString(R.string.mk_updated), Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
        mNeedToSave = false;
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
            if (mIsNew)
                addMk();
            else
                updateMk();
        }
        return super.onOptionsItemSelected(item);
    }
}
