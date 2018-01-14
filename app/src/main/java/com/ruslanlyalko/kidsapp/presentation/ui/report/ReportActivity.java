package com.ruslanlyalko.kidsapp.presentation.ui.report;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;
import com.ruslanlyalko.kidsapp.R;
import com.ruslanlyalko.kidsapp.common.Constants;
import com.ruslanlyalko.kidsapp.common.DateUtils;
import com.ruslanlyalko.kidsapp.common.Keys;
import com.ruslanlyalko.kidsapp.data.Utils;
import com.ruslanlyalko.kidsapp.data.configuration.DefaultConfigurations;
import com.ruslanlyalko.kidsapp.data.models.Mk;
import com.ruslanlyalko.kidsapp.data.models.Report;
import com.ruslanlyalko.kidsapp.presentation.ui.calendar.CalendarActivity;
import com.ruslanlyalko.kidsapp.presentation.widget.PhotoPreviewActivity;
import com.ruslanlyalko.kidsapp.presentation.widget.SwipeLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class ReportActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_IMAGE_PERMISSION = 1;
    TextView textRoom60;
    TextView textRoom30;
    TextView textRoom20;
    TextView textRoom10;
    TextView textRoomTotal;
    TextView textBday50;
    TextView textBday10;
    TextView textBday30;
    TextView textBdayTotal;
    TextView textBdayMk;
    TextView textMk1;
    TextView textMk2;
    TextView textMkT1;
    TextView textMkT2;
    TextView textMkTotal;

    TextView textDate;
    TextView textMkName;
    LinearLayout panelDate;
    LinearLayout panelRoomExpand;
    LinearLayout panelRoomExpand2;
    LinearLayout panelRoomExpand3;
    LinearLayout panelPhoto;

    SeekBar seekRoom60;
    SeekBar seekRoom30;
    SeekBar seekRoom20;
    SeekBar seekRoom10;
    SeekBar seekBday50;
    SeekBar seekBday10;
    SeekBar seekBday30;
    SeekBar seekBdayMk;
    SeekBar seekMkT1;
    SeekBar seekMkT2;
    SeekBar seekMk1;
    SeekBar seekMk2;

    EditText inputRoom60;
    EditText inputRoom30;
    EditText inputRoom20;
    EditText inputRoom10;
    EditText inputBday50;
    EditText inputBday10;
    EditText inputBday30;
    EditText inputMk1;
    EditText inputMk2;
    SwipeLayout swipeLayout;
    SwipeLayout swipeLayout2;
    SwipeLayout swipeLayout3;

    EditText editComment;
    TextView textPhoto;
    Button buttonChooseMk;
    Switch switchMyMk;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private String mUId, mUserName;
    private Calendar mDate;
    private String mDateStr, mDateMonth, mDateYear, mDateDay;
    private Report mReport;
    private SimpleDateFormat mSdf = new SimpleDateFormat("d-M-yyyy", Locale.US);
    private boolean isChanged;
    private List<Mk> mkList = new ArrayList<>();
    private ProgressBar progressBarUpload;
    private boolean mIsFuture;
    private String pictureImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        initRef();
        parseExtras();
        initDatePicker();
        initSwipesAndExpandPanels();
        initSeeks();
        loadReportFromDB();
        loadMK();
        buttonChooseMk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseMkDialog();
            }
        });
        panelPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = mReport.imageUri;
                if (uri != null && !uri.isEmpty()) {
                    startActivity(PhotoPreviewActivity.getLaunchIntent(
                            ReportActivity.this, uri, mReport.getUserName(), DefaultConfigurations.STORAGE_REPORT));
                } else {
                    // start camera to take photo
                    startCamera();
                }
            }
        });
        panelPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startCamera();
                return false;
            }
        });
    }

    private void parseExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            if (mAuth.getCurrentUser() != null)
                mUId = mAuth.getCurrentUser().getUid();
            if (mAuth.getCurrentUser() != null)
                mUserName = mAuth.getCurrentUser().getDisplayName();
            setDate(Calendar.getInstance());
        } else {
            mUId = bundle.getString(Keys.Extras.EXTRA_UID, mAuth.getCurrentUser().getUid());
            mUserName = bundle.getString(Keys.Extras.EXTRA_USER_NAME, mAuth.getCurrentUser().getDisplayName());
            String date = bundle.getString(Keys.Extras.EXTRA_DATE);
            if (date != null)
                setDate(date);
            else
                setDate(Calendar.getInstance());
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        switch (requestCode) {
            case REQUEST_IMAGE_PERMISSION:
                startCamera();
                break;
        }
    }

    void startCamera() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = timeStamp + ".jpg";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
            File file = new File(pictureImagePath);
            Uri outputFileUri = Uri.fromFile(file);
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(cameraIntent, Constants.REQUEST_CODE_CAMERA);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.image_permissions), REQUEST_IMAGE_PERMISSION, perms);
        }
    }

    @Override
    public void onPermissionsDenied(final int requestCode, final List<String> perms) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // handle request from camera
        if (requestCode == Constants.REQUEST_CODE_CAMERA) {
            if (resultCode == RESULT_OK) {
                showProgressBarUpload();
                isChanged = true;
                Bitmap bitmap = BitmapFactory.decodeFile(pictureImagePath);//= imageView.getDrawingCache();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] bytes = baos.toByteArray();
                // Meta data for imageView
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("imageView/jpg")
                        .setCustomMetadata("Date", mReport.getDate())
                        .setCustomMetadata("UserName", mReport.getUserName())
                        .build();
                // name of file in Storage
                final String filename = DateUtils.getCurrentTimeStamp() + "_" + mReport.getUserId() + ".jpg";
                UploadTask uploadTask = FirebaseStorage.getInstance()
                        .getReference(DefaultConfigurations.STORAGE_REPORT)
                        .child(filename)
                        .putBytes(bytes, metadata);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //    addCost(mTitle1, mTitle2, mPrice, filename);
                        mReport.imageUri = filename;//taskSnapshot.getDownloadUrl().toString();//filename;
                        hideProgressBarUpload();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        hideProgressBarUpload();
                    }
                });
            } else { //resultCode = CANCEL
            }
        }
        //other request code
    }

    void showProgressBarUpload() {
        progressBarUpload.setVisibility(View.VISIBLE);
        textPhoto.setText("Загрузка...");
    }

    void hideProgressBarUpload() {
        progressBarUpload.setVisibility(View.GONE);
        textPhoto.setText("Фото загружено!");
    }

    @Override
    public void onBackPressed() {
        if (progressBarUpload.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, R.string.photo_uploading, Toast.LENGTH_SHORT).show();
            return;
        }
        // Show Dialog if we have not saved data
        if (isChanged) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
            builder.setTitle(R.string.dialog_report_save_before_close_title)
                    .setMessage(R.string.dialog_report_save_before_close_text)
                    .setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            saveReportToDB();
                            onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                            isChanged = false;
                            onBackPressed();
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void loadMK() {
        mkList.clear();
        FirebaseDatabase.getInstance().getReference(DefaultConfigurations.DB_MK).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Mk mk = dataSnapshot.getValue(Mk.class);
                if (mk != null) {
                    mkList.add(0, mk);
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

    private void chooseMkDialog() {
        final ArrayList<String> mkNames = new ArrayList<>();
        for (Mk mk : mkList) {
            mkNames.add(mk.getTitle1());
        }
        mkNames.add("[не обрано]");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_choose_mk)
                .setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, mkNames),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == mkList.size()) {
                                    mReport.mkRef = "";
                                    mReport.mkName = "";
                                } else {
                                    mReport.mkRef = mkList.get(which).getKey();
                                    mReport.mkName = mkList.get(which).getTitle1();
                                }
                                isChanged = true;
                                updateMkName();
                            }
                        });
        builder.create();
        builder.show();
    }

    /**
     * Initialize DatePicker
     */
    private void initDatePicker() {
        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                setDate(year, monthOfYear, dayOfMonth);
            }
        };
        // Pop up the Date Picker after user clicked on editText
        panelDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(ReportActivity.this, dateSetListener,
                        mDate.get(Calendar.YEAR), mDate.get(Calendar.MONTH),
                        mDate.get(Calendar.DAY_OF_MONTH));
                if (!Utils.isAdmin())
                    dpd.getDatePicker().setMinDate(mDate.getTime().getTime());
                dpd.show();
            }
        });
    }

    /**
     * Initialize references for all Views
     */
    private void initRef() {
        progressBarUpload = findViewById(R.id.progress_bar_upload);
        panelPhoto = findViewById(R.id.panel_photo);
        panelRoomExpand = findViewById(R.id.panel_room_expand);
        panelRoomExpand2 = findViewById(R.id.panel_room_expand2);
        panelRoomExpand3 = findViewById(R.id.panel_room_expand3);
        textDate = findViewById(R.id.text_date);
        editComment = findViewById(R.id.edit_comment);
        textPhoto = findViewById(R.id.edit_photo);
        panelDate = findViewById(R.id.panel_date);
        // Room
        swipeLayout = findViewById(R.id.swipe_layout);
        textRoomTotal = findViewById(R.id.text_room_total);
        textRoom60 = findViewById(R.id.text_room_60);
        textRoom30 = findViewById(R.id.text_room_30);
        textRoom20 = findViewById(R.id.text_room_20);
        textRoom10 = findViewById(R.id.text_room_10);
        seekRoom60 = (SeekBar) findViewById(R.id.seek_room_60);
        seekRoom30 = (SeekBar) findViewById(R.id.seek_room_30);
        seekRoom20 = (SeekBar) findViewById(R.id.seek_room_20);
        seekRoom10 = (SeekBar) findViewById(R.id.seek_room_10);
        inputRoom60 = findViewById(R.id.input_room_60);
        inputRoom30 = findViewById(R.id.input_room_30);
        inputRoom20 = findViewById(R.id.input_room_20);
        inputRoom10 = findViewById(R.id.input_room_10);
        // BirthDay
        swipeLayout2 = findViewById(R.id.swipe_layout2);
        textBdayTotal = findViewById(R.id.text_bday_total);
        textBday50 = findViewById(R.id.text_bday_50);
        textBday10 = findViewById(R.id.text_bday_10);
        textBday30 = findViewById(R.id.text_bday_30);
        textBdayMk = findViewById(R.id.text_bday_mk_done);
        seekBday50 = (SeekBar) findViewById(R.id.seek_bday_50);
        seekBday10 = (SeekBar) findViewById(R.id.seek_bday_10);
        seekBday30 = (SeekBar) findViewById(R.id.seek_bday_30);
        seekBdayMk = (SeekBar) findViewById(R.id.seek_bday_mk_done);
        inputBday50 = findViewById(R.id.input_bday_50);
        inputBday10 = findViewById(R.id.input_bday_10);
        inputBday30 = findViewById(R.id.input_bday_30);
        // MK
        swipeLayout3 = findViewById(R.id.swipe_layout3);
        textMkName = findViewById(R.id.text_mk_name);
        seekMk1 = (SeekBar) findViewById(R.id.seek_mk_1);
        seekMk2 = (SeekBar) findViewById(R.id.seek_mk_2);
        seekMkT1 = (SeekBar) findViewById(R.id.seek_mk_t1);
        seekMkT2 = (SeekBar) findViewById(R.id.seek_mk_t2);
        textMkTotal = findViewById(R.id.text_mk_total);
        textMk1 = findViewById(R.id.text_mk_1);
        textMk2 = findViewById(R.id.text_mk_2);
        textMkT1 = findViewById(R.id.text_mk_t1);
        textMkT2 = findViewById(R.id.text_mk_t2);
        inputMk1 = findViewById(R.id.input_mk_1);
        inputMk2 = findViewById(R.id.input_mk_2);
        switchMyMk = (Switch) findViewById(R.id.switch_my_mk);
        buttonChooseMk = findViewById(R.id.button_choose_mk);
    }

    /**
     * Set OnSeekBarChanged and TextChanged Listeners for inputs
     */
    private void initSeeks() {
        seekRoom60.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReport.r60 = progress;
                updateRoomTotal();
                if (fromUser)
                    closeSoftKeyBoard();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekRoom30.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReport.r30 = progress;
                updateRoomTotal();
                if (fromUser)
                    closeSoftKeyBoard();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekRoom20.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReport.r20 = progress;
                updateRoomTotal();
                if (fromUser)
                    closeSoftKeyBoard();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekRoom10.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReport.r10 = progress;
                updateRoomTotal();
                if (fromUser)
                    closeSoftKeyBoard();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBday50.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReport.b50 = progress;
                updateBdayTotal();
                if (fromUser)
                    closeSoftKeyBoard();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBday10.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReport.b10 = progress;
                updateBdayTotal();
                if (fromUser)
                    closeSoftKeyBoard();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBday30.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReport.b30 = progress;
                if (mReport.b30 > 0)
                    mReport.bMk = 1;
                else
                    mReport.bMk = 0;
                if (mReport.b30 > 10)
                    mReport.bMk = 2;
                if (mReport.b30 > 20)
                    mReport.bMk = 3;
                seekBdayMk.setProgress(mReport.bMk);
                updateBdayTotal();
                if (fromUser)
                    closeSoftKeyBoard();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBdayMk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReport.bMk = progress;
                String mkDone = getString(R.string.mk_done) + mReport.bMk;
                textBdayMk.setText(mkDone);
                updateBdayTotal();
                if (fromUser)
                    closeSoftKeyBoard();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        // MK
        seekMkT1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReport.mkt1 = progress;
                updateMkTotal();
                if (fromUser)
                    closeSoftKeyBoard();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    EditText ed = (EditText) v;
                    ed.setSelection(ed.getText().length());
                }
            }
        };
        seekMkT2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReport.mkt2 = progress;
                updateMkTotal();
                if (fromUser)
                    closeSoftKeyBoard();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekMk1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReport.mk1 = progress;
                updateMkTotal();
                if (fromUser)
                    closeSoftKeyBoard();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekMk2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mReport.mk2 = progress;
                updateMkTotal();
                if (fromUser)
                    closeSoftKeyBoard();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        inputRoom60.setOnFocusChangeListener(focusChangeListener);
        inputRoom60.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }
                mReport.r60 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.toString().isEmpty())
                    return;
                String sub = s.toString().substring(0, 1);
                if (s.length() == 2 && sub.equals("0"))
                    s.delete(0, 1);
            }
        });
        inputRoom30.setOnFocusChangeListener(focusChangeListener);
        inputRoom30.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }
                mReport.r30 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.toString().isEmpty())
                    return;
                String sub = s.toString().substring(0, 1);
                if (s.length() == 2 && sub.equals("0"))
                    s.delete(0, 1);
            }
        });
        inputRoom20.setOnFocusChangeListener(focusChangeListener);
        inputRoom20.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }
                mReport.r20 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.toString().isEmpty())
                    return;
                String sub = s.toString().substring(0, 1);
                if (s.length() == 2 && sub.equals("0"))
                    s.delete(0, 1);
            }
        });
        inputRoom10.setOnFocusChangeListener(focusChangeListener);
        inputRoom10.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }
                mReport.r10 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.toString().isEmpty())
                    return;
                String sub = s.toString().substring(0, 1);
                if (s.length() == 2 && sub.equals("0"))
                    s.delete(0, 1);
            }
        });
        inputBday50.setOnFocusChangeListener(focusChangeListener);
        inputBday50.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }
                mReport.b50 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.toString().isEmpty())
                    return;
                String sub = s.toString().substring(0, 1);
                if (s.length() == 2 && sub.equals("0"))
                    s.delete(0, 1);
            }
        });
        inputBday10.setOnFocusChangeListener(focusChangeListener);
        inputBday10.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }
                mReport.b10 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.toString().isEmpty())
                    return;
                String sub = s.toString().substring(0, 1);
                if (s.length() == 2 && sub.equals("0"))
                    s.delete(0, 1);
            }
        });
        inputBday30.setOnFocusChangeListener(focusChangeListener);
        inputBday30.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }
                mReport.b30 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.toString().isEmpty())
                    return;
                String sub = s.toString().substring(0, 1);
                if (s.length() == 2 && sub.equals("0"))
                    s.delete(0, 1);
            }
        });
        inputMk1.setOnFocusChangeListener(focusChangeListener);
        inputMk1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }
                mReport.mk1 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.toString().isEmpty())
                    return;
                String sub = s.toString().substring(0, 1);
                if (s.length() == 2 && sub.equals("0"))
                    s.delete(0, 1);
            }
        });
        inputMk2.setOnFocusChangeListener(focusChangeListener);
        inputMk2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int value = 0;
                try {
                    value = Integer.parseInt(String.valueOf(s));
                } catch (Exception e) {
                    //eat it
                }
                mReport.mk2 = value;
                updateSeekBars();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.toString().isEmpty())
                    return;
                String sub = s.toString().substring(0, 1);
                if (s.length() == 2 && sub.equals("0"))
                    s.delete(0, 1);
            }
        });
        switchMyMk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mReport.mkMy = isChecked;
                isChanged = true;
            }
        });
        editComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mReport.comment = editComment.getText().toString();
                isChanged = true;
            }
        });
    }

    /**
     * Clear focus on focused View and hide Soft Keyboard
     */
    private void closeSoftKeyBoard() {
        // Clear Focus
        inputRoom60.clearFocus();
        inputRoom30.clearFocus();
        inputRoom20.clearFocus();
        inputRoom10.clearFocus();
        inputBday50.clearFocus();
        inputBday30.clearFocus();
        inputMk1.clearFocus();
        inputMk2.clearFocus();
        // Check if no view has focus:
        View view = ReportActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Initialize Swipe panels and Expanded panels
     */
    private void initSwipesAndExpandPanels() {
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, R.id.swipe_menu);
        swipeLayout.setLeftSwipeEnabled(true);
        swipeLayout.setBottomSwipeEnabled(false);
        swipeLayout2.addDrag(SwipeLayout.DragEdge.Left, R.id.swipe_menu2);
        swipeLayout2.setLeftSwipeEnabled(true);
        swipeLayout2.setBottomSwipeEnabled(false);
        swipeLayout3.addDrag(SwipeLayout.DragEdge.Left, R.id.swipe_menu3);
        swipeLayout3.setLeftSwipeEnabled(true);
        swipeLayout3.setBottomSwipeEnabled(false);
        panelRoomExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsFuture) return;
                swipeLayout.setVisibility((swipeLayout.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
                swipeLayout2.setVisibility(View.GONE);
                swipeLayout3.setVisibility(View.GONE);
            }
        });
        panelRoomExpand2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsFuture) return;
                swipeLayout2.setVisibility((swipeLayout2.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
                swipeLayout.setVisibility(View.GONE);
                swipeLayout3.setVisibility(View.GONE);
            }
        });
        panelRoomExpand3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsFuture) return;
                swipeLayout3.setVisibility((swipeLayout3.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
                swipeLayout2.setVisibility(View.GONE);
                swipeLayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * If exist load report from DB for selected day, otherwise create new report
     */
    private void loadReportFromDB() {
        mDatabase.getReference(DefaultConfigurations.DB_REPORTS)
                .child(mDateYear).child(mDateMonth).child(mDateDay).child(mUId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mReport = dataSnapshot.getValue(Report.class);
                        if (mReport == null) {
                            mReport = new Report(mUId, mUserName, mDateStr);
                        }
                        updateSeekBars();
                        updateMkName();
                        updateComments();
                        updatePhoto();
                        isChanged = false;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void updatePhoto() {
        String uri = mReport.imageUri;
        if (uri != null && !uri.isEmpty()) {
            textPhoto.setText(R.string.photo_uploaded);
        } else
            textPhoto.setText(R.string.text_add_photo);
    }

    private void deleteReport() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ReportActivity.this);
        builder.setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton("Видалити", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // delete from DB
                        deleteReportFromDB();
                        loadReportFromDB();
                    }
                })
                .setNegativeButton("Повернутись", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    private void deleteReportFromDB() {
        // Delete item from DB
        isChanged = false;
        mDatabase.getReference(DefaultConfigurations.DB_REPORTS)
                .child(mDateYear).child(mDateMonth).child(mDateDay)
                .child(mUId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(textRoom60, getString(R.string.toast_report_deleted), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void clearReport(boolean clearMK) {
        mReport.clearReport(clearMK);
        updateSeekBars();
        updateMkName();
        updatePhoto();
        updateComments();
        updateTitle();
        if (clearMK)
            Snackbar.make(textRoom60, getString(R.string.toast_report_cleared), Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Save current report to DB
     */
    private void saveReportToDB() {
        isChanged = false;
        mDatabase.getReference(DefaultConfigurations.DB_REPORTS)
                .child(mDateYear).child(mDateMonth).child(mDateDay)
                .child(mUId)
                .setValue(mReport).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(textRoom60, getString(R.string.toast_report_saved), Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.action_go_to_calendar), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // show calendar activity
                                Intent intent = new Intent(ReportActivity.this, CalendarActivity.class);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        });
    }

    /**
     * Update View with Mk name if exist
     */
    private void updateMkName() {
        //notification
        if (mReport.getMkName() != null && !mReport.getMkName().isEmpty())
            textMkName.setText(mReport.getMkName());
        else
            textMkName.setText(R.string.text_mk_full);
    }

    private void updateComments() {
        // comment
        if (mReport.getComment() != null && !mReport.getComment().isEmpty())
            editComment.setText(mReport.getComment());
        else
            editComment.setText("");
    }

    /**
     * Update all text views on Room Panel
     * Update Report with new values
     * Update Title of Activity
     */
    void updateRoomTotal() {
        textRoom60.setText("60 грн х " + mReport.r60 + " = " + (mReport.r60 * 60) + " ГРН");
        textRoom30.setText("30 грн х " + mReport.r30 + " = " + (mReport.r30 * 30) + " ГРН");
        textRoom20.setText("20 грн х " + mReport.r20 + " = " + (mReport.r20 * 20) + " ГРН");
        textRoom10.setText("10 грн х " + mReport.r10 + " = " + (mReport.r10 * 10) + " ГРН");
        if (!inputRoom60.hasFocus())
            inputRoom60.setText(String.valueOf(mReport.r60));
        if (!inputRoom30.hasFocus())
            inputRoom30.setText(String.valueOf(mReport.r30));
        if (!inputRoom20.hasFocus())
            inputRoom20.setText(String.valueOf(mReport.r20));
        if (!inputRoom10.hasFocus())
            inputRoom10.setText(String.valueOf(mReport.r10));
        mReport.totalRoom = mReport.r60 * 60 + mReport.r30 * 30 + mReport.r20 * 20 + mReport.r10 * 10;
        String total = DateUtils.getIntWithSpace(mReport.totalRoom) + " ГРН";
        textRoomTotal.setText(total);
        updateTitle();
    }

    /**
     * Update all text views on BirthDayPanel
     * Update Report with new values
     * Update Title of Activity
     */
    void updateBdayTotal() {
        textBday50.setText("Кімната: 50 грн х " + mReport.b50 + " = " + (mReport.b50 * 50) + " ГРН");
        textBday10.setText("Кімната: 10 грн х " + mReport.b10 + " = " + (mReport.b10 * 10) + " ГРН");
        textBday30.setText("МК: 30 грн х " + mReport.b30 + " = " + (mReport.b30 * 30) + " ГРН");
        String mkDone = getString(R.string.mk_done) + mReport.bMk;
        textBdayMk.setText(mkDone);
        if (!inputBday50.hasFocus())
            inputBday50.setText(String.valueOf(mReport.b50));
        if (!inputBday10.hasFocus())
            inputBday10.setText(String.valueOf(mReport.b10));
        if (!inputBday30.hasFocus())
            inputBday30.setText(String.valueOf(mReport.b30));
        mReport.totalBday = mReport.b50 * 50 + mReport.b10 * 10 + mReport.b30 * 30;
        String total = DateUtils.getIntWithSpace(mReport.totalBday) + " ГРН";
        textBdayTotal.setText(total);
        updateTitle();
    }

    /**
     * Update all text views on Mk Panel
     * Update Report with new values
     * Update Title of Activity
     */
    void updateMkTotal() {
        int tar1 = 30 + mReport.mkt1 * 10;
        int tar2 = 30 + mReport.mkt2 * 10;
        mReport.totalMk = tar1 * mReport.mk1 + tar2 * mReport.mk2;
        textMkT1.setText("Тариф 1: " + tar1 + " грн x");
        textMkT2.setText("Тариф 2: " + tar2 + " грн x");
        textMk1.setText("  " + mReport.mk1 + " = " + (tar1 * mReport.mk1) + " ГРН");
        textMk2.setText("  " + mReport.mk2 + " = " + (tar2 * mReport.mk2) + " ГРН");
        if (!inputMk1.hasFocus())
            inputMk1.setText(String.valueOf(mReport.mk1));
        if (!inputMk2.hasFocus())
            inputMk2.setText(String.valueOf(mReport.mk2));
        String total = DateUtils.getIntWithSpace(mReport.totalMk) + " ГРН";
        textMkTotal.setText(total);
        updateTitle();
    }

    /**
     * Update Title of Activity
     */
    void updateTitle() {
        mReport.total = (mReport.totalRoom + mReport.totalBday + mReport.totalMk);
        setTitle(getResources().getString(R.string.title_activity_report) + " (" + DateUtils.getIntWithSpace(mReport.total) + " ГРН)");
        isChanged = true;
    }

    /**
     * Load values from Report to Seek Bars
     */
    void updateSeekBars() {
        seekRoom60.setProgress(mReport.r60);
        seekRoom30.setProgress(mReport.r30);
        seekRoom20.setProgress(mReport.r20);
        seekRoom10.setProgress(mReport.r10);
        seekBday50.setProgress(mReport.b50);
        seekBday10.setProgress(mReport.b10);
        seekBday30.setProgress(mReport.b30);
        seekBdayMk.setProgress(mReport.bMk);
        seekMk1.setProgress(mReport.mk1);
        seekMk2.setProgress(mReport.mk2);
        seekMkT1.setProgress(mReport.mkt1);
        seekMkT2.setProgress(mReport.mkt2);
        switchMyMk.setChecked(mReport.mkMy);
        updateTitle();
    }

    /**
     * Set new values of current date
     *
     * @param calendar
     */
    private void setDate(Calendar calendar) {
        mDate = calendar;
        mDateStr = mSdf.format(mDate.getTime());
        fillDateStr(mDateStr);
        mIsFuture = DateUtils.future(mDateStr);
    }

    private void setDate(String dateStr) {
        mDateStr = dateStr;
        mDate = getDateFromStr(mDateStr);
        fillDateStr(mDateStr);
        mIsFuture = DateUtils.future(mDateStr);
    }

    private void setDate(int year, int monthOfYear, int dayOfMonth) {
        mDate.set(Calendar.YEAR, year);
        mDate.set(Calendar.MONTH, monthOfYear);
        mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        mDateStr = mSdf.format(mDate.getTime());
        fillDateStr(mDateStr);
        mIsFuture = DateUtils.future(mDateStr);
        if (mIsFuture) {
            swipeLayout.setVisibility(View.GONE);
            swipeLayout2.setVisibility(View.GONE);
            swipeLayout3.setVisibility(View.GONE);
            clearReport(false);
        }
    }

    private void fillDateStr(String dateStr) {
        int first = dateStr.indexOf('-');
        int last = dateStr.lastIndexOf('-');
        mDateDay = dateStr.substring(0, first);
        mDateMonth = dateStr.substring(first + 1, last);
        mDateYear = dateStr.substring(last + 1);
        textDate.setText(mDateStr);
        if (mReport != null)
            mReport.date = mDateStr;
    }

    private Calendar getDateFromStr(String dateStr) {
        Calendar date = Calendar.getInstance();
        try {
            date.setTime(mSdf.parse(dateStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        switch (id) {
            case R.id.action_add: {
                saveReportToDB();
                break;
            }
            case R.id.action_today: {
                setDate(Calendar.getInstance());
                break;
            }
            case R.id.action_clear_report: {
                clearReport(true);
                break;
            }
            case R.id.action_delete_report: {
                deleteReport();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
