package com.ruslanlyalko.kidsapp.presentation.widget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoPreviewActivity extends AppCompatActivity {

    @BindView(R.id.photo_view) PhotoView imageView;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    private String uri = "";
    private String userName = "";
    private String folder = "";

    public static Intent getLaunchIntent(final AppCompatActivity launchActivity, String uri, String userName) {
        Intent intent = new Intent(launchActivity, PhotoPreviewActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_URI, uri);
        intent.putExtra(Keys.Extras.EXTRA_USER_NAME, userName);
        return intent;
    }

    public static Intent getLaunchIntent(final AppCompatActivity launchActivity, final String uri, final String userName, final String storage) {
        Intent intent = new Intent(launchActivity, PhotoPreviewActivity.class);
        intent.putExtra(Keys.Extras.EXTRA_URI, uri);
        intent.putExtra(Keys.Extras.EXTRA_USER_NAME, userName);
        intent.putExtra(Keys.Extras.EXTRA_FOLDER, storage);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.nothing);
        setContentView(R.layout.activity_show_image);
        ButterKnife.bind(this);
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            uri = bundle.getString(Keys.Extras.EXTRA_URI);
            userName = bundle.getString(Keys.Extras.EXTRA_USER_NAME);
            folder = bundle.getString(Keys.Extras.EXTRA_FOLDER, DefaultConfigurations.STORAGE_EXPENSES);
        }
        loadWithGlide(uri);
    }

    private void loadWithGlide(String uri) {
        if (uri == null || uri.isEmpty()) return;
        setTitle("Загрузка....");
        if (uri.contains("http")) {
            Glide.with(PhotoPreviewActivity.this).load(uri).listener(new RequestListener<String, GlideDrawable>() {
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
            Glide.with(PhotoPreviewActivity.this).using(new FirebaseImageLoader()).load(ref).listener(new RequestListener<StorageReference, GlideDrawable>() {
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
