package com.skooltech.skooltechsolutionsacademy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setting the AppTheme to display the activity
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences("app", Context.MODE_PRIVATE);
        String appStatus = "";
        appStatus = settings.getString("appStatus","");

        if(appStatus == ""){
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else{
            loadFragment(new HomeFragment());
        }

        /*DbHandler dbHandler = new DbHandler(MainActivity.this);
        ArrayList<HashMap<String,String>> dataList = dbHandler.getAttendanceById("007478182",20);
        ArrayList<HashMap<String,String>> dataList = dbHandler.getDatabase();

        Log.d("test1234",dataList.toString());*/

        //getting bottom navigation view and attaching the listener
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.bottom_nav_home:
                fragment = new HomeFragment();
                break;

            case R.id.bottom_nav_notification:
                fragment = new NotificationFragment();
                break;

            case R.id.bottom_nav_announcement:
                fragment = new AnnouncementFragment();
                break;

            case R.id.bottom_nav_attendance:
                fragment = new AttendanceFragment();
                break;

            case R.id.bottom_nav_profile:
                fragment = new ProfileFragment();
                break;
        }

        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
