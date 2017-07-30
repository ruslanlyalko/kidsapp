package com.example.android.kidsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.kidsapp.utils.Constants;
import com.example.android.kidsapp.utils.Cost;
import com.example.android.kidsapp.utils.CostsAdapter;
import com.example.android.kidsapp.utils.Utils;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CostsActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    Button buttonDeleteAll;
    ImageButton buttonNext, buttonPrev;
    TextView textMonth, textUserName, textTotal, textCommon, textMk;
    ProgressBar progressBar, progressBarUpload;
    CompactCalendarView compactCalendarView;

    private CostsAdapter adapter;
    private List<Cost> costList = new ArrayList<>();
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward, fade, fade_back_quick;
    private TextView textFab1, textFab2;
    private View fadedBackground;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private String mTitle1, mTitle2, mPrice;
    private String pictureImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_costs);

        initRef();

        initRecycle();

        initFAB();

        buttonDeleteAll.setVisibility(Utils.isAdmin() ? View.VISIBLE : View.GONE);

        textUserName.setText(mAuth.getCurrentUser().getDisplayName());

        Calendar month = Calendar.getInstance();
        textMonth.setText(Constants.MONTH_FULL[month.get(Calendar.MONTH)]);

        // define a listener to receive callbacks when certain events happen.
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Calendar month = Calendar.getInstance();
                month.setTime(firstDayOfNewMonth);

                String yearSimple = new SimpleDateFormat("yy", Locale.US).format(firstDayOfNewMonth).toString();

                String str = Constants.MONTH_FULL[month.get(Calendar.MONTH)];

                if (firstDayOfNewMonth.getYear() != new Date().getYear())
                    str = str + "'" + yearSimple;

                textMonth.setText(str);

                String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(firstDayOfNewMonth).toString();
                String monthStr = new SimpleDateFormat("M", Locale.US).format(firstDayOfNewMonth).toString();

                loadCosts(yearStr, monthStr);
            }
        });
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compactCalendarView.showNextMonth();
            }
        });

        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compactCalendarView.showPreviousMonth();
            }
        });

        buttonDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utils.isAdmin()) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(CostsActivity.this);
                    builder.setTitle(R.string.dialog_cost_delete_all_title)
                            .setMessage(R.string.dialog_cost_delete_all_message)
                            .setPositiveButton("Видалити", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeAllCost();

                                    costList.clear();
                                    adapter.notifyDataSetChanged();

                                }

                            })
                            .setNegativeButton("Повернутись", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();
                }
            }
        });

        String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(new Date()).toString();
        String monthStr = new SimpleDateFormat("M", Locale.US).format(new Date()).toString();

        loadCosts(yearStr, monthStr);
    }

    /*
     int REQUEST_CODE_CAMERA =123;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
     */

    private void removeAllCost() {

        String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(new Date()).toString();
        String monthStr = new SimpleDateFormat("M", Locale.US).format(new Date()).toString();

        mDatabase.getReference(Constants.FIREBASE_REF_COSTS)
                .child(yearStr)
                .child(monthStr).removeValue();

        calcTotal();

    }

    private void initRef() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBarUpload = (ProgressBar) findViewById(R.id.progress_bar_upload);

        textMonth = (TextView) findViewById(R.id.text_month);
        textUserName = (TextView) findViewById(R.id.text_user_name);

        compactCalendarView = (CompactCalendarView) findViewById(R.id.calendar_view);
        textTotal = (TextView) findViewById(R.id.text_cost_total);
        textCommon = (TextView) findViewById(R.id.text_cost_common);
        textMk = (TextView) findViewById(R.id.text_cost_mk);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        textFab1 = (TextView) findViewById(R.id.textFab1);
        textFab2 = (TextView) findViewById(R.id.textFab2);
        fadedBackground = (View) findViewById(R.id.faded_background);
        buttonNext = (ImageButton) findViewById(R.id.button_next);
        buttonPrev = (ImageButton) findViewById(R.id.button_prev);
        buttonDeleteAll = (Button) findViewById(R.id.button_cost_delete_all);

    }


    private void initRecycle() {
        adapter = new CostsAdapter(this, costList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }


    private void loadCosts(String yearStr, String monthStr) {
        costList.clear();
        adapter.notifyDataSetChanged();
        calcTotal();

        mDatabase.getReference(Constants.FIREBASE_REF_COSTS).child(yearStr).child(monthStr)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Cost cost = dataSnapshot.getValue(Cost.class);
                        if (cost != null && (Utils.isAdmin() || cost.getUserId().equals(mAuth.getCurrentUser().getUid()))) {
                            costList.add(0, cost);
                            adapter.notifyItemInserted(0);
                            calcTotal();
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Cost cost = dataSnapshot.getValue(Cost.class);
                        if (cost != null) {
                            int i = 0;
                            for (Cost cost1 : costList) {
                                if (cost1.getKey().equals(cost.getKey())) {
                                    costList.remove(cost1);
                                    adapter.notifyItemRemoved(i);
                                    adapter.notifyDataSetChanged();
                                    calcTotal();
                                    break;
                                }
                                i++;
                            }
                        }
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void calcTotal() {
        int common = 0;
        int mk = 0;

        for (Cost cost : costList) {
            if (cost.getTitle2().equals(getString(R.string.text_cost_common)))
                common += cost.getPrice();
            if (cost.getTitle2().equals(getString(R.string.text_cost_mk))) mk += cost.getPrice();
        }

        int total = common + mk;

        progressBar.setMax(total);
        progressBar.setProgress(common);

        textCommon.setText(Utils.getIntWithSpace(common) + " грн");
        textMk.setText(Utils.getIntWithSpace(mk) + " грн");

        textTotal.setText(Utils.getIntWithSpace(total) + " ГРН");
    }


    private void initFAB() {
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
        fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        fade_back_quick = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_back_quick);

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
                addCostDialog(getString(R.string.text_cost_common));
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
                addCostDialog(getString(R.string.text_cost_mk));
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });

        fadedBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });
    }

    private void addCostDialog(final String title2) {

        final String title22 = title2;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введіть опис");


        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_cost, null, false);
        builder.setView(viewInflated);
        final EditText inputTitle1 = (EditText) viewInflated.findViewById(R.id.text_title1);
        final EditText inputPrice = (EditText) viewInflated.findViewById(R.id.text_price);

        builder.setPositiveButton("ДОБАВИТИ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String title1 = inputTitle1.getText().toString();
                String price = inputPrice.getText().toString();

                addCost(title1, title22, price, "");

            }
        });
        builder.setNeutralButton("+ ФОТО", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mTitle1 = inputTitle1.getText().toString();
                mTitle2 = title2;
                mPrice = inputPrice.getText().toString();

                startCamera();
            }
        });
        builder.setNegativeButton("Відмінити", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }


    void startCamera() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(pictureImagePath);
        Uri outputFileUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(cameraIntent, Constants.REQUEST_CODE_CAMERA);
        showProgressBarUpload();
    }

    void showProgressBarUpload() {
        progressBarUpload.setVisibility(View.VISIBLE);
    }


    void hideProgressBarUpload() {
        progressBarUpload.setVisibility(View.GONE);
        recyclerView.scrollToPosition(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // handle request from camera
        if (requestCode == Constants.REQUEST_CODE_CAMERA) {
            if (resultCode == RESULT_OK) {


                Bitmap bitmap = BitmapFactory.decodeFile(pictureImagePath);//= imageView.getDrawingCache();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] bytes = baos.toByteArray();

                // Meta data for imageView
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("imageView/jpg")
                        .setCustomMetadata("Title1", mTitle1)
                        .setCustomMetadata("Title2", mTitle2)
                        .setCustomMetadata("Uid", mAuth.getCurrentUser().getUid())
                        .build();

                // name of file in Storage
                final String filename = Utils.getCurrentTimeStamp() + "_" + mAuth.getCurrentUser().getUid() + ".jpg";

                UploadTask uploadTask = FirebaseStorage.getInstance()
                        .getReference(Constants.FIREBASE_STORAGE_COST)
                        .child(filename)
                        .putBytes(bytes, metadata);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        addCost(mTitle1, mTitle2, mPrice, filename);
                        hideProgressBarUpload();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        addCost(mTitle1, mTitle2, mPrice, "");
                        hideProgressBarUpload();
                    }
                });

            } else { //resultCode
                addCost(mTitle1, mTitle2, mPrice, "");
                hideProgressBarUpload();

            }

        }
        //other request code
    }

    private void addCost(String title1, String title22, String price, String uri) {
        if (price == null || price.isEmpty())
            price = "0";

        Cost cost = new Cost(title1, title22,
                new SimpleDateFormat("d-M-yyyy", Locale.US).format(new Date()).toString(),
                uri,
                mAuth.getCurrentUser().getUid(),
                mAuth.getCurrentUser().getDisplayName(),
                Integer.parseInt(price));

        addCostToDb(cost);
    }

    private void addCostToDb(Cost newCost) {

        String yearStr = new SimpleDateFormat("yyyy", Locale.US).format(new Date()).toString();
        String monthStr = new SimpleDateFormat("M", Locale.US).format(new Date()).toString();
        String time = new SimpleDateFormat("HH:mm", Locale.US).format(new Date()).toString();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_REF_COSTS)
                .child(yearStr)
                .child(monthStr);

        String key = ref.push().getKey();
        newCost.setKey(key);
        newCost.setTime(time);

        ref.child(key).setValue(newCost).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                recyclerView.smoothScrollToPosition(0);
            }
        });
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
            fadedBackground.setClickable(false);
            fadedBackground.startAnimation(fade_back_quick);
            isFabOpen = false;
        } else {
            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            textFab1.startAnimation(fab_open);
            textFab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            fadedBackground.setClickable(true);
            fadedBackground.startAnimation(fade);
            isFabOpen = true;
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
        if (progressBarUpload.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, R.string.photo_uploading, Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }
}
