package com.example.android.kidsapp;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Mk;
import com.example.android.kidsapp.utils.MksAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MkActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MksAdapter adapter;
    private List<Mk> mkList;

    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward, fade, fade_back;
    private TextView textFab1, textFab2;
    private View fadedBeckground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setContentView(R.layout.activity_mk);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initCollapsingToolbar();
        initRef();

        mkList = new ArrayList<>();
        adapter = new MksAdapter(this, mkList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        loadMK();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("mk-cards").child("cover.jpg");
        try {
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .into((ImageView) findViewById(R.id.backdrop));

        } catch (Exception e) {
            e.printStackTrace();
        }

        initFAB();
    }

    private void loadMK() {
        mkList.clear();

        FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_REF_MK).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Mk mk = dataSnapshot.getValue(Mk.class);
                if (mk != null) {
                    mkList.add(0, mk);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void initFAB() {
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
        fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        fade_back = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_back);

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
                Mk newMk = new Mk("Новий МК", "Кулінарний майстер клас", "Опис", Constants.URI_CARD_MK_K, FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                addMk(newMk);
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(0);
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
                Mk newMk = new Mk("Новий МК", "Творчий майстер клас", "Опис", Constants.URI_CARD_MK_T, FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                addMk(newMk);
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(0);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });


        fadedBeckground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });

    }

    /**
     * Save new MK to database
     *
     * @param newMk
     */
    private void addMk(Mk newMk) {
        String key = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_REF_MK).push().getKey();

        newMk.setKey(key);

        FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_REF_MK)
                .child(key).setValue(newMk);
    }

    private void initRef() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        textFab1 = (TextView) findViewById(R.id.textFab1);
        textFab2 = (TextView) findViewById(R.id.textFab2);
        fadedBeckground = (View) findViewById(R.id.fadedbackgroud);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    }

    public void animateFAB() {

        if (isFabOpen) {

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);

            textFab1.startAnimation(fab_close);
            textFab2.startAnimation(fab_close);
//            body.startAnimation(fade_back);


            fadedBeckground.setClickable(false);
            fadedBeckground.startAnimation(fade_back);

            isFabOpen = false;

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            textFab1.startAnimation(fab_open);
            textFab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
//            body.startAnimation(fade);

            fadedBeckground.setClickable(true);
            fadedBeckground.startAnimation(fade);
            isFabOpen = true;
        }
    }

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */

    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.title_activity_mk));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }


    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }

    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isFabOpen) {
            animateFAB();
            return;
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
}
