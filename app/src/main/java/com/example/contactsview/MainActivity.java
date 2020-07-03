package com.example.contactsview;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_READ_CONTACTS = 1;
    private RecyclerView contactsView = null;
    private FloatingActionButton fab = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contactsView = findViewById(R.id.contactsView);
        int hasReadContactsPermission = ContextCompat.checkSelfPermission(this, READ_CONTACTS);
        Log.d(TAG, "onCreate: checkSelfPermission = " + hasReadContactsPermission);

        if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: requesting permission...");
            ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: fab onClick starts");
                if (ContextCompat.checkSelfPermission(MainActivity.this, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    String[] projection = {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};
                    ContentResolver contentResolver = getContentResolver();
                    Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                            projection,
                            null,
                            null,
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);

                    if (cursor != null) {
                        List<String> contacts = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            contacts.add(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)));
                        }
                        cursor.close();
                        Log.d(TAG, "onClick: contacts = " + contacts.get(0) + contacts.get(1));
                        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(contacts);
                        contactsView.setAdapter(recyclerViewAdapter);
                        contactsView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    }
                } else {
                    Snackbar.make(view, "This app needs read access to your contacts to display them.",
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction("Action", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Log.d(TAG, "Snackbar onClick: starts");
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, READ_CONTACTS)) {
                                        Log.d(TAG, "onClick: calling request permission");
                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
                                    } else {
                                        // The user has permanently denied access so take them to settings
                                        Log.d(TAG, "Snackbar onClick: launching settings");
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                                        Log.d(TAG, "Snackbar onClick: Uri = " + uri.toString());
                                        intent.setData(uri);
                                        MainActivity.this.startActivity(intent);
                                    }
                                    Log.d(TAG, "Snackbar onClick: ends");
                                }
                            }).show();
                }
                Log.d(TAG, "fab onClick: ends");
            }
        });
        Log.d(TAG, "onCreate: ends");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}