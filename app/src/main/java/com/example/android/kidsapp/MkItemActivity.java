package com.example.android.kidsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
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
    private Menu mMenu;

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
            if (mMenu != null) {
                mMenu.findItem(R.id.action_delete_mk).setVisible(Utils.isAdmin()
                        || mk.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));

                mMenu.findItem(R.id.action_edit).setVisible(Utils.isAdmin()
                        || mk.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));

            }
            toolbarLayout.setTitle(mk.getTitle1());
            textTitle2.setText(mk.getTitle2());
            textDescription.setText(mk.getDescription());

            fab.setVisibility((mk.getLink() != null && !mk.getLink().isEmpty())
                    ? View.VISIBLE : View.GONE);

            if (mk.getImageUri() != null && !mk.getImageUri().isEmpty()) {

                StorageReference ref = storage.getReference(Constants.FIREBASE_STORAGE_MK).child(mk.getImageUri());
                Glide.with(this).using(new FirebaseImageLoader()).load(ref).into(imageView);
            }

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
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                builder.setToolbarColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
                customTabsIntent.launchUrl(MkItemActivity.this, Uri.parse(mk.getLink()));
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

        mMenu = menu;

        if (mk != null) {
            mMenu.findItem(R.id.action_delete_mk).setVisible(Utils.isAdmin()
                    || mk.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));
            mMenu.findItem(R.id.action_edit).setVisible(Utils.isAdmin()
                    || mk.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.action_edit) {
            if (Utils.isAdmin()
                    || mk.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                editMk();
                onBackPressed();
            }
        }
        if (id == R.id.action_delete_mk) {

            if (Utils.isAdmin()
                    || mk.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                deleteMk();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void editMk() {
        Intent intent = new Intent(MkItemActivity.this, MkEditActivity.class);
        intent.putExtra(Constants.EXTRA_MK_ID, mkKey);
        startActivityForResult(intent, Constants.REQUEST_CODE_EDIT);
    }

    private void deleteMk() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MkItemActivity.this);

        AlertDialog dialog = builder.setTitle(R.string.dialog_delete_mk_title)
                .setMessage(R.string.dialog_delete_mk_title)
                .setPositiveButton("Видалити", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        database.getReference(Constants.FIREBASE_REF_MK).child(mk.getKey()).removeValue();
                        onBackPressed();
                    }
                })
                .setNegativeButton("Повернутись", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).create();

        dialog.show();
    }


}
