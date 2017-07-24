package com.example.android.kidsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Mk;
import com.example.android.kidsapp.utils.Utils;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class MkItemActivity extends AppCompatActivity {

    //VIEWS
    private Toolbar toolbar;
    private CollapsingToolbarLayout toolbarLayout;
    private TextView textDescription, textTitle2;
    private ImageView imageView;
    private FloatingActionButton fab;

    // VARIABLES
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String mkKey;
    Mk mk;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mk_item);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            mkKey = bundle.getString(Constants.EXTRA_MK_ID);
        }

        initRef();

        loadMkFromDB();
    }

    private void loadMkFromDB() {
        if (mkKey == null || mkKey.isEmpty()) return;

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

    private void updateUI() {
        if (mk != null) {
            toolbarLayout.setTitle(mk.getTitle1());
            textTitle2.setText(mk.getTitle2());
            textDescription.setText(mk.getDescription());

            fab.setVisibility((Utils.isIsAdmin() || mk.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                    ? View.VISIBLE : View.GONE);

            if (mk.getImageUri() != null && !mk.getImageUri().isEmpty()) {

                StorageReference ref = storage.getReference(Constants.FIREBASE_STORAGE_MK).child(mk.getImageUri());
                Glide.with(this).using(new FirebaseImageLoader()).load(ref).into(imageView);
            }

            //todo add other fields
        } else {
            // mk == null
            toolbar.setTitle(R.string.title_activity_mk_item);
        }
    }

    private void initRef() {

        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        textDescription = (TextView) findViewById(R.id.text_description);
        textTitle2 = (TextView) findViewById(R.id.text_title2);
        imageView = (ImageView) findViewById(R.id.image_view);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo add logic for making edit
                Intent intent = new Intent(MkItemActivity.this, MkEditActivity.class);
                intent.putExtra(Constants.EXTRA_MK_ID, mkKey);
                startActivityForResult(intent, Constants.REQUEST_CODE_EDIT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_EDIT) {

            loadMkFromDB();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mk_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.action_delete_mk) {

            deleteMk();
            onBackPressed();
            //todo
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteMk() {

        database.getReference(Constants.FIREBASE_REF_MK).child(mk.getKey()).removeValue();
    }


}
