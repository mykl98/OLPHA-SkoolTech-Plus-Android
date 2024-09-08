package com.skooltech.OLPHA_skooltechplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;

public class MainActivity extends AppCompatActivity{
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    RequestQueue requestQueue;

    private BottomNavigationView bottomNavigationView;

    String TAG = "Main Activity";

    public static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        createNotificationChannel();
        instance = this;

        sharedPreferences = getSharedPreferences("skooltech", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        FirebaseApp.initializeApp(this);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(!task.isSuccessful()){
                            showOkDialog(false, "Unable to retrieve firebase registration token!");
                            return;
                        }
                        String token = task.getResult();
                        editor.putString("token", token);
                        editor.commit();
                    }
                });


        String isLoggedIn = sharedPreferences.getString("isLoggedIn", "");

        //signOut();

        if(!isLoggedIn.equals("true")){
            gotoRegisterActivity();
        }

        createNotificationChannel();

        loadFragment(new HomeFragment());

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                final int itemId = item.getItemId();
                if(itemId == R.id.bottom_nav_home){
                    fragment = new HomeFragment();
                }else if(itemId == R.id.bottom_nav_notification){
                    fragment = new NotificationFragment();
                }else if(itemId == R.id.bottom_nav_announcement){
                    fragment = new AnnouncementFragment();
                }else if(itemId == R.id.bottom_nav_attendance){
                    fragment = new AttendanceFragment();
                }else if(itemId == R.id.bottom_nav_profile){
                    fragment = new ProfileFragment();
                }

                return loadFragment(fragment);
            }
        });
    }

    private boolean loadFragment(Fragment fragment){
        if(fragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void gotoRegisterActivity(){
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void signOut(){
        editor.putString("isLoggedIn", "false");
        editor.commit();
        FirebaseAuth.getInstance().signOut();
        gotoRegisterActivity();
    }

    public void createNotificationChannel() {

        String CHANNEL_ID = "SkoolTech";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SkoolTech_Plus_Channel";
            String description = "SkoolTech_Plus_Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[] {android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    public void showOkDialog(Boolean type, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View customLayout = this.getLayoutInflater().inflate(R.layout.ok_dialog_layout, null);
        builder.setView(customLayout);
        TextView titleTextview = customLayout.findViewById(R.id.ok_dialog_layout_title);
        TextView msgTextview = customLayout.findViewById(R.id.ok_dialog_layout_body);
        Button okButton = customLayout.findViewById(R.id.ok_dialog_layout_okbtn);
        if(type){
            titleTextview.setText("Success");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                titleTextview.setBackground(getDrawable(R.color.success));
                okButton.setBackgroundColor(getResources().getColor(R.color.success));
            }
        }else{
            titleTextview.setText("Error");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                titleTextview.setBackground(getDrawable(R.color.danger));
                okButton.setBackgroundColor(getResources().getColor(R.color.danger));
            }
        }

        msgTextview.setText(message);

        AlertDialog dialog = builder.create();
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public String decodeText(String inText){
        String decodedText = inText.replace("%2520", " ");
        decodedText = decodedText.replace("\\n", "\n");
        return decodedText;
    }

    public void requestAPI(Map<String, String> parameters, final APICallback callback){
        String url = getString(R.string.app_api);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error.getLocalizedMessage());
                    }
                }
        ){
            @Override
            protected Map<String,String> getParams() {
                return parameters;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 4800,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(MainActivity.this);
        }
        requestQueue.getCache().clear();
        requestQueue.add(postRequest);
    }

    public interface APICallback{
        void onSuccess(String result);

        void onError(String error);
    }
}
