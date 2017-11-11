package com.ruslanlyalko.kidsapp.presentation.ui.mk;

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
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

public class MkEditActivity extends AppCompatActivity {

    private EditText textDescription, textTitle1, textLink;
    private ImageView imageView;
    private ProgressBar progressBar;
    private TextView textTitle2;
    // VARIABLES
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String mkKey, mkTitle2;
    Mk mk = new Mk();
    boolean isNew = false;
    boolean needToSave = false;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.nothing);
        setContentView(R.layout.activity_mk_edit);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mkKey = bundle.getString(Keys.Extras.EXTRA_ITEM_ID);
            mkTitle2 = bundle.getString(Keys.Extras.EXTRA_TITLE2);
        }
        isNew = mkKey == null;
        initRef();
        loadMkItem();
        updateUI();
    }

    private void initRef() {
        progressBar = findViewById(R.id.progress_bar);
        textTitle1 = findViewById(R.id.edit_title1);
        textTitle2 = findViewById(R.id.text_title2);
        textLink = findViewById(R.id.edit_link);
        textDescription = findViewById(R.id.edit_description);
        imageView = findViewById(R.id.image_view);
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
                needToSave = true;
            }
        };
        textTitle1.addTextChangedListener(watcher);
        textTitle2.addTextChangedListener(watcher);
        textLink.addTextChangedListener(watcher);
        textDescription.addTextChangedListener(watcher);
    }

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
            final String imageName = (mkKey != null ? mkKey : "newMK")
                    + new SimpleDateFormat("_ddMMyyyy_HHmmss").format(new Date()) + ".jpg";
            // save in database
            mk.setImageUri(imageName);
            // upload to storage
            StorageReference ref = storage.getReference(DefaultConfigurations.STORAGE_MK).child(imageName);
            ref.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void loadMkItem() {
        if (isNew) return;
        DatabaseReference ref = database.getReference(DefaultConfigurations.DB_MK).child(mkKey);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mk = dataSnapshot.getValue(Mk.class);
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void updateMkModel() {
        mk.setTitle1(textTitle1.getText().toString());
        mk.setTitle2(textTitle2.getText().toString());
        mk.setLink(textLink.getText().toString());
        mk.setDescription(textDescription.getText().toString());
    }

    private void addMk() {
        updateMkModel();
        isNew = false;
        mkKey = database.getReference(DefaultConfigurations.DB_MK).push().getKey();
        mk.setKey(mkKey);
        mk.setUserId(auth.getCurrentUser().getUid());
        mk.setUserName(auth.getCurrentUser().getDisplayName());
        database.getReference(DefaultConfigurations.DB_MK)
                .child(mkKey).setValue(mk).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(imageView, getString(R.string.mk_added), Snackbar.LENGTH_SHORT).show();
            }
        });
        needToSave = false;
    }

    private void updateMk() {
        updateMkModel();
        database.getReference(DefaultConfigurations.DB_MK)
                .child(mk.getKey()).setValue(mk).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(imageView, getString(R.string.mk_updated), Snackbar.LENGTH_SHORT).show();
            }
        });
        needToSave = false;
    }

    private void updateUI() {
        if (isNew) {
            setTitle(R.string.title_activity_add);
            textTitle2.setText(mkTitle2);
        } else {
            setTitle(R.string.title_activity_edit);
            if (mk != null) {
                textTitle1.setText(mk.getTitle1());
                textTitle2.setText(mk.getTitle2());
                textLink.setText(mk.getLink());
                textDescription.setText(mk.getDescription());
                if (mk.getImageUri() != null && !mk.getImageUri().isEmpty()) {
                    StorageReference ref = storage.getReference(DefaultConfigurations.STORAGE_MK).child(mk.getImageUri());
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Glide.with(this).using(new FirebaseImageLoader()).load(ref).into(imageView);
                }
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
                addMk();
            else
                updateMk();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(MkEditActivity.this);
            builder.setTitle(R.string.dialog_discart_changes)
                    .setPositiveButton(R.string.action_discard, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            needToSave = false;
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
}
