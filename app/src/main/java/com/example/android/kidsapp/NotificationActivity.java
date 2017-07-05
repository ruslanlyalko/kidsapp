package com.example.android.kidsapp;

import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setContentView(R.layout.activity_notification);

        //setListAdapter(mListAdapter);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(mListAdapter);
    }


    private BaseAdapter mListAdapter = new BaseAdapter() {
        public int iddd = 0;
        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position + 1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.notification_item, container, false);
            }

            // Because the list item contains multiple touch targets, you should not override
            // onListItemClick. Instead, set a click listener for each target individually.

            //((TextView) convertView.findViewById(R.id.text1)).setText("Count" + position);

            convertView.findViewById(R.id.primary_target).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {


                            Toast.makeText(NotificationActivity.this,
                                    String.valueOf(getItemId(position)),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            convertView.findViewById(R.id.secondary_action).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(NotificationActivity.this,
                                    "2",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            return convertView;
        }
    };

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
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
}
