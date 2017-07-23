package com.example.android.kidsapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Mk;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MkEditActivity extends AppCompatActivity {

    private EditText textDescription, textTitle1, textTitle2;
    private ImageView imageView;
    private ProgressBar progressBar;

    // VARIABLES
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String mkKey, mkTitle2;
    Mk mk;
    boolean isNew = false;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mk_edit);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            mkKey = bundle.getString(Constants.EXTRA_MK_ID);
            mkTitle2 = bundle.getString(Constants.EXTRA_MK_TITLE2);
        }

        isNew = mkKey == null;

        initRef();

        loadMkItem();

        updateUI();

    }


    private void initRef() {

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        textTitle1 = (EditText) findViewById(R.id.edit_title1);
        textTitle2 = (EditText) findViewById(R.id.edit_title2);
        textDescription = (EditText) findViewById(R.id.edit_description);
        imageView = (ImageView) findViewById(R.id.image_view);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, Constants.REQUEST_CODE_GALLERY);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }

            Uri selectedImage = data.getData();

            // todo upload to Storage
            imageView.setImageURI(selectedImage);

            final String imageName = (mk != null ? mk.getKey() : "") 
                    + new SimpleDateFormat("_ddMMyyyy_HHmmss").format(new Date()) + ".jpg";

            StorageReference ref = storage.getReference(Constants.FIREBASE_STORAGE_MK).child(imageName);
            mk.setImageUri(imageName);
            progressBar.setVisibility(View.VISIBLE);
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

        DatabaseReference ref = database.getReference(Constants.FIREBASE_REF_MK).child(mkKey);

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

    private void addMk() {
        String key = database.getReference(Constants.FIREBASE_REF_MK).push().getKey();

        mk.setKey(key);
        isNew = false;

        database.getReference(Constants.FIREBASE_REF_MK)
                .child(key).setValue(mk).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // todo snackbar
            }
        });

    }

    private void updateMk() {
        database.getReference(Constants.FIREBASE_REF_MK)
                .child(mk.getKey()).setValue(mk).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //todo snackbar
            }
        });
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
                textDescription.setText(mk.getDescription());
                if (mk.getImageUri() != null && !mk.getImageUri().isEmpty()) {

                    StorageReference ref = storage.getReference(Constants.FIREBASE_STORAGE_MK).child(mk.getImageUri());
                    Glide.with(this).using(new FirebaseImageLoader()).load(ref).into(imageView);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mk_edit, menu);
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
            if (mk == null) mk = new Mk();

            mk.setTitle1(textTitle1.getText().toString());
            mk.setTitle2(textTitle2.getText().toString());
            mk.setDescription(textDescription.getText().toString());


            if (isNew)
                addMk();
            else
                updateMk();
        }
        return super.onOptionsItemSelected(item);
    }

}
