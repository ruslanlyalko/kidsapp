package com.example.android.kidsapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.kidsapp.utils.Constants;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ShowImageActivity extends AppCompatActivity {

    private String uri = "";
    private String userName = "";
    private String folder= "";
    private float mx, my;
    boolean scroll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            uri = bundle.getString(Constants.EXTRA_URI);
            userName = bundle.getString(Constants.EXTRA_USER_NAME);
            folder = bundle.getString(Constants.EXTRA_FOLDER, Constants.FIREBASE_STORAGE_COST);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadImage(uri);
    }


    private void loadImage(String uri) {
        if (uri.isEmpty()) return;

        StorageReference ref = FirebaseStorage.getInstance().getReference(folder).child(uri);
        final ImageView image = (ImageView) findViewById(R.id.imageView);
        //load image using Glide
        Glide.with(ShowImageActivity.this).using(new FirebaseImageLoader()).load(ref).into(image);

        setTitle("Фото (" + userName + ")");

        // scroll image
        image.setOnTouchListener(new View.OnTouchListener() {

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
                            image.scrollBy((int) (mx - curX), (int) (my - curY));
                            mx = curX;
                            my = curY;
                            break;
                        case MotionEvent.ACTION_UP:
                            curX = event.getX();
                            curY = event.getY();
                            image.scrollBy((int) (mx - curX), (int) (my - curY));
                            break;
                    }
                }
                return false;
            }
        });


        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image.setScaleType(ImageView.ScaleType.CENTER);
                scroll = true;
            }
        });


        Toast.makeText(this, "Загрузка....", Toast.LENGTH_SHORT).show();
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

}
