package com.ruslanlyalko.kidsapp.presentation.widget;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;

public class ShowImageActivity extends AppCompatActivity {

    private String uri = "";
    private String userName = "";
    private String folder = "";
    private float mx, my;
    boolean scroll = false;
    ImageView imageView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.nothing);
        setContentView(R.layout.activity_show_image);
        imageView = (ImageView) findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progress_bar);
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            uri = bundle.getString(Keys.Extras.EXTRA_URI);
            userName = bundle.getString(Keys.Extras.EXTRA_USER_NAME);
            folder = bundle.getString(Keys.Extras.EXTRA_FOLDER, DefaultConfigurations.STORAGE_COST);
        }
        loadWithGlide(uri);
        // scroll imageView
        imageView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View arg0, MotionEvent event) {
                if (scroll) {
                    float curX, curY;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mx = event.getX();
                            my = event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            curX = event.getX();
                            curY = event.getY();
                            imageView.scrollBy((int) (mx - curX), (int) (my - curY));
                            mx = curX;
                            my = curY;
                            break;
                        case MotionEvent.ACTION_UP:
                            curX = event.getX();
                            curY = event.getY();
                            imageView.scrollBy((int) (mx - curX), (int) (my - curY));
                            break;
                    }
                }
                return false;
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                scroll = true;
            }
        });
    }

    private void loadWithGlide(String uri) {
        if (uri == null || uri.isEmpty()) return;
        setTitle("Загрузка....");
        if (uri.contains("http")) {
            Glide.with(ShowImageActivity.this).load(uri).listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    setTitle("Помилка при загрузці");
                    progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    setTitle("Автор: " + userName);
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(imageView);
        } else {
            StorageReference ref = FirebaseStorage.getInstance().getReference(folder).child(uri);
            //load imageView using Glide
            Glide.with(ShowImageActivity.this).using(new FirebaseImageLoader()).load(ref).listener(new RequestListener<StorageReference, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, StorageReference model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFirstResource) {
                    setTitle("Помилка при загрузці");
                    progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, StorageReference model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    setTitle("Автор: " + userName);
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(imageView);
        }
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
        super.onBackPressed();
        overridePendingTransition(R.anim.nothing, R.anim.fadeout);
    }
}
